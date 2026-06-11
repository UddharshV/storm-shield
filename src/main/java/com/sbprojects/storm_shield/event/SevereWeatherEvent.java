package com.sbprojects.storm_shield.event;

import org.springframework.context.ApplicationEvent;

public class SevereWeatherEvent extends ApplicationEvent {

    private final String city;
    private final double windSpeed;

    public SevereWeatherEvent(Object source, String city, double windSpeed){
        super(source); //Passes the origin component to the parent class
        this.city = city;
        this.windSpeed = windSpeed;
    }

    //Getters -> So that the listeners can read the incident facts
    public String getCity(){
        return city;
    }
    public double getWindSpeed(){
        return windSpeed;
    }
}
