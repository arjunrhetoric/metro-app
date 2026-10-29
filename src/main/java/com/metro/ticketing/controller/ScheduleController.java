package com.metro.ticketing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.metro.ticketing.entity.Route;
import com.metro.ticketing.entity.Schedule;
import com.metro.ticketing.entity.Train;
import com.metro.ticketing.repository.RouteRepository;
import com.metro.ticketing.repository.ScheduleRepository;
import com.metro.ticketing.repository.TrainRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Schedules", description = "Manage timetables and detect overlapping trips.")
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;
    private final RouteRepository routeRepository;
    private final TrainRepository trainRepository;

    public ScheduleController(ScheduleRepository scheduleRepository,
                              RouteRepository routeRepository,
                              TrainRepository trainRepository) {
        this.scheduleRepository = scheduleRepository;
        this.routeRepository = routeRepository;
        this.trainRepository = trainRepository;
    }

    @Operation(summary = "Create a schedule", description = "Creates a timetable entry and rejects overlapping times for the same train. Admin only.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Schedule> createSchedule(@Valid @RequestBody Schedule request) {
        validateTimes(request);
        if (scheduleRepository.hasOverlappingSchedule(
                request.getTrainId(), request.getScheduledDeparture(), request.getScheduledArrival(), null)) {
            throw new IllegalArgumentException("Train is already scheduled during this time period");
        }

        Schedule schedule = new Schedule();
        schedule.setScheduleId("SCH-" + UUID.randomUUID());
        copyScheduleFields(request, schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleRepository.save(schedule));
    }

    @Operation(summary = "Update a schedule", description = "Updates a schedule and rechecks train overlap. Admin only.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Schedule> updateSchedule(@PathVariable String id, @Valid @RequestBody Schedule request) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        validateTimes(request);
        if (scheduleRepository.hasOverlappingSchedule(
                request.getTrainId(), request.getScheduledDeparture(), request.getScheduledArrival(), id)) {
            throw new IllegalArgumentException("Train is already scheduled during this time period");
        }

        copyScheduleFields(request, schedule);
        return ResponseEntity.ok(scheduleRepository.save(schedule));
    }

    @Operation(summary = "Get a schedule", description = "Returns one schedule by ID.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Schedule> getScheduleById(@PathVariable String id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        return ResponseEntity.ok(schedule);
    }

    @Operation(summary = "List all schedules", description = "Returns every timetable entry.")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleRepository.findAll());
    }

    @Operation(summary = "Delete a schedule", description = "Deletes a schedule by ID. Admin only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteSchedule(@PathVariable String id) {
        if (!scheduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Schedule not found");
        }
        scheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void validateTimes(Schedule request) {
        if (request.getScheduledArrival().isBefore(request.getScheduledDeparture())) {
            throw new IllegalArgumentException("Arrival time cannot be before departure time");
        }
    }

    private void copyScheduleFields(Schedule request, Schedule schedule) {
        if (request.getRouteId() == null || request.getTrainId() == null) {
            throw new IllegalArgumentException("Train and route are required");
        }
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));
        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new IllegalArgumentException("Train not found"));

        schedule.setRoute(route);
        schedule.setTrain(train);
        schedule.setScheduledDeparture(request.getScheduledDeparture());
        schedule.setScheduledArrival(request.getScheduledArrival());
        schedule.setDayOfWeek(request.getDayOfWeek());
        if (request.getIsActive() != null) {
            schedule.setIsActive(request.getIsActive());
        }
    }
}
