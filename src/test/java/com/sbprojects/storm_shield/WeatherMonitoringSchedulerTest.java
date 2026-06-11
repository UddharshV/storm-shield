package com.sbprojects.storm_shield;

import com.sbprojects.storm_shield.event.SevereWeatherEvent;
import com.sbprojects.storm_shield.model.FreightRoute;
import com.sbprojects.storm_shield.repository.FreightRouteRepository;
import com.sbprojects.storm_shield.service.WeatherMonitoringScheduler;
import com.sbprojects.storm_shield.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class WeatherMonitoringSchedulerTest {

    @Mock private FreightRouteRepository routeRepository;
    @Mock private WeatherService weatherService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private WeatherMonitoringScheduler scheduler;

    @Test
    public void testMonitorHubWeather_LoopsThroughActiveRoutes(){
        MockitoAnnotations.openMocks(this);

        //1. Arrange: Create mock routes to simulate data returning from the database
        FreightRoute route1 = new FreightRoute("Memphis", "Dallas", "OPERATIONAL");
        FreightRoute route2 = new FreightRoute("Chicago", "Atlanta", "OPERATIONAL");

        when(routeRepository.findAll()).thenReturn(List.of(route1, route2));
        when(weatherService.getWindSpeed(anyString())).thenReturn(12.5);

        //2. Act: Force the execution of the scheduled loop method manually
        scheduler.monitorHubWeather();

        //3. Assert: Verify the scheduler contacted the database and checked weather for BOTH unique cities
        verify(routeRepository, times(1)).findAll();
        verify(weatherService, times(1)).getWindSpeed("Memphis");
        verify(weatherService, times(1)).getWindSpeed("Chicago");
    }
    @Test
    public void testMonitorHubWeather_PublishesEventOnHighWind(){
        MockitoAnnotations.openMocks(this);

        //1. Arrange
        FreightRoute route = new FreightRoute("Memphis", "Dallas", "OPERATIONAL");
        when(routeRepository.findAll()).thenReturn(List.of(route));
        when(weatherService.getWindSpeed("Memphis")).thenReturn(52.0); //Breach threshold

        //2.Act
        scheduler.monitorHubWeather();

        //3.Assert
        //Verify the system broadcasted the event exactly once
        verify(eventPublisher, times(1)).publishEvent(any(SevereWeatherEvent.class));
    }
    @Test
    public void testMonitorHubWeather_PublishesNoEventOnLowWind(){
        MockitoAnnotations.openMocks(this);

        //1. Arrange
        FreightRoute route = new FreightRoute("Memphis", "Dallas", "OPERATIONAL");
        when(routeRepository.findAll()).thenReturn(List.of(route));
        when(weatherService.getWindSpeed("Memphis")).thenReturn(12.0); //Below threshold

        //2.Act
        scheduler.monitorHubWeather();

        //3.Assert
        //Verify the system doesn't broadcast the event
        verify(eventPublisher, times(0)).publishEvent(any(SevereWeatherEvent.class));
    }
}
