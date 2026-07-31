package com.ing.mortgage.dto;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(LocalDate paymentDate, BigDecimal amount) {}
