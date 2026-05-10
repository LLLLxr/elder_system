package org.smart_elder_system.admission.controller;

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
import org.smart_elder_system.admission.AdmissionAuthorizationException;
import org.smart_elder_system.admission.service.AdmissionService;
import org.smart_elder_system.common.dto.admission.FamilyVisitReservationDto;
import org.smart_elder_system.common.dto.admission.FamilyVisitReservationRuleDto;
import org.smart_elder_system.common.dto.admission.FamilyVisitSlotDto;
import org.smart_elder_system.exception.GlobalExceptionHandler;

import java.time.LocalDate;
import java.time.LocalTime;
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
class AdmissionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdmissionService admissionService;

    @InjectMocks
    private AdmissionController admissionController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(admissionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void shouldReturnFamilyVisitReservationRules() throws Exception {
        when(admissionService.getFamilyVisitReservationRules()).thenReturn(new FamilyVisitReservationRuleDto(
                1,
                7,
                true,
                "08:00",
                "17:00",
                60,
                List.of("12:00-13:00"),
                List.of(1, 2, 3, 4, 5)
        ));

        mockMvc.perform(get("/admission/family-visit-reservation-rules").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minAdvanceDays").value(1))
                .andExpect(jsonPath("$.maxWorkingDaysAhead").value(7))
                .andExpect(jsonPath("$.bookingStartTime").value("08:00"))
                .andExpect(jsonPath("$.excludedTimeRanges[0]").value("12:00-13:00"));

        verify(admissionService).getFamilyVisitReservationRules();
    }

    @Test
    void shouldReturnFamilyVisitSlots() throws Exception {
        when(admissionService.listFamilyVisitSlots(LocalDate.of(2026, 5, 8))).thenReturn(List.of(slot(11L)));

        mockMvc.perform(get("/admission/family-visit-slots")
                        .param("slotDate", "2026-05-08")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slotId").value(11))
                .andExpect(jsonPath("$[0].slotDate[0]").value(2026))
                .andExpect(jsonPath("$[0].slotDate[1]").value(5))
                .andExpect(jsonPath("$[0].slotDate[2]").value(8))
                .andExpect(jsonPath("$[0].status").value("OPEN"));

        verify(admissionService).listFamilyVisitSlots(LocalDate.of(2026, 5, 8));
    }

    @Test
    void shouldCreateFamilyVisitReservation() throws Exception {
        when(admissionService.createFamilyVisitReservation(any(FamilyVisitReservationDto.class)))
                .thenReturn(reservation(21L, "PENDING", null, null));

        mockMvc.perform(post("/admission/family-visit-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slotId": 11,
                                  "elderId": 1001,
                                  "visitorName": "张三",
                                  "visitorPhone": "13800000000",
                                  "relationToElder": "儿子",
                                  "visitPurpose": "探望"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(21))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(admissionService).createFamilyVisitReservation(any(FamilyVisitReservationDto.class));
    }

    @Test
    void shouldReturnConflictWhenReservationViolatesRules() throws Exception {
        when(admissionService.createFamilyVisitReservation(any(FamilyVisitReservationDto.class)))
                .thenThrow(new IllegalStateException("当前时段不符合预约规则"));

        mockMvc.perform(post("/admission/family-visit-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slotId": 11,
                                  "elderId": 1001,
                                  "visitorName": "张三",
                                  "visitorPhone": "13800000000",
                                  "relationToElder": "儿子",
                                  "visitPurpose": "探望"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("当前时段不符合预约规则"));
    }

    @Test
    void shouldListMyFamilyVisitReservations() throws Exception {
        when(admissionService.listMyFamilyVisitReservations()).thenReturn(List.of(
                reservation(21L, "PENDING", null, null),
                reservation(20L, "APPROVED", "medic1", "同意预约")
        ));

        mockMvc.perform(get("/admission/family-visit-reservations/my").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(21))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].reservationId").value(20))
                .andExpect(jsonPath("$[1].status").value("APPROVED"));

        verify(admissionService).listMyFamilyVisitReservations();
    }

    @Test
    void shouldListFamilyVisitReservationsByStatus() throws Exception {
        when(admissionService.listFamilyVisitReservations("APPROVED"))
                .thenReturn(List.of(reservation(22L, "APPROVED", "medic1", "同意预约")));

        mockMvc.perform(get("/admission/family-visit-reservations")
                        .param("status", "APPROVED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(22))
                .andExpect(jsonPath("$[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[0].reviewedBy").value("medic1"));

        verify(admissionService).listFamilyVisitReservations("APPROVED");
    }

    @Test
    void shouldGetFamilyVisitReservationDetail() throws Exception {
        when(admissionService.getFamilyVisitReservationDetail(23L))
                .thenReturn(reservation(23L, "PENDING", null, null));

        mockMvc.perform(get("/admission/family-visit-reservations/23").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(23))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(admissionService).getFamilyVisitReservationDetail(23L);
    }

    @Test
    void shouldApproveFamilyVisitReservation() throws Exception {
        when(admissionService.approveFamilyVisitReservation(eq(24L), any()))
                .thenReturn(reservation(24L, "APPROVED", "medic1", "同意预约"));

        mockMvc.perform(post("/admission/family-visit-reservations/24/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reviewComment": "同意预约"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(24))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewedBy").value("medic1"))
                .andExpect(jsonPath("$.reviewComment").value("同意预约"));

        verify(admissionService).approveFamilyVisitReservation(eq(24L), any());
    }

    @Test
    void shouldRejectFamilyVisitReservation() throws Exception {
        when(admissionService.rejectFamilyVisitReservation(eq(25L), any()))
                .thenReturn(reservation(25L, "REJECTED", "medic1", "暂不方便接待"));

        mockMvc.perform(post("/admission/family-visit-reservations/25/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reviewComment": "暂不方便接待"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(25))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.reviewedBy").value("medic1"))
                .andExpect(jsonPath("$.reviewComment").value("暂不方便接待"));

        verify(admissionService).rejectFamilyVisitReservation(eq(25L), any());
    }

    @Test
    void shouldReturnForbiddenWhenAdmissionAuthorizationFails() throws Exception {
        when(admissionService.listFamilyVisitReservations("PENDING"))
                .thenThrow(new AdmissionAuthorizationException("当前用户无权执行预约参观操作: admission:family-visit-reservation:list"));

        mockMvc.perform(get("/admission/family-visit-reservations")
                        .param("status", "PENDING")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("当前用户无权执行预约参观操作: admission:family-visit-reservation:list"));
    }

    private FamilyVisitSlotDto slot(Long slotId) {
        FamilyVisitSlotDto dto = new FamilyVisitSlotDto();
        dto.setSlotId(slotId);
        dto.setSlotDate(LocalDate.of(2026, 5, 8));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setCapacity(3);
        dto.setReservedCount(1);
        dto.setStatus("OPEN");
        return dto;
    }

    private FamilyVisitReservationDto reservation(Long reservationId, String status, String reviewedBy, String reviewComment) {
        FamilyVisitReservationDto dto = new FamilyVisitReservationDto();
        dto.setReservationId(reservationId);
        dto.setSlotId(11L);
        dto.setElderId(1001L);
        dto.setFamilyUserId(3001L);
        dto.setFamilyUsername("family1");
        dto.setVisitorName("张三");
        dto.setVisitorPhone("13800000000");
        dto.setRelationToElder("儿子");
        dto.setVisitPurpose("探望");
        dto.setStatus(status);
        dto.setReviewedBy(reviewedBy);
        dto.setReviewComment(reviewComment);
        dto.setSlotDate(LocalDate.of(2026, 5, 8));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        return dto;
    }
}
