package com.sbprojects.storm_shield.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service //Indicates to Spring - to manage this class as a reusable component (Spring bean)
public class WeatherService { //Weather data provider

    //To automatically inject the stored API key from application.properties
    @Value("${weather.api.key}")
    private String apiKey;

    //private final RestClient restClient = RestClient.create(); - Hardcodes RestClient to this class and males it impossible to mock

    //Fix: Constructor Injection: Allows us to mock this dependency in unit tests

    private final RestClient restClient;

    public WeatherService(RestClient.Builder restClientBuilder, @Value("${weather.api.base-url}") String baseUrl) {
        //Initializing the RestClient with a permanent base configuration
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public double getWindSpeed(String city){
        try {
            // Building the dynamic URL targeting a specific hub city
            String uriPath = "/weather?q=" + city + "&appid=" + apiKey + "&units=imperial";

            //Execute the GET request and read the raw JSON back as a generic Java Map
            Map<String, Object> response = restClient.get() //start building an HTTP get request
                    //The client automatically merges this with the base URL
                    .uri(uriPath) //set the URL of the request
                    .retrieve() //execute the request and get a response
                    .body(Map.class); //parse the JSON body into a Map <String, Object>

            //OpenWeatherMap nests wind speed values as: {"wind": {"speed": 14.5}}
            if (response != null && response.containsKey("wind")) {
                Map<String, Object> windData = (Map<String, Object>) response.get("wind");
                return Double.parseDouble(windData.get("speed").toString()); //TODO: extend WeatherService to return a small WeatherSnapshot DTO
            }
            //TODO: Handle an exception of weather payload response not having the wind attribute
        } catch (Exception e) {
            System.out.println("[WEATHER SERVICE ERROR] Failed to fetch data for " + city + ": " + e.getMessage()); //TODO: Improve exception handling
        }
        return 0.0; //Fallback to safe default value if the network or API keys fail
    }
}