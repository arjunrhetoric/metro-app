package com.metro.ticketing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.metro.ticketing.entity.Fare;
import com.metro.ticketing.entity.Station;
import com.metro.ticketing.entity.Ticket;
import com.metro.ticketing.entity.User;
import com.metro.ticketing.repository.FareRepository;
import com.metro.ticketing.repository.StationRepository;
import com.metro.ticketing.repository.TicketRepository;
import com.metro.ticketing.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Tickets", description = "Book, view, validate and delete tickets.")
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final FareRepository fareRepository;

    public TicketController(TicketRepository ticketRepository,
                            UserRepository userRepository,
                            StationRepository stationRepository,
                            FareRepository fareRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.fareRepository = fareRepository;
    }

    @Operation(summary = "Book a ticket", description = "Creates a ticket for the authenticated passenger or a supplied passenger. Looks up the fare and defaults validity to 24 hours.")
    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT', 'ADMIN')")
    @Transactional
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket request, Authentication authentication) {
        if (request.getSourceStationId() == null || request.getDestStationId() == null) {
            throw new IllegalArgumentException("Source and destination stations are required");
        }
        if (request.getSourceStationId().equals(request.getDestStationId())) {
            throw new IllegalArgumentException("Source and destination stations must be different");
        }

        String passengerId = request.getPassengerId();
        if (isPassenger(authentication)) {
            passengerId = currentUser(authentication).getUserId();
        }
        if (passengerId == null || passengerId.isBlank()) {
            throw new IllegalArgumentException("Passenger ID is required");
        }

        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found"));
        Station source = stationRepository.findById(request.getSourceStationId())
                .orElseThrow(() -> new IllegalArgumentException("Source station not found"));
        Station destination = stationRepository.findById(request.getDestStationId())
                .orElseThrow(() -> new IllegalArgumentException("Destination station not found"));

        Fare fare = fareRepository.findBySourceStation_StationIdAndDestStation_StationId(
                        request.getSourceStationId(), request.getDestStationId())
                .orElseThrow(() -> new IllegalArgumentException("Fare not found for this station pair"));

        Ticket ticket = new Ticket();
        ticket.setTicketId("TKT-" + UUID.randomUUID());
        ticket.setPassenger(passenger);
        ticket.setFare(fare);
        ticket.setSourceStation(source);
        ticket.setDestStation(destination);
        ticket.setTicketType(request.getTicketType());
        ticket.setIssueTime(LocalDateTime.now());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setIsUsed(false);
        ticket.setValidUntil(request.getValidUntil() != null
                ? request.getValidUntil() : LocalDateTime.now().plusHours(24));

        return ResponseEntity.status(HttpStatus.CREATED).body(ticketRepository.save(ticket));
    }

    @Operation(summary = "Get a ticket", description = "Returns one ticket. Passengers may only read their own tickets.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Ticket> getTicketById(@PathVariable String id, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        checkOwnership(ticket, authentication);
        return ResponseEntity.ok(ticket);
    }

    @Operation(summary = "List all tickets", description = "Returns every ticket. Admin and ticket agents only.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_AGENT')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketRepository.findAll());
    }

    @Operation(summary = "List a passenger's tickets", description = "Returns tickets for a passenger. Passengers are always limited to their own tickets.")
    @GetMapping("/passenger/{passengerId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Ticket>> getTicketsByPassenger(@PathVariable String passengerId,
                                                              Authentication authentication) {
        if (isPassenger(authentication)) {
            passengerId = currentUser(authentication).getUserId();
        }
        return ResponseEntity.ok(ticketRepository.findByPassenger_UserId(passengerId));
    }

    @Operation(summary = "Use a ticket", description = "Marks a ticket as used after checking ownership, used status and expiry.")
    @PatchMapping("/{id}/use")
    @PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT', 'ADMIN')")
    @Transactional
    public ResponseEntity<Ticket> markTicketAsUsed(@PathVariable String id, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        checkOwnership(ticket, authentication);

        if (Boolean.TRUE.equals(ticket.getIsUsed())) {
            throw new IllegalArgumentException("Ticket already used");
        }
        if (ticket.getValidUntil() != null && ticket.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ticket has expired");
        }

        ticket.setIsUsed(true);
        return ResponseEntity.ok(ticketRepository.save(ticket));
    }

    @Operation(summary = "Delete a ticket", description = "Deletes a ticket if no payment references it. Admin only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        if (!ticketRepository.existsById(id)) {
            throw new IllegalArgumentException("Ticket not found");
        }
        ticketRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void checkOwnership(Ticket ticket, Authentication authentication) {
        if (isPassenger(authentication)
                && !ticket.getPassenger().getUserId().equals(currentUser(authentication).getUserId())) {
            throw new AccessDeniedException("You can only access your own tickets");
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
