package org.smart_elder_system.elder.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.common.dto.elder.ElderProfileDto;
import org.smart_elder_system.elder.service.ElderProfileService;
import org.smart_elder_system.exception.GlobalExceptionHandler;
import org.smart_elder_system.exception.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ElderProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ElderProfileService elderProfileService;

    @InjectMocks
    private ElderProfileController elderProfileController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(elderProfileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void shouldReturnNotFoundWhenElderProfileDoesNotExistByIdCard() throws Exception {
        when(elderProfileService.getByIdCard("110101195001010025"))
                .thenThrow(new ResourceNotFoundException("未找到老人档案"));

        mockMvc.perform(get("/elders")
                        .param("idCard", "110101195001010025")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("未找到老人档案"));
    }

    @Test
    void shouldCreateOrUpdateElderProfile() throws Exception {
        ElderProfileDto response = new ElderProfileDto();
        response.setElderId(1L);
        response.setElderName("张三");
        response.setIdCard("110101195001010025");
        response.setPhone("13800138010");
        response.setGender("男");
        response.setBirthDate(LocalDate.of(1950, 1, 1));
        response.setStatus("ACTIVE");

        when(elderProfileService.createIfAbsent(org.mockito.ArgumentMatchers.any(ElderProfileDto.class))).thenReturn(response);

        mockMvc.perform(post("/elders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"elderName\": \"张三\",
                                  \"idCard\": \"110101195001010025\",
                                  \"phone\": \"13800138010\",
                                  \"gender\": \"男\",
                                  \"birthDate\": \"1950-01-01\",
                                  \"status\": \"ACTIVE\"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderId").value(1))
                .andExpect(jsonPath("$.elderName").value("张三"))
                .andExpect(jsonPath("$.idCard").value("110101195001010025"));
    }
}
