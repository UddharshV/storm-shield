package com.sbprojects.storm_shield.config;

import com.sbprojects.storm_shield.model.FreightRoute;
import com.sbprojects.storm_shield.repository.FreightRouteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component //Indicates Spring to automatically manage this class and run it on startup -> converts ot to a Spring bean
public class DataInitializer implements CommandLineRunner {
    //CommandLineRunner: tells Spring Boot: After the application context is fully started, call this run(...) method once
    //Any code inside a class that implements CommandLineRunner is executed automatically right after the application context fully boots up

    private final FreightRouteRepository routeRepository;

    //Dependency Injection: Spring automatically provides our database tool here
    public DataInitializer(FreightRouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }
    //constructor-based dependency injection: recommended style in Spring (immutable dependency, easy to test)

    @Override
    public void run(String... args) throws Exception {
        System.out.println("[SYSTEM INITIALIZATION] Seeding mock freight routes into database...");
        // Creating and saving mock regional FedEx Freight hubs and lanes
        routeRepository.save(new FreightRoute("Memphis", "Dallas", "OPERATIONAL"));
        routeRepository.save(new FreightRoute("Memphis", "Atlanta", "OPERATIONAL"));
        routeRepository.save(new FreightRoute("Raleigh", "Memphis", "OPERATIONAL"));
        routeRepository.save(new FreightRoute("Dallas", "Phoenix", "OPERATIONAL"));
        routeRepository.save(new FreightRoute("Chicago", "Memphis", "OPERATIONAL"));
        System.out.println("[SYSTEM INITIALIZATION] Database successfully seeded with " + routeRepository.count() + " active routes.");
    }
}
