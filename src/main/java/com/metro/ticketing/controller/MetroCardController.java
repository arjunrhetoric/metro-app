package com.metro.ticketing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.metro.ticketing.entity.MetroCard;
import com.metro.ticketing.entity.User;
import com.metro.ticketing.repository.MetroCardRepository;
import com.metro.ticketing.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Metro Cards", description = "Issue, top up, deactivate and manage metro cards.")
@RestController
@RequestMapping("/api/cards")
public class MetroCardController {

    private final MetroCardRepository metroCardRepository;
    private final UserRepository userRepository;

    public MetroCardController(MetroCardRepository metroCardRepository, UserRepository userRepository) {
        this.metroCardRepository = metroCardRepository;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Issue a metro card", description = "Issues a card. A passenger may only issue their own and may have one active card.")
    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT', 'ADMIN')")
    @Transactional
    public ResponseEntity<MetroCard> createMetroCard(@Valid @RequestBody MetroCard request,
                                                     Authentication authentication) {
        String passengerId = request.getPassengerId();
        if (isPassenger(authentication)) {
            passengerId = currentUser(authentication).getUserId();
        }
        if (passengerId == null || passengerId.isBlank()) {
            throw new IllegalArgumentException("Passenger ID is required");
        }

        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found"));

        if (metroCardRepository.findByPassenger_UserIdAndIsActiveTrue(passengerId).isPresent()) {
            throw new IllegalArgumentException("Passenger already has an active metro card");
        }

        MetroCard card = new MetroCard();
        card.setCardId("CARD-" + UUID.randomUUID());
        card.setPassenger(passenger);
        card.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        card.setIssueDate(LocalDate.now());
        card.setCreatedAt(LocalDateTime.now());
        card.setIsActive(true);
        card.setExpiryDate(request.getExpiryDate() != null
                ? request.getExpiryDate() : LocalDate.now().plusYears(5));

        return ResponseEntity.status(HttpStatus.CREATED).body(metroCardRepository.save(card));
    }

    @Operation(summary = "Top up a metro card", description = "Adds an amount to a valid, active card. Passengers may only top up their own cards.")
    @PostMapping("/{id}/topup")
    @PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT', 'ADMIN')")
    @Transactional
    public ResponseEntity<MetroCard> topUpCard(@PathVariable String id,
                                               @RequestParam BigDecimal amount,
                                               Authentication authentication) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be greater than zero");
        }

        MetroCard card = metroCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Metro card not found"));
        checkOwnership(card, authentication);

        if (!Boolean.TRUE.equals(card.getIsActive())) {
            throw new IllegalArgumentException("Metro card is inactive");
        }
        if (card.getExpiryDate() != null && card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Metro card has expired");
        }

        card.setBalance(card.getBalance().add(amount));
        return ResponseEntity.ok(metroCardRepository.save(card));
    }

    @Operation(summary = "Get a metro card", description = "Returns one card. Passengers may only read their own cards.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<MetroCard> getMetroCardById(@PathVariable String id, Authentication authentication) {
        MetroCard card = metroCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Metro card not found"));
        checkOwnership(card, authentication);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "List all metro cards", description = "Returns every card. Admin and ticket agents only.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_AGENT')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MetroCard>> getAllMetroCards() {
        return ResponseEntity.ok(metroCardRepository.findAll());
    }

    @Operation(summary = "List a passenger's cards", description = "Returns cards for a passenger. Passengers are limited to their own cards.")
    @GetMapping("/passenger/{passengerId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MetroCard>> getMetroCardsByPassenger(@PathVariable String passengerId,
                                                                    Authentication authentication) {
        if (isPassenger(authentication)) {
            passengerId = currentUser(authentication).getUserId();
        }
        return ResponseEntity.ok(metroCardRepository.findByPassenger_UserId(passengerId));
    }

    @Operation(summary = "Deactivate a metro card", description = "Marks a card inactive. Admin and ticket agents only.")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_AGENT')")
    @Transactional
    public ResponseEntity<MetroCard> deactivateMetroCard(@PathVariable String id) {
        MetroCard card = metroCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Metro card not found"));
        card.setIsActive(false);
        return ResponseEntity.ok(metroCardRepository.save(card));
    }

    @Operation(summary = "Delete a metro card", description = "Deletes a card by ID. Admin only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteMetroCard(@PathVariable String id) {
        if (!metroCardRepository.existsById(id)) {
            throw new IllegalArgumentException("Metro card not found");
        }
        metroCardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void checkOwnership(MetroCard card, Authentication authentication) {
        if (isPassenger(authentication)
                && !card.getPassenger().getUserId().equals(currentUser(authentication).getUserId())) {
            throw new AccessDeniedException("You can only access your own metro cards");
        }
    }

    private boolean isPassenger(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PASSENGER"));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByContact(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
    }
}
