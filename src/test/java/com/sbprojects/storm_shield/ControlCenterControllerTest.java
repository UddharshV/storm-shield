package com.sbprojects.storm_shield;

import com.sbprojects.storm_shield.controller.ControlCenterController;
import com.sbprojects.storm_shield.dto.RouteStatusUpdateRequest;
import com.sbprojects.storm_shield.model.FreightRoute;
import com.sbprojects.storm_shield.repository.FreightRouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ControlCenterController.getControlCenterAnalytics().
 * These tests:
 * - Mock FreightRouteRepository
 * - Call the controller method directly
 * - Assert on the analytics map
 */
@ExtendWith(MockitoExtension.class)
public class ControlCenterControllerTest {

    @Mock
    private FreightRouteRepository routeRepository;

    private ControlCenterController controller;

    @BeforeEach
    void setUp() {
        controller = new ControlCenterController(routeRepository);
    }

    @Test
    void getControlCenterAnalytics_MixedRoutes_ComputesHealthy75Percent() {
        // 1. Arrange: 4 routes, 1 delayed → 75.0% availability (HEALTHY)
        FreightRoute r1 = new FreightRoute("Memphis", "Dallas", "DELAYED");
        FreightRoute r2 = new FreightRoute("Raleigh", "Atlanta", "OPERATIONAL");
        FreightRoute r3 = new FreightRoute("Chicago", "Memphis", "OPERATIONAL");
        FreightRoute r4 = new FreightRoute("Dallas", "Chicago", "OPERATIONAL");

        when(routeRepository.findAll()).thenReturn(List.of(r1, r2, r3, r4));

        // 2. Act
        Map<String, Object> analytics = controller.getControlCenterAnalytics();

        // 3. Assert
        assertEquals(4L, analytics.get("totalMonitoredRoutes"));
        assertEquals(1L, analytics.get("activeWeatherDelays"));
        assertEquals(3L, analytics.get("activeOperationalLanes"));
        assertEquals("75.0%", analytics.get("networkAvailabilityPercentage"));
        assertEquals("HEALTHY", analytics.get("systemStatus"));

        verify(routeRepository, times(1)).findAll();
    }

    @Test
    void getControlCenterAnalytics_NoRoutes_DefaultsToHealthy100Percent() {
        // 1. Arrange: empty network
        when(routeRepository.findAll()).thenReturn(List.of());

        // 2. Act
        Map<String, Object> analytics = controller.getControlCenterAnalytics();

        // 3. Assert
        assertEquals(0L, analytics.get("totalMonitoredRoutes"));
        assertEquals(0L, analytics.get("activeWeatherDelays"));
        assertEquals(0L, analytics.get("activeOperationalLanes"));
        assertEquals("100.0%", analytics.get("networkAvailabilityPercentage"));
        assertEquals("HEALTHY", analytics.get("systemStatus"));

        verify(routeRepository, times(1)).findAll();
    }

    @Test
    void getControlCenterAnalytics_AvailabilityBelow70Percent_MarksSystemDegraded() {
        // 1. Arrange: 3 routes, 2 delayed → 33.3% availability (DEGRADED)
        FreightRoute r1 = new FreightRoute("Memphis", "Dallas", "DELAYED");
        FreightRoute r2 = new FreightRoute("Raleigh", "Atlanta", "DELAYED");
        FreightRoute r3 = new FreightRoute("Chicago", "Memphis", "OPERATIONAL");

        when(routeRepository.findAll()).thenReturn(List.of(r1, r2, r3));

        // 2. Act
        Map<String, Object> analytics = controller.getControlCenterAnalytics();

        // 3. Assert
        assertEquals(3L, analytics.get("totalMonitoredRoutes"));
        assertEquals(2L, analytics.get("activeWeatherDelays"));
        assertEquals(1L, analytics.get("activeOperationalLanes"));
        assertEquals("33.3%", analytics.get("networkAvailabilityPercentage"));
        assertEquals("DEGRADED", analytics.get("systemStatus"));

        verify(routeRepository, times(1)).findAll();
    }

