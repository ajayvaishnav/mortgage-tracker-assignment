-- Insert a standard mortgage matching the prompt example: €10,000 baseline, 6% interest rate, €300 monthly payment
INSERT INTO mortgages (initial_principal, interest_rate, monthly_payment, remaining_principal) 
VALUES (10000.00, 6.00, 300.00, 10000.00);

-- Insert a secondary mortgage to allow testing for non-existent vs existing assets (e.g., ID 2)
INSERT INTO mortgages (initial_principal, interest_rate, monthly_payment, remaining_principal) 
VALUES (250000.00, 4.50, 1500.00, 250000.00);

-- Pre-populate one historical payment for Mortgage ID 1 to verify the payment breakdown API instantly
-- Formula tracking: Interest = 10000 * (6/12/100) = 50.00. Principal Paid = 300 - 50 = 250. Remaining = 9750.
INSERT INTO payments (mortgage_id, payment_date, total_payment, principal_paid, interest_paid, remaining_principal)
VALUES (1, '2026-07-15', 300.00, 250.00, 50.00, 9750.00);

-- Sync the remaining principal on Mortgage ID 1 to match the payment made above
UPDATE mortgages SET remaining_principal = 9750.00 WHERE id = 1;
