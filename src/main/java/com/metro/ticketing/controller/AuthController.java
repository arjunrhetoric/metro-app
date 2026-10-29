package com.metro.ticketing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.metro.ticketing.entity.Role;
import com.metro.ticketing.entity.Station;
import com.metro.ticketing.entity.User;
import com.metro.ticketing.repository.StationRepository;
import com.metro.ticketing.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Authentication", description = "Passenger registration, employee hiring and current-user details.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          StationRepository stationRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Register a passenger", description = "Creates a passenger account with the PASSENGER role. Public endpoint; the password is BCrypt-hashed.")
    @PostMapping("/passenger/register")
    @Transactional
    @SecurityRequirements
    public ResponseEntity<User> registerPassenger(@Valid @RequestBody User request) {
        if (userRepository.findByContact(request.getContact()).isPresent()) {
            throw new IllegalArgumentException("User with this contact already exists.");
        }

        User user = new User();
        user.setUserId("PASS-" + UUID.randomUUID());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setContact(request.getContact());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PASSENGER);
        user.setRegistrationDate(LocalDate.now());
        user.setIsActive(true);

        return ResponseEntity.ok(userRepository.save(user));
    }

    @Operation(summary = "Hire an employee", description = "Creates a staff account and assigns it to a station. Admin only.")
    @PostMapping("/employee/hire")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<User> hireEmployee(@Valid @RequestBody User request) {
        if (userRepository.findByContact(request.getContact()).isPresent()) {
            throw new IllegalArgumentException("User with this contact already exists.");
        }
        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role is required.");
        }
        if (request.getStationId() == null) {
            throw new IllegalArgumentException("Station is required.");
        }

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Station not found."));

        User user = new User();
        user.setUserId("EMP-" + UUID.randomUUID());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setContact(request.getContact());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setHireDate(LocalDate.now());
        user.setRegistrationDate(LocalDate.now());
        user.setStation(station);
        user.setIsActive(true);

        return ResponseEntity.ok(userRepository.save(user));
    }

    @Operation(summary = "Get current user", description = "Returns the user represented by the supplied Basic authentication credentials.")
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByContact(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        return ResponseEntity.ok(user);
    }
}
