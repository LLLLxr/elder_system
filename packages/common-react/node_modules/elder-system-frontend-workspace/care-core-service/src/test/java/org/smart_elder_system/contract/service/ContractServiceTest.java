package org.smart_elder_system.contract.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smart_elder_system.common.dto.care.ServiceAgreementDTO;
import org.smart_elder_system.contract.model.ServiceAgreement;
import org.smart_elder_system.contract.po.ServiceAgreementPo;
import org.smart_elder_system.contract.repository.ServiceAgreementRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ServiceAgreementRepository serviceAgreementRepository;

    @InjectMocks
    private ContractService contractService;

    @Test
    void shouldCreateDraftAgreementUsingModelConversion() {
        ServiceAgreementDTO request = new ServiceAgreementDTO();
        request.setApplicationId(1L);
        request.setElderId(1001L);
        request.setServiceScene("HOME");

        when(serviceAgreementRepository.findLatestByApplicationIdForUpdate(1L)).thenReturn(Optional.empty());
        when(serviceAgreementRepository.save(any())).thenAnswer(invocation -> {
            ServiceAgreementPo po = invocation.getArgument(0);
            po.setId(10L);
            return po;
        });

        ServiceAgreementDTO result = contractService.createDraftAgreement(request);

        assertEquals(10L, result.getAgreementId());
        assertEquals(ServiceAgreement.STATUS_DRAFT, result.getStatus());

        ArgumentCaptor<ServiceAgreementPo> captor = ArgumentCaptor.forClass(ServiceAgreementPo.class);
        verify(serviceAgreementRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getApplicationId());
        assertEquals(1001L, captor.getValue().getElderId());
        assertEquals("HOME", captor.getValue().getServiceScene());
        assertEquals(ServiceAgreement.STATUS_DRAFT, captor.getValue().getStatus());
    }

    @Test
    void shouldSignAgreementByApplyingDomainToExistingPo() {
        ServiceAgreementPo po = agreementPo(1L, ServiceAgreement.STATUS_DRAFT, null, null, null);
        when(serviceAgreementRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));
        when(serviceAgreementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceAgreementDTO request = new ServiceAgreementDTO();
        request.setAgreementId(1L);
        request.setSignedBy("签约员");
        request.setEffectiveDate(LocalDate.of(2026, 4, 26));
        request.setExpiryDate(LocalDate.of(2027, 4, 26));

        ServiceAgreementDTO result = contractService.signAgreement(request);

        assertEquals(ServiceAgreement.STATUS_ACTIVE, result.getStatus());
        assertEquals("签约员", po.getSignedBy());
        assertEquals(LocalDate.of(2026, 4, 26), po.getEffectiveDate());
        assertEquals(LocalDate.of(2027, 4, 26), po.getExpiryDate());
    }

    @Test
    void shouldRevertAgreementToDraft() {
        ServiceAgreementPo po = agreementPo(1L, ServiceAgreement.STATUS_ACTIVE, "签约员", LocalDate.of(2026, 4, 26), LocalDate.of(2027, 4, 26));
        when(serviceAgreementRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));
        when(serviceAgreementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceAgreementDTO result = contractService.revertToDraftAgreement(1L, "退回");

        assertEquals(ServiceAgreement.STATUS_DRAFT, result.getStatus());
        assertNull(po.getSignedBy());
        assertNull(po.getEffectiveDate());
        assertNull(po.getExpiryDate());
    }

    private ServiceAgreementPo agreementPo(Long id, String status, String signedBy, LocalDate effectiveDate, LocalDate expiryDate) {
        ServiceAgreementPo po = new ServiceAgreementPo();
        po.setId(id);
        po.setApplicationId(1L);
        po.setElderId(1001L);
        po.setServiceScene("HOME");
        po.setStatus(status);
        po.setSignedBy(signedBy);
        po.setEffectiveDate(effectiveDate);
        po.setExpiryDate(expiryDate);
        return po;
    }
}
