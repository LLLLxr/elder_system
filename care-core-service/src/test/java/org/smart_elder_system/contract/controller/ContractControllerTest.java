package org.smart_elder_system.contract.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.smart_elder_system.common.dto.contract.ServiceAgreementDto;
import org.smart_elder_system.contract.service.ContractService;
import org.smart_elder_system.exception.GlobalExceptionHandler;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContractControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContractService contractService;

    @InjectMocks
    private ContractController contractController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(contractController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void shouldGetAgreementById() throws Exception {
        ServiceAgreementDto response = ServiceAgreementDto.builder()
                .agreementId(3001L)
                .applicationId(10001L)
                .elderId(1001L)
                .serviceScene("HOME")
                .status("ACTIVE")
                .effectiveDate(LocalDate.of(2026, 5, 1))
                .expiryDate(LocalDate.of(2027, 5, 1))
                .signedBy("签约员")
                .build();

        when(contractService.getAgreement(3001L)).thenReturn(response);

        mockMvc.perform(get("/contract/agreements/3001").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agreementId").value(3001))
                .andExpect(jsonPath("$.applicationId").value(10001))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.signedBy").value("签约员"));

        verify(contractService).getAgreement(3001L);
    }

    @Test
    void shouldGetLatestAgreementByApplicationId() throws Exception {
        ServiceAgreementDto response = ServiceAgreementDto.builder()
                .agreementId(3001L)
                .applicationId(10001L)
                .elderId(1001L)
                .serviceScene("HOME")
                .status("DRAFT")
                .build();

        when(contractService.getLatestAgreementByApplicationId(10001L)).thenReturn(response);

        mockMvc.perform(get("/contract/agreements/by-application/10001").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agreementId").value(3001))
                .andExpect(jsonPath("$.applicationId").value(10001))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(contractService).getLatestAgreementByApplicationId(10001L);
    }
}
