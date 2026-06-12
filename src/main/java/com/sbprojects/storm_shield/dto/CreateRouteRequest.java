package com.sbprojects.storm_shield.dto;

public class CreateRouteRequest {

    private String sourceCity;
    private String destinationCity;

    // optional; will be ignored or used as a fallback
    private String status;

    public String getSourceCity() {
        return sourceCity;
    }

    public void setSourceCity(String sourceCity) {
        this.sourceCity = sourceCity;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}