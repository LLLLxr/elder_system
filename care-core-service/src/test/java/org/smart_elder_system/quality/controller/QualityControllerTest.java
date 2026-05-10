package org.smart_elder_system.quality.controller;

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
import org.smart_elder_system.common.dto.quality.CaregiverQualificationApplicationDto;
import org.smart_elder_system.common.dto.quality.CaregiverQualificationReviewDto;
import org.smart_elder_system.exception.GlobalExceptionHandler;
import org.smart_elder_system.quality.QualityAuthorizationException;
import org.smart_elder_system.quality.service.QualityService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QualityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private QualityService qualityService;

    @InjectMocks
    private QualityController qualityController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(qualityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void shouldSubmitCaregiverQualificationApplication() throws Exception {
        CaregiverQualificationApplicationDto response = qualification(21L, "PENDING", null, null);
        when(qualityService.submitCaregiverQualificationApplication(any(CaregiverQualificationApplicationDto.class))).thenReturn(response);

        mockMvc.perform(post("/quality/caregiver-qualification-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"realName\": \"护理员甲\",
                                  \"phone\": \"13800000000\",
                                  \"idCardNo\": \"110101199001010011\",
                                  \"certificateNo\": \"CERT-001\",
                                  \"certificateType\": \"护士执业证\",
                                  \"yearsOfExperience\": 5,
                                  \"skillSummary\": \"擅长失能老人护理\"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(21))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.caregiverUsername").value("caregiver1"));

        verify(qualityService).submitCaregiverQualificationApplication(any(CaregiverQualificationApplicationDto.class));
    }

    @Test
    void shouldRejectCaregiverQualificationApplicationWithoutRequiredFields() throws Exception {
        mockMvc.perform(post("/quality/caregiver-qualification-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数验证失败"))
                .andExpect(jsonPath("$.data.realName").exists())
                .andExpect(jsonPath("$.data.phone").exists())
                .andExpect(jsonPath("$.data.idCardNo").exists())
                .andExpect(jsonPath("$.data.certificateNo").exists())
                .andExpect(jsonPath("$.data.certificateType").exists())
                .andExpect(jsonPath("$.data.skillSummary").exists());
    }

    @Test
    void shouldListMyCaregiverQualificationApplications() throws Exception {
        when(qualityService.listMyCaregiverQualificationApplications()).thenReturn(List.of(
                qualification(21L, "PENDING", null, null),
                qualification(20L, "REJECTED", "medic1", "资料不完整")
        ));

        mockMvc.perform(get("/quality/caregiver-qualification-applications/my").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(21))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].applicationId").value(20))
                .andExpect(jsonPath("$[1].status").value("REJECTED"));

        verify(qualityService).listMyCaregiverQualificationApplications();
    }

    @Test
    void shouldListCaregiverQualificationApplicationsByStatus() throws Exception {
        when(qualityService.listCaregiverQualificationApplications("APPROVED"))
                .thenReturn(List.of(qualification(22L, "APPROVED", "medic1", "资质齐全")));

        mockMvc.perform(get("/quality/caregiver-qualification-applications")
                        .param("status", "APPROVED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(22))
                .andExpect(jsonPath("$[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[0].reviewedBy").value("medic1"));

        verify(qualityService).listCaregiverQualificationApplications("APPROVED");
    }

    @Test
    void shouldGetCaregiverQualificationApplicationDetail() throws Exception {
        when(qualityService.getCaregiverQualificationApplicationDetail(23L))
                .thenReturn(qualification(23L, "PENDING", null, null));

        mockMvc.perform(get("/quality/caregiver-qualification-applications/23").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(23))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(qualityService).getCaregiverQualificationApplicationDetail(23L);
    }

    @Test
    void shouldApproveCaregiverQualificationApplication() throws Exception {
        CaregiverQualificationApplicationDto response = qualification(24L, "APPROVED", "medic1", "资质齐全");
        when(qualityService.approveCaregiverQualificationApplication(eq(24L), any(CaregiverQualificationReviewDto.class))).thenReturn(response);

        mockMvc.perform(post("/quality/caregiver-qualification-applications/24/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"reviewComment\": \"资质齐全\"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(24))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewedBy").value("medic1"))
                .andExpect(jsonPath("$.reviewComment").value("资质齐全"));

        verify(qualityService).approveCaregiverQualificationApplication(eq(24L), any(CaregiverQualificationReviewDto.class));
    }

    @Test
    void shouldRejectCaregiverQualificationApplication() throws Exception {
        CaregiverQualificationApplicationDto response = qualification(25L, "REJECTED", "medic1", "资料不完整");
        when(qualityService.rejectCaregiverQualificationApplication(eq(25L), any(CaregiverQualificationReviewDto.class))).thenReturn(response);

        mockMvc.perform(post("/quality/caregiver-qualification-applications/25/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"reviewComment\": \"资料不完整\"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(25))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.reviewedBy").value("medic1"))
                .andExpect(jsonPath("$.reviewComment").value("资料不完整"));

        verify(qualityService).rejectCaregiverQualificationApplication(eq(25L), any(CaregiverQualificationReviewDto.class));
    }

    @Test
    void shouldReturnForbiddenWhenQualityAuthorizationFails() throws Exception {
        when(qualityService.listCaregiverQualificationApplications("PENDING"))
                .thenThrow(new QualityAuthorizationException("当前用户无权执行资质审核操作: quality:caregiver-qualification:list"));

        mockMvc.perform(get("/quality/caregiver-qualification-applications")
                        .param("status", "PENDING")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("当前用户无权执行资质审核操作: quality:caregiver-qualification:list"));
    }

    private CaregiverQualificationApplicationDto qualification(Long applicationId, String status, String reviewedBy, String reviewComment) {
        CaregiverQualificationApplicationDto dto = new CaregiverQualificationApplicationDto();
        dto.setApplicationId(applicationId);
        dto.setCaregiverUserId(2001L);
        dto.setCaregiverUsername("caregiver1");
        dto.setRealName("护理员甲");
        dto.setPhone("13800000000");
        dto.setIdCardNo("110101199001010011");
        dto.setCertificateNo("CERT-001");
        dto.setCertificateType("护士执业证");
        dto.setYearsOfExperience(5);
        dto.setSkillSummary("擅长失能老人护理");
        dto.setStatus(status);
        dto.setReviewedBy(reviewedBy);
        dto.setReviewComment(reviewComment);
        return dto;
    }
}
