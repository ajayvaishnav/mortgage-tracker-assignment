package com.ing.mortgage.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ing.mortgage.dto.PaymentBreakdownDto;
import com.ing.mortgage.dto.PaymentRequest;
import com.ing.mortgage.dto.PaymentResponse;
import com.ing.mortgage.dto.ProjectionDto;
import com.ing.mortgage.service.MortgageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/mortgages")
@Tag(name = "Mortgage Management", description = "Endpoints for recording payments and calculating historical/future projections.")
public class MortgageController {

    private final MortgageService mortgageService;

    public MortgageController(MortgageService mortgageService) {
        this.mortgageService = mortgageService;
    }

    @Operation(summary = "Record a Mortgage Payment", description = "Processes a client payment, deducts the calculated interest, reduces the remaining principal, and logs the history.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment processed successfully", 
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Mortgage ID not found", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid payment input or mortgage already paid off", content = @Content)
    })
    @PostMapping("/{mortgageId}/payments")
    public ResponseEntity<PaymentResponse> recordPayment(@PathVariable Long mortgageId, @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(mortgageService.recordPayment(mortgageId, request));
    }

    @Operation(summary = "View Payment Breakdown", description = "Retrieves a chronological historical statement list showing principal, interest paid, and remaining balance for every payment made.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment records retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Mortgage ID not found", content = @Content)
    })
    @GetMapping("/{mortgageId}/payments")
    public ResponseEntity<List<PaymentBreakdownDto>> getPaymentBreakdown(@PathVariable Long mortgageId) {
        return ResponseEntity.ok(mortgageService.getPaymentBreakdown(mortgageId));
    }

    @Operation(summary = "Project Future Balances", description = "Calculates an iterative monthly financial amortization forecast for a specified number of upcoming months.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Future projection generated successfully"),
        @ApiResponse(responseCode = "404", description = "Mortgage ID not found", content = @Content)
    })
    @GetMapping("/{mortgageId}/projection")
    public ResponseEntity<List<ProjectionDto>> projectFutureBalances(@PathVariable Long mortgageId, @RequestParam int months) {
        return ResponseEntity.ok(mortgageService.projectFutureBalances(mortgageId, months));
    }
}