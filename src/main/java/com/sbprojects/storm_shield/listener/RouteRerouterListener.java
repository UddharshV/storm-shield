package com.sbprojects.storm_shield.listener;

import com.sbprojects.storm_shield.event.SevereWeatherEvent;
import com.sbprojects.storm_shield.model.FreightRoute;
import com.sbprojects.storm_shield.repository.FreightRouteRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component //Registers this playbook as an independent platform worker
public class RouteRerouterListener {

    private final FreightRouteRepository routeRepository;

    public RouteRerouterListener(FreightRouteRepository routeRepository){
        this.routeRepository = routeRepository;
    }

    @EventListener // Tells Spring: "Wake this method up automatically when a SevereWeatherEvent is published
    public void handleSevereWeather(SevereWeatherEvent event){
        String targetCity = event.getCity();
        System.out.println("[PLAYBOOK ACTION] RouteRerouteListener responding to hazard event at hub: " + targetCity);

        //Find all active routes originating from the storm zone
        List<FreightRoute> impactedRoutes = routeRepository.findBySourceCity(targetCity);

        for(FreightRoute route : impactedRoutes){
            route.setStatus("DELAYED");
            routeRepository.save(route); //update the state inside the database
            System.out.println("[STATE CHANGE] Freight Route ID " + route.getId() + " (" + targetCity + " -> " + route.getDestinationCity() + ") status set to DELAYED.");
        }
    }
}
