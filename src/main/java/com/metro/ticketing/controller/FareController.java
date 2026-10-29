package com.metro.ticketing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.metro.ticketing.entity.Fare;
import com.metro.ticketing.entity.Station;
import com.metro.ticketing.repository.FareRepository;
import com.metro.ticketing.repository.StationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Fares", description = "Manage the station-to-station fare matrix.")
@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareRepository fareRepository;
    private final StationRepository stationRepository;

    public FareController(FareRepository fareRepository, StationRepository stationRepository) {
        this.fareRepository = fareRepository;
        this.stationRepository = stationRepository;
    }

    @Operation(summary = "Create a fare", description = "Creates a directed fare for a station pair. Admin only.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Fare> createFare(@Valid @RequestBody Fare request) {
        validateStationPair(request.getSourceStationId(), request.getDestStationId());

        if (fareRepository.existsBySourceStation_StationIdAndDestStation_StationId(
                request.getSourceStationId(), request.getDestStationId())) {
            throw new IllegalArgumentException("A fare for this station pair already exists");
        }

        Fare fare = new Fare();
        fare.setFareId("FARE-" + UUID.randomUUID());
        copyFareFields(request, fare);
        return ResponseEntity.status(HttpStatus.CREATED).body(fareRepository.save(fare));
    }

    @Operation(summary = "Look up a fare", description = "Returns the fare for a source and destination station.")
    @GetMapping("/lookup")
    @Transactional(readOnly = true)
    public ResponseEntity<Fare> getFareByStations(@RequestParam String sourceStationId,
                                                  @RequestParam String destStationId) {
        validateStationPair(sourceStationId, destStationId);
        Fare fare = fareRepository.findBySourceStation_StationIdAndDestStation_StationId(
                        sourceStationId, destStationId)
                .orElseThrow(() -> new IllegalArgumentException("Fare not found for this station pair"));
        return ResponseEntity.ok(fare);
    }

    @Operation(summary = "Get a fare", description = "Returns one fare by ID.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Fare> getFareById(@PathVariable String id) {
        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fare not found"));
        return ResponseEntity.ok(fare);
    }

    @Operation(summary = "List all fares", description = "Returns the complete fare matrix.")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Fare>> getAllFares() {
        return ResponseEntity.ok(fareRepository.findAll());
    }

    @Operation(summary = "Update a fare", description = "Updates a fare and checks station-pair uniqueness. Admin only.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Fare> updateFare(@PathVariable String id, @Valid @RequestBody Fare request) {
        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fare not found"));

        validateStationPair(request.getSourceStationId(), request.getDestStationId());

        if (fareRepository.existsBySourceStation_StationIdAndDestStation_StationIdAndFareIdNot(
                request.getSourceStationId(), request.getDestStationId(), id)) {
            throw new IllegalArgumentException("A fare for this station pair already exists");
        }

        copyFareFields(request, fare);
        return ResponseEntity.ok(fareRepository.save(fare));
    }

    @Operation(summary = "Delete a fare", description = "Deletes a fare if no tickets reference it. Admin only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteFare(@PathVariable String id) {
        if (!fareRepository.existsById(id)) {
            throw new IllegalArgumentException("Fare not found");
        }
        fareRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void validateStationPair(String sourceStationId, String destStationId) {
        if (sourceStationId == null || destStationId == null) {
            throw new IllegalArgumentException("Source and destination stations are required");
        }
        if (sourceStationId.equals(destStationId)) {
            throw new IllegalArgumentException("Source and destination stations must be different");
        }
    }

    private void copyFareFields(Fare request, Fare fare) {
        Station source = stationRepository.findById(request.getSourceStationId())
                .orElseThrow(() -> new IllegalArgumentException("Source station not found"));
        Station destination = stationRepository.findById(request.getDestStationId())
                .orElseThrow(() -> new IllegalArgumentException("Destination station not found"));

        fare.setSourceStation(source);
        fare.setDestStation(destination);
        fare.setBaseFare(request.getBaseFare());
    }
}
