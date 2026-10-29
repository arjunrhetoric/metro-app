package com.metro.ticketing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.metro.ticketing.entity.Station;
import com.metro.ticketing.repository.StationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Stations", description = "Create, read, update and delete metro stations.")
@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationRepository stationRepository;

    public StationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Operation(summary = "Create a station", description = "Adds a new metro station. Admin only.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Station> createStation(@Valid @RequestBody Station request) {
        Station station = new Station();
        station.setStationId("STN-" + UUID.randomUUID());
        copyStationFields(request, station);
        return ResponseEntity.status(HttpStatus.CREATED).body(stationRepository.save(station));
    }

    @Operation(summary = "Update a station", description = "Updates station details. Admin only.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Station> updateStation(@PathVariable String id, @Valid @RequestBody Station request) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));
        copyStationFields(request, station);
        return ResponseEntity.ok(stationRepository.save(station));
    }

    @Operation(summary = "Get a station", description = "Returns one station by ID.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Station> getStationById(@PathVariable String id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));
        return ResponseEntity.ok(station);
    }

    @Operation(summary = "List all stations", description = "Returns all metro stations.")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Station>> getAllStations() {
        return ResponseEntity.ok(stationRepository.findAll());
    }

    @Operation(summary = "Delete a station", description = "Deletes a station if no other records reference it. Admin only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteStation(@PathVariable String id) {
        if (!stationRepository.existsById(id)) {
            throw new IllegalArgumentException("Station not found");
        }
        stationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void copyStationFields(Station request, Station station) {
        station.setStationCode(request.getStationCode());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setAddress(request.getAddress());
        station.setOpenedDate(request.getOpenedDate());
        station.setLineColor(request.getLineColor());
        if (request.getIsActive() != null) {
            station.setIsActive(request.getIsActive());
        }
    }
}
