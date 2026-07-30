package com.ing.mortgage.dto;

import java.math.BigDecimal;

public record ProjectionDto(
    String month, 
    BigDecimal totalPayment, 
    BigDecimal principalPaid, 
    BigDecimal interestPaid, 
    BigDecimal remainingPrincipal
) {}