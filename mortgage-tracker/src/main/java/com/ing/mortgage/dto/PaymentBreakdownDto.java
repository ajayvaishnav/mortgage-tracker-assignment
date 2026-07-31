package com.ing.mortgage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentBreakdownDto(
	    LocalDate paymentDate, 
	    BigDecimal totalPayment, 
	    BigDecimal principalPaid, 
	    BigDecimal interestPaid, 
	    BigDecimal remainingPrincipal
	) {}
