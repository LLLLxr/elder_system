package org.smart_elder_system.elder.service;

import org.smart_elder_system.common.dto.elder.ElderProfileDto;
import org.smart_elder_system.elder.vo.ElderProfile;
import org.smart_elder_system.elder.po.ElderProfilePo;
import org.smart_elder_system.elder.repository.ElderProfileRepository;
import org.smart_elder_system.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderProfileService {

    private final ElderProfileRepository elderProfileRepository;

    public ElderProfileService(ElderProfileRepository elderProfileRepository) {
        this.elderProfileRepository = elderProfileRepository;
    }

    @Transactional(readOnly = true)
    public ElderProfileDto getByElderId(Long elderId) {
        ElderProfilePo po = elderProfileRepository.findById(elderId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到老人档案"));
        return ElderProfile.fromPo(po).toDto();
    }

    @Transactional(readOnly = true)
    public ElderProfileDto getByIdCard(String idCard) {
        ElderProfilePo po = elderProfileRepository.findByIdCard(idCard)
                .orElseThrow(() -> new ResourceNotFoundException("未找到老人档案"));
        return ElderProfile.fromPo(po).toDto();
    }

    @Transactional(rollbackFor = Exception.class)
    public ElderProfileDto createIfAbsent(ElderProfileDto dto) {
        ElderProfilePo existing = elderProfileRepository.findByIdCardForUpdate(dto.getIdCard()).orElse(null);
        if (existing != null) {
            merge(existing, dto);
            return ElderProfile.fromPo(elderProfileRepository.save(existing)).toDto();
        }

        ElderProfile profile = ElderProfile.fromDto(dto);
        profile.initialize();

        try {
            ElderProfilePo saved = elderProfileRepository.save(profile.toPo());
            return ElderProfile.fromPo(saved).toDto();
        } catch (DataIntegrityViolationException exception) {
            ElderProfilePo fallback = elderProfileRepository.findByIdCard(dto.getIdCard()).orElseThrow(() -> exception);
            merge(fallback, dto);
            return ElderProfile.fromPo(elderProfileRepository.save(fallback)).toDto();
        }
    }

    private void merge(ElderProfilePo po, ElderProfileDto dto) {
        if (dto.getElderName() != null && !dto.getElderName().isBlank()) {
            po.setElderName(dto.getElderName());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            po.setPhone(dto.getPhone());
        }
        if (dto.getGender() != null && !dto.getGender().isBlank()) {
            po.setGender(dto.getGender());
        }
        if (dto.getBirthDate() != null) {
            po.setBirthDate(dto.getBirthDate());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            po.setStatus(dto.getStatus());
        }
    }
}
