package com.metro.ticketing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.metro.ticketing.entity.Route;
import com.metro.ticketing.entity.RouteStation;
import com.metro.ticketing.entity.Station;
import com.metro.ticketing.repository.RouteRepository;
import com.metro.ticketing.repository.RouteStationRepository;
import com.metro.ticketing.repository.StationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Tag(name = "Routes", description = "Manage routes and their ordered stops.")
@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteRepository routeRepository;
    private final RouteStationRepository routeStationRepository;
    private final StationRepository stationRepository;

    public RouteController(RouteRepository routeRepository,
                           RouteStationRepository routeStationRepository,
                           StationRepository stationRepository) {
        this.routeRepository = routeRepository;
        this.routeStationRepository = routeStationRepository;
        this.stationRepository = stationRepository;
    }

    @Operation(summary = "Create a route", description = "Creates a route with start/end stations and an optional ordered stop list. Admin only.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Route> createRoute(@Valid @RequestBody Route request) {
        Route route = new Route();
        route.setRouteId("RT-" + UUID.randomUUID());
        copyRouteFields(request, route);
        route.setCreatedAt(LocalDateTime.now());
        route.setUpdatedAt(LocalDateTime.now());
        route = routeRepository.save(route);

        saveRouteStations(route, request.getStationIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(loadRoute(route.getRouteId()));
    }

    @Operation(summary = "Update a route", description = "Updates route details and replaces its ordered stops. Admin only.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Route> updateRoute(@PathVariable String id, @Valid @RequestBody Route request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));

        copyRouteFields(request, route);
        route.setUpdatedAt(LocalDateTime.now());
        route = routeRepository.save(route);

        routeStationRepository.deleteByRoute_RouteId(id);
        saveRouteStations(route, request.getStationIds());

        return ResponseEntity.ok(loadRoute(id));
    }

    @Operation(summary = "Get a route", description = "Returns a route with its ordered station IDs.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Route> getRouteById(@PathVariable String id) {
        return ResponseEntity.ok(loadRoute(id));
    }

    @Operation(summary = "List all routes", description = "Returns every route with its ordered station IDs.")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Route>> getAllRoutes() {
        List<Route> routes = routeRepository.findAll();
        routes.forEach(route -> route.setStationIds(loadStationIds(route.getRouteId())));
        return ResponseEntity.ok(routes);
    }

    @Operation(summary = "Delete a route", description = "Deletes a route and its stops. Admin only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteRoute(@PathVariable String id) {
        if (!routeRepository.existsById(id)) {
            throw new IllegalArgumentException("Route not found");
        }
        routeStationRepository.deleteByRoute_RouteId(id);
        routeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Route loadRoute(String routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));
        route.setStationIds(loadStationIds(routeId));
        return route;
    }

    private List<String> loadStationIds(String routeId) {
        List<String> stationIds = new ArrayList<>();
        for (RouteStation routeStation : routeStationRepository.findByRoute_RouteIdOrderBySequenceNumberAsc(routeId)) {
            stationIds.add(routeStation.getStation().getStationId());
        }
        return stationIds;
    }

    private void copyRouteFields(Route request, Route route) {
        route.setRouteName(request.getRouteName());
        route.setTotalDistance(request.getTotalDistance());
        route.setEstimatedTime(request.getEstimatedTime());
        route.setLineColor(request.getLineColor());

        if (request.getStartStationId() == null || request.getEndStationId() == null) {
            throw new IllegalArgumentException("Start and end stations are required");
        }

        Station start = stationRepository.findById(request.getStartStationId())
                .orElseThrow(() -> new IllegalArgumentException("Start station not found"));
        Station end = stationRepository.findById(request.getEndStationId())
                .orElseThrow(() -> new IllegalArgumentException("End station not found"));

        route.setStartStation(start);
        route.setEndStation(end);
    }

    private void saveRouteStations(Route route, List<String> stationIds) {
        List<String> orderedStations = stationIds;
        if (orderedStations == null || orderedStations.isEmpty()) {
            orderedStations = List.of(
                    route.getStartStation().getStationId(),
                    route.getEndStation().getStationId());
        }

        int sequenceNumber = 1;
        for (String stationId : orderedStations) {
            Station station = stationRepository.findById(stationId)
                    .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));

            RouteStation routeStation = new RouteStation();
            routeStation.setRouteStationId("RS-" + UUID.randomUUID());
            routeStation.setRoute(route);
            routeStation.setStation(station);
            routeStation.setSequenceNumber(sequenceNumber++);
            routeStationRepository.save(routeStation);
        }
    }
}
