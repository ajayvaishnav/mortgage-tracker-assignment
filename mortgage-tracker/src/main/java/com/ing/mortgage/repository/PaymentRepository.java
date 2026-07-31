package com.ing.mortgage.repository;
import com.ing.mortgage.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByMortgageIdOrderByPaymentDateAsc(Long mortgageId);
}
