package com.metro.ticketing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.metro.ticketing.entity.MaintenanceIssue;
import com.metro.ticketing.entity.Station;
import com.metro.ticketing.entity.Train;
import com.metro.ticketing.entity.User;
import com.metro.ticketing.repository.MaintenanceIssueRepository;
import com.metro.ticketing.repository.StationRepository;
import com.metro.ticketing.repository.TrainRepository;
import com.metro.ticketing.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Maintenance", description = "Report, update, filter and resolve maintenance issues.")
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceIssueController {

    private final MaintenanceIssueRepository maintenanceIssueRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;

    public MaintenanceIssueController(MaintenanceIssueRepository maintenanceIssueRepository,
                                      TrainRepository trainRepository,
                                      StationRepository stationRepository,
                                      UserRepository userRepository) {
        this.maintenanceIssueRepository = maintenanceIssueRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Report an issue", description = "Creates a maintenance issue for a train, a station, or both, with priority and description.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_STAFF', 'TICKET_AGENT')")
    @Transactional
    public ResponseEntity<MaintenanceIssue> createMaintenanceIssue(@Valid @RequestBody MaintenanceIssue request) {
        MaintenanceIssue issue = new MaintenanceIssue();
        issue.setIssueId("ISS-" + UUID.randomUUID());
        issue.setIssueType(request.getIssueType());
        issue.setDescription(request.getDescription());
        issue.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        issue.setStatus("Open");
        issue.setCreatedAt(LocalDateTime.now());
        applyRelations(request, issue);

        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceIssueRepository.save(issue));
    }

    @Operation(summary = "Update an issue", description = "Updates issue details, status, priority and optional links. Admin and maintenance staff only.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<MaintenanceIssue> updateMaintenanceIssue(@PathVariable String id,
                                                                   @Valid @RequestBody MaintenanceIssue request) {
        MaintenanceIssue issue = maintenanceIssueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance issue not found"));

        issue.setIssueType(request.getIssueType());
        issue.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }
        applyRelations(request, issue);

        return ResponseEntity.ok(maintenanceIssueRepository.save(issue));
    }

    @Operation(summary = "Get an issue", description = "Returns one maintenance issue by ID.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<MaintenanceIssue> getMaintenanceIssueById(@PathVariable String id) {
        MaintenanceIssue issue = maintenanceIssueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance issue not found"));
        return ResponseEntity.ok(issue);
    }

    @Operation(summary = "List all issues", description = "Returns every maintenance issue. Admin and maintenance staff only.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_STAFF')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MaintenanceIssue>> getAllMaintenanceIssues() {
        return ResponseEntity.ok(maintenanceIssueRepository.findAll());
    }

    @Operation(summary = "List issues for a train", description = "Returns maintenance issues linked to a train.")
    @GetMapping("/train/{trainId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MaintenanceIssue>> getIssuesByTrain(@PathVariable String trainId) {
        return ResponseEntity.ok(maintenanceIssueRepository.findByTrain_TrainId(trainId));
    }

    @Operation(summary = "List issues for a station", description = "Returns maintenance issues linked to a station.")
    @GetMapping("/station/{stationId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MaintenanceIssue>> getIssuesByStation(@PathVariable String stationId) {
        return ResponseEntity.ok(maintenanceIssueRepository.findByStation_StationId(stationId));
    }

    @Operation(summary = "Resolve an issue", description = "Marks an open issue Closed and records the resolution time. Admin and maintenance staff only.")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_STAFF')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MaintenanceIssue>> getIssuesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(maintenanceIssueRepository.findByStatus(status));
    }

    @Operation(summary = "Resolve an issue", description = "Marks an open issue Closed and records the resolution time. Admin and maintenance staff only.")
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_STAFF')")
    @Transactional
    public ResponseEntity<MaintenanceIssue> resolveMaintenanceIssue(@PathVariable String id) {
        MaintenanceIssue issue = maintenanceIssueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance issue not found"));

        if ("Closed".equalsIgnoreCase(issue.getStatus())) {
            throw new IllegalArgumentException("Maintenance issue is already closed");
        }

        issue.setStatus("Closed");
        issue.setResolvedAt(LocalDateTime.now());
        return ResponseEntity.ok(maintenanceIssueRepository.save(issue));
    }

    @Operation(summary = "Delete an issue", description = "Deletes a maintenance issue by ID. Admin only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteMaintenanceIssue(@PathVariable String id) {
        if (!maintenanceIssueRepository.existsById(id)) {
            throw new IllegalArgumentException("Maintenance issue not found");
        }
        maintenanceIssueRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyRelations(MaintenanceIssue request, MaintenanceIssue issue) {
        if (request.getTrainId() != null) {
            Train train = trainRepository.findById(request.getTrainId())
                    .orElseThrow(() -> new IllegalArgumentException("Train not found"));
            issue.setTrain(train);
        }
        if (request.getStationId() != null) {
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new IllegalArgumentException("Station not found"));
            issue.setStation(station);
        }
        if (request.getReportedById() != null) {
            User reporter = userRepository.findById(request.getReportedById())
                    .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));
            issue.setReportedBy(reporter);
        }
    }
}
