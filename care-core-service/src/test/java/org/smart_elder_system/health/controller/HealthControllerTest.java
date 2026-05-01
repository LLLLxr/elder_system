package org.smart_elder_system.health.controller;

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
import org.smart_elder_system.common.dto.care.HealthCheckFormCreateRequestDTO;
import org.smart_elder_system.common.dto.care.HealthCheckFormDTO;
import org.smart_elder_system.exception.GlobalExceptionHandler;
import org.smart_elder_system.health.service.HealthService;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HealthService healthService;

    @InjectMocks
    private HealthController healthController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(healthController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void shouldRejectUserCreateHealthCheckForm() throws Exception {
        mockMvc.perform(post("/health/check-forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("健康体检表需由后台专业人员填写"));
    }

    @Test
    void shouldCreateAdminHealthCheckForm() throws Exception {
        HealthCheckFormDTO response = healthCheckForm(
                20L,
                1001L,
                3001L,
                2001L,
                "张三",
                LocalDate.of(2026, 4, 26),
                "李医生",
                "PAPER_V1"
        );

        when(healthService.createAdminHealthCheckForm(any(HealthCheckFormCreateRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/health/admin/check-forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"elderId\": 1001,
                                  \"agreementId\": 3001,
                                  \"elderName\": \"张三\",
                                  \"checkDate\": \"2026-04-26\",
                                  \"responsibleDoctor\": \"李医生\",
                                  \"chronicDiseaseSummary\": \"高血压\",
                                  \"allergySummary\": \"无\"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value(20))
                .andExpect(jsonPath("$.elderId").value(1001))
                .andExpect(jsonPath("$.agreementId").value(3001))
                .andExpect(jsonPath("$.authorUserId").value(2001))
                .andExpect(jsonPath("$.responsibleDoctor").value("李医生"))
                .andExpect(jsonPath("$.formVersion").value("PAPER_V1"));

        verify(healthService).createAdminHealthCheckForm(any(HealthCheckFormCreateRequestDTO.class));
    }

    @Test
    void shouldRejectAdminHealthCheckFormWithoutRequiredFields() throws Exception {
        mockMvc.perform(post("/health/admin/check-forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"elderId\": 1001,
                                  \"agreementId\": 3001
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数验证失败"))
                .andExpect(jsonPath("$.data.elderName").exists());
    }

    @Test
    void shouldGetLatestHealthCheckFormWithoutAuthorUserId() throws Exception {
        HealthCheckFormDTO response = healthCheckForm(
                21L,
                1001L,
                3001L,
                null,
                "张三",
                LocalDate.of(2026, 4, 26),
                "李医生",
                "PAPER_V1"
        );

        when(healthService.getLatestHealthCheckForm(1001L, 3001L)).thenReturn(response);

        mockMvc.perform(get("/health/check-forms/latest")
                        .param("elderId", "1001")
                        .param("agreementId", "3001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value(21))
                .andExpect(jsonPath("$.elderId").value(1001))
                .andExpect(jsonPath("$.agreementId").value(3001));

        verify(healthService).getLatestHealthCheckForm(1001L, 3001L);
    }

    @Test
    void shouldIgnoreAuthorUserIdOnLatestHealthCheckFormRequest() throws Exception {
        HealthCheckFormDTO response = healthCheckForm(
                22L,
                1001L,
                3001L,
                null,
                "张三",
                null,
                null,
                null
        );

        when(healthService.getLatestHealthCheckForm(1001L, 3001L)).thenReturn(response);

        mockMvc.perform(get("/health/check-forms/latest")
                        .param("elderId", "1001")
                        .param("agreementId", "3001")
                        .param("authorUserId", "2001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formId").value(22))
                .andExpect(jsonPath("$.elderId").value(1001))
                .andExpect(jsonPath("$.agreementId").value(3001));

        verify(healthService).getLatestHealthCheckForm(1001L, 3001L);
    }

    private HealthCheckFormDTO healthCheckForm(
            Long formId,
            Long elderId,
            Long agreementId,
            Long authorUserId,
            String elderName,
            LocalDate checkDate,
            String responsibleDoctor,
            String formVersion
    ) throws Exception {
        HealthCheckFormDTO response = new HealthCheckFormDTO();
        setField(response, "formId", formId);
        setField(response, "elderId", elderId);
        setField(response, "agreementId", agreementId);
        setField(response, "authorUserId", authorUserId);
        setField(response, "elderName", elderName);
        setField(response, "checkDate", checkDate);
        setField(response, "responsibleDoctor", responsibleDoctor);
        setField(response, "formVersion", formVersion);
        return response;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
