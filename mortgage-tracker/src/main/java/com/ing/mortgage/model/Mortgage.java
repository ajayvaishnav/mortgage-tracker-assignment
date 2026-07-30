package com.ing.mortgage.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "mortgages")
public class Mortgage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal initialPrincipal;
    private BigDecimal interestRate; // Annual percentage (e.g., 6.00)
    private BigDecimal monthlyPayment;
    private BigDecimal remainingPrincipal;

    // Getters, Setters, Constructors
    public Mortgage() {}

    public Mortgage(BigDecimal initialPrincipal, BigDecimal interestRate, BigDecimal monthlyPayment) {
        this.initialPrincipal = initialPrincipal;
        this.interestRate = interestRate;
        this.monthlyPayment = monthlyPayment;
        this.remainingPrincipal = initialPrincipal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getInitialPrincipal() { return initialPrincipal; }
    public void setInitialPrincipal(BigDecimal initialPrincipal) { this.initialPrincipal = initialPrincipal; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment = monthlyPayment; }
    public BigDecimal getRemainingPrincipal() { return remainingPrincipal; }
    public void setRemainingPrincipal(BigDecimal remainingPrincipal) { this.remainingPrincipal = remainingPrincipal; }
}