    @Test
    void getControlCenterAnalytics_AvailabilityExactly70Percent_MarksSystemDegraded() {
        // 1. Arrange: 10 routes, 7 operational, 3 delayed → 70.0% availability
        // 7 / 10 * 100 = 70.0 → should be DEGRADED because condition is > 70.0 for HEALTHY
        FreightRoute op1 = new FreightRoute("A", "B", "OPERATIONAL");
        FreightRoute op2 = new FreightRoute("C", "D", "OPERATIONAL");
        FreightRoute op3 = new FreightRoute("E", "F", "OPERATIONAL");
        FreightRoute op4 = new FreightRoute("G", "H", "OPERATIONAL");
        FreightRoute op5 = new FreightRoute("I", "J", "OPERATIONAL");
        FreightRoute op6 = new FreightRoute("K", "L", "OPERATIONAL");
        FreightRoute op7 = new FreightRoute("M", "N", "OPERATIONAL");

        FreightRoute d1 = new FreightRoute("O", "P", "DELAYED");
        FreightRoute d2 = new FreightRoute("Q", "R", "DELAYED");
        FreightRoute d3 = new FreightRoute("S", "T", "DELAYED");

        when(routeRepository.findAll()).thenReturn(
                List.of(op1, op2, op3, op4, op5, op6, op7, d1, d2, d3)
        );

        // 2. Act
        Map<String, Object> analytics = controller.getControlCenterAnalytics();

        // 3. Assert
        assertEquals(10L, analytics.get("totalMonitoredRoutes"));
        assertEquals(3L, analytics.get("activeWeatherDelays"));
        assertEquals(7L, analytics.get("activeOperationalLanes"));
        assertEquals("70.0%", analytics.get("networkAvailabilityPercentage"));
        assertEquals("DEGRADED", analytics.get("systemStatus"));

        verify(routeRepository, times(1)).findAll();
    }
    @Test
    void getControlCenterAnalytics_TreatsDelayedCaseInsensitively() {
        // 1. Arrange: mix of upper/lower-case statuses
        FreightRoute r1 = new FreightRoute("Memphis", "Dallas", "delayed");      // lower-case
        FreightRoute r2 = new FreightRoute("Raleigh", "Atlanta", "DELAYED");     // upper-case
        FreightRoute r3 = new FreightRoute("Chicago", "Memphis", "Operational"); // mixed-case, treated as operational

        when(routeRepository.findAll()).thenReturn(List.of(r1, r2, r3));

        // 2. Act
        Map<String, Object> analytics = controller.getControlCenterAnalytics();

        // 3. Assert
        assertEquals(3L, analytics.get("totalMonitoredRoutes"));
        assertEquals(2L, analytics.get("activeWeatherDelays"));     // both delayed/DELAYED counted
        assertEquals(1L, analytics.get("activeOperationalLanes"));
        verify(routeRepository, times(1)).findAll();
    }
    @Test
    void updateRouteStatus_ExistingRoute_UpdatesStatusAndReturnsUpdatedEntity() {
        // 1. Arrange
        Long routeId = 42L;
        FreightRoute existing = new FreightRoute("Memphis", "Dallas", "DELAYED");

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(existing));
        when(routeRepository.save(existing)).thenReturn(existing);

        RouteStatusUpdateRequest request = new RouteStatusUpdateRequest();
        request.setStatus("OPERATIONAL");

        // 2. Act
        ResponseEntity<FreightRoute> response = controller.updateRouteStatus(routeId, request);

        // 3. Assert: HTTP status and body
        assertEquals(HttpStatus.OK, response.getStatusCode());
        FreightRoute body = response.getBody();
        assertNotNull(body);
        assertEquals("OPERATIONAL", body.getStatus());

        // 4. Assert: interactions with repository
        verify(routeRepository, times(1)).findById(routeId);
        verify(routeRepository, times(1)).save(existing);
    }
    @Test
    void updateRouteStatus_NonExistingRoute_ReturnsNotFoundAndDoesNotSave() {
        // 1. Arrange
        Long missingId = 99L;
        when(routeRepository.findById(missingId)).thenReturn(Optional.empty());

        RouteStatusUpdateRequest request = new RouteStatusUpdateRequest();
        request.setStatus("OPERATIONAL");

        // 2. Act
        ResponseEntity<FreightRoute> response = controller.updateRouteStatus(missingId, request);

        // 3. Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(routeRepository, times(1)).findById(missingId);
        verify(routeRepository, never()).save(any());
    }
}