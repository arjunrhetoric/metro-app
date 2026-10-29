package com.metro.ticketing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.metro.ticketing.entity.Train;
import com.metro.ticketing.repository.TrainRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Trains", description = "Manage the metro train fleet.")
@RestController
@RequestMapping("/api/trains")
public class TrainController {

    private final TrainRepository trainRepository;

    public TrainController(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    @Operation(summary = "Create a train", description = "Adds a train to the fleet. Admin only.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Train> createTrain(@Valid @RequestBody Train request) {
        Train train = new Train();
        train.setTrainId("TRN-" + UUID.randomUUID());
        copyTrainFields(request, train);
        return ResponseEntity.status(HttpStatus.CREATED).body(trainRepository.save(train));
    }

    @Operation(summary = "Update a train", description = "Updates train details. Admin only.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Train> updateTrain(@PathVariable String id, @Valid @RequestBody Train request) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Train not found"));
        copyTrainFields(request, train);
        return ResponseEntity.ok(trainRepository.save(train));
    }

    @Operation(summary = "Get a train", description = "Returns one train by ID.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Train> getTrainById(@PathVariable String id) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Train not found"));
        return ResponseEntity.ok(train);
    }

    @Operation(summary = "List all trains", description = "Returns the complete fleet.")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Train>> getAllTrains() {
        return ResponseEntity.ok(trainRepository.findAll());
    }

    @Operation(summary = "Delete a train", description = "Deletes a train if no schedules or issues reference it. Admin only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteTrain(@PathVariable String id) {
        if (!trainRepository.existsById(id)) {
            throw new IllegalArgumentException("Train not found");
        }
        trainRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void copyTrainFields(Train request, Train train) {
        train.setTrainNumber(request.getTrainNumber());
        train.setCapacity(request.getCapacity());
        train.setTotalCoaches(request.getTotalCoaches());
        train.setManufactureYear(request.getManufactureYear());
        if (request.getIsActive() != null) {
            train.setIsActive(request.getIsActive());
        }
    }
}
