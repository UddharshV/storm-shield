package com.sbprojects.storm_shield.service;

import com.sbprojects.storm_shield.model.FreightRoute;
import com.sbprojects.storm_shield.repository.FreightRouteRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component //Registers the class as an active background daemon - makes this a Spring bean
public class WeatherMonitoringScheduler {

    private final FreightRouteRepository routeRepository;
    private final WeatherService weatherService;

    //Constructor Injection: Spring passes our database tool and weather client automatically
    public WeatherMonitoringScheduler(FreightRouteRepository routeRepository, WeatherService weatherService) {
        this.routeRepository = routeRepository;
        this.weatherService = weatherService;
    }

    //The loop executes automatically every 60,000 milliseconds (1 minute)
    @Scheduled(fixedRate = 60000)
    public void monitorHubWeather(){
        System.out.println("[MONITORING SYSTEM] Scanning active freight routes for weather hazards...");

        List<FreightRoute> activeRoutes = routeRepository.findAll();

        for(FreightRoute route: activeRoutes){
            //Reaches out across the internet to check the live status of the origin hub city
            double windSpeed = weatherService.getWindSpeed(route.getSourceCity());
            System.out.println("Hub [" + route.getSourceCity() + "] Current Wind Speed: " + windSpeed + " mph");

            //Enterprise Risk Operational Parameter Check
            if(windSpeed > 40.0)
                System.out.println("[CRITICAL HAZARD DETECTED] Severe wind at " + route.getSourceCity() + "Hub! Threat Level: High.");
                //TODO: replace the print statement with an automated event playbook dispatcher

        }
    }
}
