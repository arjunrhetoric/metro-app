package com.metro.ticketing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.metro.ticketing.entity.Payment;
import com.metro.ticketing.entity.Ticket;
import com.metro.ticketing.entity.User;
import com.metro.ticketing.repository.PaymentRepository;
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

@Tag(name = "Payments", description = "Record and view payments for tickets.")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public PaymentController(PaymentRepository paymentRepository,
                             TicketRepository ticketRepository,
                             UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Create a payment", description = "Records payment for a ticket. The passenger must own the ticket and the amount must match the fare.")
    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT')")
    @Transactional
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody Payment request, Authentication authentication) {
        if (request.getTicketId() == null) {
            throw new IllegalArgumentException("Ticket ID is required");
        }
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (isPassenger(authentication)
                && !ticket.getPassenger().getUserId().equals(currentUser(authentication).getUserId())) {
            throw new AccessDeniedException("You can only make payments for your own tickets");
        }

        if (ticket.getFare() != null && request.getAmount().compareTo(ticket.getFare().getBaseFare()) != 0) {
            throw new IllegalArgumentException("Payment amount does not match the ticket fare");
        }

        Payment payment = new Payment();
        payment.setPaymentId("PAY-" + UUID.randomUUID());
        payment.setTicket(ticket);
        payment.setAmount(request.getAmount());
        payment.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentRepository.save(payment));
    }

    @Operation(summary = "Get a payment", description = "Returns one payment. Passengers may only read payments for their own tickets.")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id, Authentication authentication) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        checkOwnership(payment, authentication);
        return ResponseEntity.ok(payment);
    }

    @Operation(summary = "List all payments", description = "Returns every payment. Admin and ticket agents only.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_AGENT')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }

    @Operation(summary = "List payments for a ticket", description = "Returns payments for one ticket. Passengers may only view their own.")
    @GetMapping("/ticket/{ticketId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Payment>> getPaymentsByTicket(@PathVariable String ticketId,
                                                             Authentication authentication) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (isPassenger(authentication)
                && !ticket.getPassenger().getUserId().equals(currentUser(authentication).getUserId())) {
            throw new AccessDeniedException("You can only view payments for your own tickets");
        }

        return ResponseEntity.ok(paymentRepository.findByTicket_TicketId(ticketId));
    }

    @Operation(summary = "Delete a payment", description = "Deletes a payment by ID. Admin only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        if (!paymentRepository.existsById(id)) {
            throw new IllegalArgumentException("Payment not found");
        }
        paymentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void checkOwnership(Payment payment, Authentication authentication) {
        if (isPassenger(authentication)
                && !payment.getTicket().getPassenger().getUserId().equals(currentUser(authentication).getUserId())) {
            throw new AccessDeniedException("You can only access payments for your own tickets");
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
