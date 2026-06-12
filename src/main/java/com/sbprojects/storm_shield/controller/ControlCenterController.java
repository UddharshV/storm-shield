package com.sbprojects.storm_shield.controller;

import com.sbprojects.storm_shield.model.FreightRoute;
import com.sbprojects.storm_shield.repository.FreightRouteRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes") //Structures all sub-routes under a safe route URI
public class ControlCenterController {

    private final FreightRouteRepository routeRepository;

    //Constructor Injection: Spring automatically provides the DB repository bean


    public ControlCenterController(FreightRouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @GetMapping("/analytics") //Accessible via HTTP GET at http://localhost:8080/api/routes/analytics
    public Map<String, Object> getControlCenterAnalytics(){
        List<FreightRoute> allRoutes = routeRepository.findAll();

        long totalRoutes = allRoutes.size();
        long delayedRoutes = allRoutes.stream()
                .filter(route -> "DELAYED".equalsIgnoreCase(route.getStatus()))
                .count();
        long operationalRoutes = totalRoutes - delayedRoutes;

        //Calculate platform operational reliability percentage
        double networkAvailabilityPercentage = totalRoutes>0
                ?((double) operationalRoutes/totalRoutes)*100
                : 100.0; //ternary operator

        //Bundle data states into an analytics JSON map payload
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalMonitoredRoutes", totalRoutes);
        analytics.put("activeOperationalLanes", operationalRoutes);
        analytics.put("activeWeatherDelays", delayedRoutes);
        analytics.put("networkAvailabilityPercentage", String.format("%.1f%%", networkAvailabilityPercentage));
        analytics.put("systemStatus", networkAvailabilityPercentage>70.0?"HEALTHY":"DEGRADED");

        return analytics;
    }
}
