package com.sbprojects.storm_shield;

import com.sbprojects.storm_shield.event.SevereWeatherEvent;
import com.sbprojects.storm_shield.listener.RouteRerouterListener;
import com.sbprojects.storm_shield.model.FreightRoute;
import com.sbprojects.storm_shield.repository.FreightRouteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

public class RouteRerouterListenerTest {

    @Mock
    private FreightRouteRepository routeRepository;

    @InjectMocks private RouteRerouterListener rerouterListener;

    @Test
    public void testHandleSevereWeather_UpdatesImpactedRouteStatusToDelayed(){
        MockitoAnnotations.openMocks(this);

        //1. Arrange: If a storm hits Memphis, find an operational route tied to it
        SevereWeatherEvent mockEvent = new SevereWeatherEvent(this, "Memphis", 45.0);
        FreightRoute mockRoute = new FreightRoute("Memphis", "Dallas", "OPERATIONAL");

        when(routeRepository.findBySourceCity("Memphis")).thenReturn(List.of(mockRoute));

        //2. Act: Manually trigger the event handler method
        rerouterListener.handleSevereWeather(mockEvent);

        //3. Assert: the saved route has status DELAYED
        verify(routeRepository, times(1)).save(argThat(route ->
                "DELAYED".equals(route.getStatus())
                        && "Memphis".equals(route.getSourceCity())
                        && "Dallas".equals(route.getDestinationCity())
        ));
    }
}
