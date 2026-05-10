package org.smart_elder_system.caredelivery.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.caredelivery.po.NurseCareRecordPo;
import org.smart_elder_system.common.dto.caredelivery.NurseCareRecordDto;
import org.smart_elder_system.common.dto.caredelivery.NurseCareRecordSaveDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NurseCareRecord {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private Long recordId;
    private Long elderId;
    private String elderName;
    private Long nurseId;
    private String nurseName;
    private Long servicePlanId;
    private LocalDate recordDate;
    private Map<String, Object> recordFormData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NurseCareRecord create(Long nurseId, String nurseName, String elderName, NurseCareRecordSaveDto dto) {
        return NurseCareRecord.builder()
                .elderId(dto.getElderId())
                .elderName(elderName)
                .nurseId(nurseId)
                .nurseName(nurseName)
                .servicePlanId(dto.getServicePlanId())
                .recordDate(dto.getRecordDate())
                .recordFormData(dto.getRecordFormData())
                .build();
    }

    public static NurseCareRecord fromPo(NurseCareRecordPo po, ObjectMapper objectMapper) {
        return NurseCareRecord.builder()
                .recordId(po.getId())
                .elderId(po.getElderId())
                .elderName(po.getElderName())
                .nurseId(po.getNurseId())
                .nurseName(po.getNurseName())
                .servicePlanId(po.getServicePlanId())
                .recordDate(po.getRecordDate())
                .recordFormData(readRecordFormData(po.getRecordFormData(), objectMapper))
                .createdAt(po.getCreatedDateTimeUtc())
                .updatedAt(po.getLastModifiedDateTimeUtc())
                .build();
    }

    public NurseCareRecordDto toDto() {
        return NurseCareRecordDto.builder()
                .recordId(recordId)
                .elderId(elderId)
                .elderName(elderName)
                .nurseId(nurseId)
                .nurseName(nurseName)
                .servicePlanId(servicePlanId)
                .recordDate(recordDate)
                .recordFormData(recordFormData)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public NurseCareRecordPo toPo(ObjectMapper objectMapper) {
        return NurseCareRecordPo.builder()
                .id(recordId)
                .elderId(elderId)
                .elderName(elderName)
                .nurseId(nurseId)
                .nurseName(nurseName)
                .servicePlanId(servicePlanId)
                .recordDate(recordDate)
                .recordFormData(writeRecordFormData(recordFormData, objectMapper))
                .build();
    }

    public void applyTo(NurseCareRecordPo po, ObjectMapper objectMapper) {
        po.setId(recordId);
        po.setElderId(elderId);
        po.setElderName(elderName);
        po.setNurseId(nurseId);
        po.setNurseName(nurseName);
        po.setServicePlanId(servicePlanId);
        po.setRecordDate(recordDate);
        po.setRecordFormData(writeRecordFormData(recordFormData, objectMapper));
    }

    public void updateFrom(NurseCareRecordSaveDto dto) {
        this.elderId = dto.getElderId();
        this.servicePlanId = dto.getServicePlanId();
        this.recordDate = dto.getRecordDate();
        this.recordFormData = dto.getRecordFormData();
    }

    private static Map<String, Object> readRecordFormData(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("护理记录表单数据格式错误");
        }
    }

    private static String writeRecordFormData(Map<String, Object> data, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(data == null ? Map.of() : data);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("护理记录表单数据序列化失败");
        }
    }
}
