package com.ing.mortgage.service;

import com.ing.mortgage.dto.*;
import com.ing.mortgage.model.*;
import com.ing.mortgage.repository.*;
import com.ing.mortgage.exception.MortgageNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MortgageService {

    private final MortgageRepository mortgageRepository;
    private final PaymentRepository paymentRepository;

    public MortgageService(MortgageRepository mortgageRepository, PaymentRepository paymentRepository) {
        this.mortgageRepository = mortgageRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse recordPayment(Long mortgageId, PaymentRequest request) {
        Mortgage mortgage = mortgageRepository.findById(mortgageId)
                .orElseThrow(() -> new MortgageNotFoundException("Mortgage not found"));

        BigDecimal currentPrincipal = mortgage.getRemainingPrincipal();
        if (currentPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Mortgage is already fully paid off.");
        }

        // Calculate Interest for the month
        BigDecimal monthlyInterestRate = mortgage.getInterestRate()
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        
        BigDecimal interestPaid = currentPrincipal.multiply(monthlyInterestRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPayment = request.amount();
        BigDecimal principalPaid = totalPayment.subtract(interestPaid);

        // Edge Case Handling: Overpayment validation
        if (principalPaid.compareTo(currentPrincipal) > 0) {
            principalPaid = currentPrincipal;
            totalPayment = interestPaid.add(principalPaid);
        }

        BigDecimal newPrincipal = currentPrincipal.subtract(principalPaid);
        mortgage.setRemainingPrincipal(newPrincipal);
        mortgageRepository.save(mortgage);

        Payment payment = new Payment();
        payment.setMortgageId(mortgageId);
        payment.setPaymentDate(request.paymentDate());
        payment.setTotalPayment(totalPayment);
        payment.setInterestPaid(interestPaid);
        payment.setPrincipalPaid(principalPaid);
        payment.setRemainingPrincipal(newPrincipal);
        paymentRepository.save(payment);

        return new PaymentResponse(newPrincipal);
    }

    public List<PaymentBreakdownDto> getPaymentBreakdown(Long mortgageId) {
        if (!mortgageRepository.existsById(mortgageId)) {
            throw new MortgageNotFoundException("Mortgage not found");
        }
        return paymentRepository.findByMortgageIdOrderByPaymentDateAsc(mortgageId).stream()
                .map(p -> new PaymentBreakdownDto(p.getPaymentDate(), p.getTotalPayment(), p.getPrincipalPaid(), p.getInterestPaid(), p.getRemainingPrincipal()))
                .toList();
    }

    public List<ProjectionDto> projectFutureBalances(Long mortgageId, int months) {
        Mortgage mortgage = mortgageRepository.findById(mortgageId)
                .orElseThrow(() -> new MortgageNotFoundException("Mortgage not found"));

        List<ProjectionDto> projections = new ArrayList<>();
        BigDecimal runningPrincipal = mortgage.getRemainingPrincipal();
        
        BigDecimal monthlyInterestRate = mortgage.getInterestRate()
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        LocalDate startDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 1; i <= months; i++) {
            if (runningPrincipal.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal interestPaid = runningPrincipal.multiply(monthlyInterestRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalPayment = mortgage.getMonthlyPayment();
            BigDecimal principalPaid = totalPayment.subtract(interestPaid);

            if (principalPaid.compareTo(runningPrincipal) > 0) {
                principalPaid = runningPrincipal;
                totalPayment = interestPaid.add(principalPaid);
            }

            runningPrincipal = runningPrincipal.subtract(principalPaid);
            String monthLabel = startDate.plusMonths(i).format(formatter);

            projections.add(new ProjectionDto(monthLabel, totalPayment, principalPaid, interestPaid, runningPrincipal));
        }

        return projections;
    }
}
