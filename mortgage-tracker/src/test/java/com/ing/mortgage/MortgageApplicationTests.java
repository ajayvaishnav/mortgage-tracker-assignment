package com.ing.mortgage;

import com.ing.mortgage.dto.PaymentRequest;
import com.ing.mortgage.model.Mortgage;
import com.ing.mortgage.repository.MortgageRepository;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MortgageApplicationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private MortgageRepository mortgageRepository;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void testEndToEndMortgageFlow() throws Exception {
        Mortgage mortgage = new Mortgage(BigDecimal.valueOf(10000.00), BigDecimal.valueOf(6.00), BigDecimal.valueOf(300.00));
        mortgage = mortgageRepository.save(mortgage);

        PaymentRequest request = new PaymentRequest(LocalDate.of(2024, 1, 15), BigDecimal.valueOf(300.00));

        mockMvc.perform(post("/mortgages/" + mortgage.getId() + "/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingPrincipal").value(9750.00));

        mockMvc.perform(get("/mortgages/" + mortgage.getId() + "/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].interestPaid").value(50.00))
                .andExpect(jsonPath("$[0].principalPaid").value(250.00));
    }
}
