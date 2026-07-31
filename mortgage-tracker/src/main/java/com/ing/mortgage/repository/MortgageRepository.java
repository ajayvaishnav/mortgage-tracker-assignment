package com.ing.mortgage.repository;

import com.ing.mortgage.model.Mortgage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MortgageRepository extends JpaRepository<Mortgage, Long> {
}
