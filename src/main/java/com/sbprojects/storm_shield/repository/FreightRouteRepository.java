package com.sbprojects.storm_shield.repository;

import com.sbprojects.storm_shield.model.FreightRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository //Indicates to Spring that this is the database access tool for managing FreightRoutes
public interface FreightRouteRepository extends JpaRepository<FreightRoute, Long>{
    /*By extending JpaRepository<FreightRoute, Long>, this single interface instantly inherits built-in database methods like:
     .findAll(), .findById(), .save() and .deleteById() completely out of the box
     */
    //Spring automatically generates the SQL query based on the method name
    //Here, it matches sourceCity to the field name on your FreightRoute entity
    List<FreightRoute> findBySourceCity(String sourceCity);
}
