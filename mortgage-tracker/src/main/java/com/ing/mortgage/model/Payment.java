package com.ing.mortgage.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long mortgageId;
    private LocalDate paymentDate;
    private BigDecimal totalPayment;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal remainingPrincipal;

    // Getters, Setters, Constructors
    public Payment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMortgageId() { return mortgageId; }
    public void setMortgageId(Long mortgageId) { this.mortgageId = mortgageId; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public BigDecimal getTotalPayment() { return totalPayment; }
    public void setTotalPayment(BigDecimal totalPayment) { this.totalPayment = totalPayment; }
    public BigDecimal getPrincipalPaid() { return principalPaid; }
    public void setPrincipalPaid(BigDecimal principalPaid) { this.principalPaid = principalPaid; }
    public BigDecimal getInterestPaid() { return interestPaid; }
    public void setInterestPaid(BigDecimal interestPaid) { this.interestPaid = interestPaid; }
    public BigDecimal getRemainingPrincipal() { return remainingPrincipal; }
    public void setRemainingPrincipal(BigDecimal remainingPrincipal) { this.remainingPrincipal = remainingPrincipal; }
}