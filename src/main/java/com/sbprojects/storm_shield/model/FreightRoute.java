package com.sbprojects.storm_shield.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity //Explains to Spring Boot to turn the class into an H2 database table
public class FreightRoute {

    @Id //Identifies this field as the unique primary key column
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Automatically counts IDs sequentially (1,2,3,..)
    //JPA uses field access to set the id using reflection directly on the private field -> setter not required

    private Long id;

    private String sourceCity;
    private String destinationCity;
    private String status; //Operational Status: "OPERATIONAL", "DELAYED", "REROUTED"

    public FreightRoute() { //default constructor: For the JPA framework to initialize data rows
    }
    //Constructor: allows us to quickly instantiate new routes
    public FreightRoute(String sourceCity, String destinationCity, String status) {
        this.status = status;
        this.destinationCity = destinationCity;
        this.sourceCity = sourceCity;
    }
    //Getters and Setters
    public Long getId() {
        return id;
    }

    /*
        public void setId(Long id) {
              this.id = id;
        }
        Not required -> @GeneratedValue, which means the database/JPA generates the id
        Once created, the identity of this entity is immutable from the outside world
        No setter for id protects the entity identity from being mutated by business code
     */


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
