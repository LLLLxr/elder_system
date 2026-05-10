package org.smart_elder_system.health.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smart_elder_system.common.dto.health.DoctorRoundRecordDto;
import org.smart_elder_system.common.dto.health.DoctorRoundRecordSaveDto;
import org.smart_elder_system.health.po.DoctorRoundRecordPo;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRoundRecord {

    private Long roundRecordId;
    private Long elderId;
    private String elderName;
    private Long doctorId;
    private String doctorName;
    private String content;
    private Boolean riskFlag;
    private LocalDateTime roundTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DoctorRoundRecord create(Long doctorId, String doctorName, String elderName, DoctorRoundRecordSaveDto dto) {
        return DoctorRoundRecord.builder()
                .elderId(dto.getElderId())
                .elderName(elderName)
                .doctorId(doctorId)
                .doctorName(doctorName)
                .content(dto.getContent())
                .riskFlag(dto.getRiskFlag())
                .roundTime(dto.getRoundTime())
                .build();
    }

    public static DoctorRoundRecord fromPo(DoctorRoundRecordPo po) {
        return DoctorRoundRecord.builder()
                .roundRecordId(po.getId())
                .elderId(po.getElderId())
                .elderName(po.getElderName())
                .doctorId(po.getDoctorId())
                .doctorName(po.getDoctorName())
                .content(po.getContent())
                .riskFlag(po.getRiskFlag())
                .roundTime(po.getRoundTime())
                .createdAt(po.getCreatedDateTimeUtc())
                .updatedAt(po.getLastModifiedDateTimeUtc())
                .build();
    }

    public DoctorRoundRecordDto toDto() {
        return DoctorRoundRecordDto.builder()
                .roundRecordId(roundRecordId)
                .elderId(elderId)
                .elderName(elderName)
                .doctorId(doctorId)
                .doctorName(doctorName)
                .content(content)
                .riskFlag(riskFlag)
                .roundTime(roundTime)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public DoctorRoundRecordPo toPo() {
        return DoctorRoundRecordPo.builder()
                .id(roundRecordId)
                .elderId(elderId)
                .elderName(elderName)
                .doctorId(doctorId)
                .doctorName(doctorName)
                .content(content)
                .riskFlag(riskFlag)
                .roundTime(roundTime)
                .build();
    }

    public void applyTo(DoctorRoundRecordPo po) {
        po.setId(roundRecordId);
        po.setElderId(elderId);
        po.setElderName(elderName);
        po.setDoctorId(doctorId);
        po.setDoctorName(doctorName);
        po.setContent(content);
        po.setRiskFlag(riskFlag);
        po.setRoundTime(roundTime);
    }

    public void updateFrom(DoctorRoundRecordSaveDto dto) {
        this.elderId = dto.getElderId();
        this.content = dto.getContent();
        this.riskFlag = dto.getRiskFlag();
        this.roundTime = dto.getRoundTime();
    }
}
