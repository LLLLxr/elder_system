package org.smart_elder_system.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.smart_elder_system.common.dto.elder.ElderProfileDto;
import org.smart_elder_system.iam.IamServiceApplication;
import org.smart_elder_system.user.constant.UserConstants;
import org.smart_elder_system.user.feign.CareCoreElderProfileClient;
import org.smart_elder_system.user.po.UserElderBindingPo;
import org.smart_elder_system.user.po.UserElderBindingRequestPo;
import org.smart_elder_system.user.repository.UserElderBindingRepository;
import org.smart_elder_system.user.repository.UserElderBindingRequestRepository;
import org.smart_elder_system.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = IamServiceApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.profiles.active=h2",
        "app.redis.enabled=false",
        "spring.sql.init.mode=always"
})
class ElderBindingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserElderBindingRequestRepository requestRepository;

    @Autowired
    private UserElderBindingRepository bindingRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private CareCoreElderProfileClient careCoreElderProfileClient;

    @Test
    @Transactional
    void shouldSubmitFamilyBindingRequest() throws Exception {
        String responseBody = mockMvc.perform(post("/api/users/me/elder-binding-requests/family")
                        .with(user("family1").authorities(new SimpleGrantedAuthority("elder-binding:request:create")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "elderName": "张三",
                                  "elderIdCard": "110101195001010025",
                                  "elderPhone": "13800138010",
                                  "relationToElder": "儿子"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").isNumber())
                .andExpect(jsonPath("$.status").value(UserConstants.ELDER_BINDING_REQUEST_STATUS_PENDING))
                .andExpect(jsonPath("$.elderName").value("张三"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long requestId = jsonNode.get("requestId").asLong();
        UserElderBindingRequestPo savedRequest = requestRepository.findById(requestId).orElseThrow();

        assertEquals(4L, ReflectionTestUtils.getField(savedRequest, "applicantUserId"));
        assertEquals("family1", ReflectionTestUtils.getField(savedRequest, "createdBy"));
        assertEquals("family1", ReflectionTestUtils.getField(savedRequest, "lastModifiedBy"));
        assertNotNull(ReflectionTestUtils.getField(savedRequest, "createdDateTimeUtc"));
        assertNotNull(ReflectionTestUtils.getField(savedRequest, "lastModifiedDateTimeUtc"));
    }

    @Test
    @Transactional
    void shouldApproveFamilyBindingRequestAndExposeBindingToFamilyUser() throws Exception {
        ElderProfileDto elderProfile = new ElderProfileDto();
        elderProfile.setElderId(5001L);
        elderProfile.setElderName("张三");
        elderProfile.setIdCard("110101195001010025");
        elderProfile.setPhone("13800138010");
        elderProfile.setGender("MALE");
        elderProfile.setBirthDate(LocalDate.of(1950, 1, 1));
        elderProfile.setStatus("ACTIVE");

        when(careCoreElderProfileClient.getByIdCard("110101195001010025")).thenReturn(elderProfile);
        when(careCoreElderProfileClient.getByElderId(5001L)).thenReturn(elderProfile);
        when(careCoreElderProfileClient.createIfAbsent(any(ElderProfileDto.class))).thenReturn(elderProfile);

        String createResponseBody = mockMvc.perform(post("/api/users/me/elder-binding-requests/family")
                        .with(user("family1").authorities(new SimpleGrantedAuthority("elder-binding:request:create")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "elderName": "张三",
                                  "elderIdCard": "110101195001010025",
                                  "elderPhone": "13800138010",
                                  "relationToElder": "儿子"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        Long requestId = objectMapper.readTree(createResponseBody).get("requestId").asLong();

        mockMvc.perform(post("/api/users/elder-binding-requests/{requestId}/approve", requestId)
                        .with(user("medic1").authorities(new SimpleGrantedAuthority("elder-binding:request:approve")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reviewComment": "资料核验通过"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.status").value(UserConstants.ELDER_BINDING_REQUEST_STATUS_APPROVED))
                .andExpect(jsonPath("$.elderId").value(5001));

        UserElderBindingRequestPo request = requestRepository.findById(requestId).orElseThrow();
        assertEquals(UserConstants.ELDER_BINDING_REQUEST_STATUS_APPROVED, ReflectionTestUtils.getField(request, "status"));
        assertEquals(5001L, ReflectionTestUtils.getField(request, "elderId"));
        assertEquals("medic1", ReflectionTestUtils.getField(request, "reviewedBy"));
        assertEquals("资料核验通过", ReflectionTestUtils.getField(request, "reviewComment"));
        assertNotNull(ReflectionTestUtils.getField(request, "reviewedAt"));

        UserElderBindingPo binding = bindingRepository
                .findByUserIdAndElderIdAndBindingType(4L, 5001L, UserConstants.ELDER_BINDING_TYPE_FAMILY)
                .orElseThrow();
        assertEquals("儿子", ReflectionTestUtils.getField(binding, "relationToElder"));
        assertNotEquals(4L, ReflectionTestUtils.getField(binding, "elderId"));

        mockMvc.perform(get("/api/users/me/elder-bindings")
                        .with(user("family1").authorities(List.of(new SimpleGrantedAuthority("elder-binding:request:my:list")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].elderId").value(5001))
                .andExpect(jsonPath("$[0].bindingType").value(UserConstants.ELDER_BINDING_TYPE_FAMILY))
                .andExpect(jsonPath("$[0].elderName").value("张三"));

        assertTrue(bindingRepository.findByUserIdOrderByCreatedDateTimeUtcDesc(4L).stream()
                .anyMatch(savedBinding -> Long.valueOf(5001L).equals(ReflectionTestUtils.getField(savedBinding, "elderId"))));
    }

    @Test
    @Transactional
    void shouldCreateElderProfileWhenCareCoreReturnsNotFoundDuringApproval() throws Exception {
        ElderProfileDto createdProfile = new ElderProfileDto();
        createdProfile.setElderId(7001L);
        createdProfile.setElderName("张三");
        createdProfile.setIdCard("110101195001010025");
        createdProfile.setPhone("13800138010");
        createdProfile.setGender("MALE");
        createdProfile.setBirthDate(LocalDate.of(1950, 1, 1));
        createdProfile.setStatus("ACTIVE");

        when(careCoreElderProfileClient.getByIdCard("110101195001010025"))
                .thenThrow(FeignException.errorStatus(
                        "CareCoreElderProfileClient#getByIdCard(String)",
                        Response.builder()
                                .status(404)
                                .reason("Not Found")
                                .headers(Collections.emptyMap())
                                .request(Request.create(
                                        Request.HttpMethod.GET,
                                        "http://localhost:18091/elders?idCard=110101195001010025",
                                        Collections.emptyMap(),
                                        null,
                                        StandardCharsets.UTF_8,
                                        null))
                                .build()));
        when(careCoreElderProfileClient.createIfAbsent(any(ElderProfileDto.class))).thenReturn(createdProfile);

        String createResponseBody = mockMvc.perform(post("/api/users/me/elder-binding-requests/family")
                        .with(user("family1").authorities(new SimpleGrantedAuthority("elder-binding:request:create")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "elderName": "张三",
                                  "elderIdCard": "110101195001010025",
                                  "elderPhone": "13800138010",
                                  "relationToElder": "儿子"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        Long requestId = objectMapper.readTree(createResponseBody).get("requestId").asLong();

        mockMvc.perform(post("/api/users/elder-binding-requests/{requestId}/approve", requestId)
                        .with(user("medic1").authorities(new SimpleGrantedAuthority("elder-binding:request:approve")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reviewComment": "资料核验通过"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.status").value(UserConstants.ELDER_BINDING_REQUEST_STATUS_APPROVED))
                .andExpect(jsonPath("$.elderId").value(7001));

        ArgumentCaptor<ElderProfileDto> elderProfileCaptor = ArgumentCaptor.forClass(ElderProfileDto.class);
        verify(careCoreElderProfileClient).createIfAbsent(elderProfileCaptor.capture());

        ElderProfileDto createdRequest = elderProfileCaptor.getValue();
        assertEquals("张三", createdRequest.getElderName());
        assertEquals("110101195001010025", createdRequest.getIdCard());
        assertEquals("13800138010", createdRequest.getPhone());
        assertEquals(LocalDate.of(1950, 1, 1), createdRequest.getBirthDate());
        assertEquals("2", createdRequest.getGender());
        assertEquals("ACTIVE", createdRequest.getStatus());

        UserElderBindingRequestPo request = requestRepository.findById(requestId).orElseThrow();
        assertEquals(UserConstants.ELDER_BINDING_REQUEST_STATUS_APPROVED, ReflectionTestUtils.getField(request, "status"));
        assertEquals(7001L, ReflectionTestUtils.getField(request, "elderId"));

        UserElderBindingPo binding = bindingRepository
                .findByUserIdAndElderIdAndBindingType(4L, 7001L, UserConstants.ELDER_BINDING_TYPE_FAMILY)
                .orElseThrow();
        assertEquals("儿子", ReflectionTestUtils.getField(binding, "relationToElder"));
    }

    @Test
    @Transactional
    void shouldRejectFamilyBindingRequestWhenRelationIsSelf() throws Exception {
        mockMvc.perform(post("/api/users/me/elder-binding-requests/family")
                        .with(user("family1").authorities(new SimpleGrantedAuthority("elder-binding:request:create")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "elderName": "张三",
                                  "elderIdCard": "110101195001010025",
                                  "elderPhone": "13800138010",
                                  "relationToElder": "本人"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("家属绑定申请中的与老人关系不能填写本人，请改用老人本人绑定"));
    }

    @Test
    @Transactional
    void shouldCreateSelfBindingWithoutFamilyRelation() throws Exception {
        ElderProfileDto elderProfile = new ElderProfileDto();
        elderProfile.setElderId(8001L);
        elderProfile.setElderName("老人用户");
        elderProfile.setIdCard("110101195001010033");
        elderProfile.setPhone("13800138002");
        elderProfile.setGender("MALE");
        elderProfile.setBirthDate(LocalDate.of(1950, 1, 1));
        elderProfile.setStatus("ACTIVE");

        when(careCoreElderProfileClient.getByIdCard("110101195001010033")).thenReturn(elderProfile);
        when(careCoreElderProfileClient.getByElderId(8001L)).thenReturn(elderProfile);

        var elderUser = userRepository.findByUsername("elder").orElseThrow();
        ReflectionTestUtils.setField(elderUser, "realName", "老人用户");
        ReflectionTestUtils.setField(elderUser, "idCard", "110101195001010033");
        userRepository.saveAndFlush(elderUser);

        mockMvc.perform(post("/api/users/me/elder-bindings/self")
                        .with(user("elder").authorities(new SimpleGrantedAuthority("elder-binding:self:bind"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderId").value(8001))
                .andExpect(jsonPath("$.bindingType").value(UserConstants.ELDER_BINDING_TYPE_SELF))
                .andExpect(jsonPath("$.relationToElder").value(nullValue()));

        UserElderBindingPo binding = bindingRepository
                .findByUserIdAndElderIdAndBindingType(3L, 8001L, UserConstants.ELDER_BINDING_TYPE_SELF)
                .orElseThrow();
        assertNull(ReflectionTestUtils.getField(binding, "relationToElder"));

        mockMvc.perform(get("/api/users/me/elder-bindings")
                        .with(user("elder").authorities(List.of(new SimpleGrantedAuthority("elder-binding:list")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].elderId").value(8001))
                .andExpect(jsonPath("$[0].bindingType").value(UserConstants.ELDER_BINDING_TYPE_SELF))
                .andExpect(jsonPath("$[0].relationToElder").value(nullValue()));
    }

    @Test
    @Transactional
    void shouldReturnRolesWithPermissionsAndPermissionListWithoutJsonCycle() throws Exception {
        mockMvc.perform(get("/api/roles/with-permissions")
                        .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_MANAGE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roleCode").isNotEmpty())
                .andExpect(jsonPath("$[0].permissions").isArray());

        mockMvc.perform(get("/api/permissions")
                        .with(user("admin").authorities(new SimpleGrantedAuthority("PERMISSION_MANAGE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].permissionCode").isNotEmpty());
    }
}
