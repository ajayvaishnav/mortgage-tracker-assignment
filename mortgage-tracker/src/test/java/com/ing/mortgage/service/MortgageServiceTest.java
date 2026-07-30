package com.ing.mortgage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ing.mortgage.dto.PaymentBreakdownDto;
import com.ing.mortgage.dto.PaymentRequest;
import com.ing.mortgage.dto.PaymentResponse;
import com.ing.mortgage.dto.ProjectionDto;
import com.ing.mortgage.exception.MortgageNotFoundException;
import com.ing.mortgage.model.Mortgage;
import com.ing.mortgage.model.Payment;
import com.ing.mortgage.repository.MortgageRepository;
import com.ing.mortgage.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class MortgageServiceTest {

    @Mock
    private MortgageRepository mortgageRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private MortgageService mortgageService;

    private Mortgage testMortgage;

    @BeforeEach
    void setUp() {
        // Initializing mock mortgage matching the prompt example: €10,000 baseline, 6% interest rate
        testMortgage = new Mortgage(
                BigDecimal.valueOf(10000.00), 
                BigDecimal.valueOf(6.00), 
                BigDecimal.valueOf(300.00)
        );
        testMortgage.setId(1L);
    }

    @Test
    @DisplayName("Record Payment: Should calculate interest and principal reductions correctly")
    void testRecordPayment_Success() {
        // Given
        PaymentRequest request = new PaymentRequest(LocalDate.of(2024, 1, 15), BigDecimal.valueOf(300.00));
        when(mortgageRepository.findById(1L)).thenReturn(Optional.of(testMortgage));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = mortgageService.recordPayment(1L, request);

        // Then
        // Interest: 10000 * (6 / 12 / 100) = 50.00
        // Principal Paid: 300.00 - 50.00 = 250.00
        // New Balance: 10000.00 - 250.00 = 9750.00
        assertEquals(new BigDecimal("9750.00"), response.remainingPrincipal());
        assertEquals(new BigDecimal("9750.00"), testMortgage.getRemainingPrincipal());

        // Verify entity storage state
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertTrue(new BigDecimal("300.00").compareTo(savedPayment.getTotalPayment()) == 0);
        assertEquals(new BigDecimal("50.00"), savedPayment.getInterestPaid());
        assertEquals(new BigDecimal("250.00"), savedPayment.getPrincipalPaid());
        assertEquals(new BigDecimal("9750.00"), savedPayment.getRemainingPrincipal());
    }

    @Test
    @DisplayName("Record Payment: Should cap payment and principal when payment exceeds remaining principal balance")
    void testRecordPayment_OverpaymentEdgeCase() {
        // Given: Principal remaining is very small
        testMortgage.setRemainingPrincipal(BigDecimal.valueOf(100.00));
        // Overpaying with €300.00 instead of exact balance + interest
        PaymentRequest request = new PaymentRequest(LocalDate.of(2024, 2, 15), BigDecimal.valueOf(300.00));
        
        when(mortgageRepository.findById(1L)).thenReturn(Optional.of(testMortgage));

        // When
        PaymentResponse response = mortgageService.recordPayment(1L, request);

        // Then
        // Interest: 100.00 * (6 / 12 / 100) = 0.50
        // Cap PrincipalPaid to remaining (100.00) instead of raw subtract math
        // Final adjusted totalPayment: 100.00 + 0.50 = 100.50
        assertTrue(BigDecimal.ZERO.compareTo(response.remainingPrincipal()) == 0);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment savedPayment = paymentCaptor.getValue();
        assertTrue(new BigDecimal("100.50").compareTo(savedPayment.getTotalPayment()) == 0, "Total payment mismatch");
        assertTrue(new BigDecimal("100.00").compareTo(savedPayment.getPrincipalPaid()) == 0, "Principal paid mismatch");
        assertTrue(new BigDecimal("0.50").compareTo(savedPayment.getInterestPaid()) == 0, "Interest paid mismatch");;
    }

    @Test
    @DisplayName("Record Payment: Should throw exception if mortgage ID does not exist")
    void testRecordPayment_NotFound() {
        // Given
        PaymentRequest request = new PaymentRequest(LocalDate.now(), BigDecimal.valueOf(100.00));
        when(mortgageRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MortgageNotFoundException.class, () -> mortgageService.recordPayment(99L, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Record Payment: Should throw exception if loan is already paid off")
    void testRecordPayment_AlreadyPaidOff() {
        // Given
        testMortgage.setRemainingPrincipal(BigDecimal.ZERO);
        PaymentRequest request = new PaymentRequest(LocalDate.now(), BigDecimal.valueOf(100.00));
        when(mortgageRepository.findById(1L)).thenReturn(Optional.of(testMortgage));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> mortgageService.recordPayment(1L, request));
    }

    @Test
    @DisplayName("View Breakdown: Should return history ordered chronologically")
    void testGetPaymentBreakdown_Success() {
        // Given
        Payment paymentRecord = new Payment();
        paymentRecord.setPaymentDate(LocalDate.of(2024, 1, 15));
        paymentRecord.setTotalPayment(BigDecimal.valueOf(300.00));
        paymentRecord.setPrincipalPaid(BigDecimal.valueOf(250.00));
        paymentRecord.setInterestPaid(BigDecimal.valueOf(50.00));
        paymentRecord.setRemainingPrincipal(BigDecimal.valueOf(9750.00));

        when(mortgageRepository.existsById(1L)).thenReturn(true);
        when(paymentRepository.findByMortgageIdOrderByPaymentDateAsc(1L)).thenReturn(List.of(paymentRecord));

        // When
        List<PaymentBreakdownDto> breakdown = mortgageService.getPaymentBreakdown(1L);

        // Then
        assertFalse(breakdown.isEmpty());
        assertEquals(1, breakdown.size());
        assertTrue(new BigDecimal("9750.00").compareTo(breakdown.get(0).remainingPrincipal()) == 0);
    }

    @Test
    @DisplayName("Future Projection: Should compute simulated breakdown metrics sequentially without writing to DB")
    void testProjectFutureBalances_Success() {
        // Given
        when(mortgageRepository.findById(1L)).thenReturn(Optional.of(testMortgage));

        // When
        List<ProjectionDto> projections = mortgageService.projectFutureBalances(1L, 2);

        // Then
        assertEquals(2, projections.size());

        // Month 1 projection checking
        // Int: 10000 * 0.005 = 50.00 | Princ: 300 - 50 = 250 | Rem: 9750.00
        ProjectionDto m1 = projections.get(0);
        assertEquals(new BigDecimal("50.00"), m1.interestPaid());
        assertEquals(new BigDecimal("250.00"), m1.principalPaid());
        assertEquals(new BigDecimal("9750.00"), m1.remainingPrincipal());

        // Month 2 projection checking
        // Int: 9750 * 0.005 = 48.75 | Princ: 300 - 48.75 = 251.25 | Rem: 9750 - 251.25 = 9498.75
        ProjectionDto m2 = projections.get(1);
        assertEquals(new BigDecimal("48.75"), m2.interestPaid());
        assertEquals(new BigDecimal("251.25"), m2.principalPaid());
        assertEquals(new BigDecimal("9498.75"), m2.remainingPrincipal());

        // Verify zero modifications are persisted down into the database records
        verify(mortgageRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }
}
