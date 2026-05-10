package org.smart_elder_system.user.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.user.po.UserElderBindingRequestPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserElderBindingRequestRepository extends JpaRepository<UserElderBindingRequestPo, Long> {

    List<UserElderBindingRequestPo> findByApplicantUserIdOrderByCreatedDateTimeUtcDesc(Long applicantUserId);

    List<UserElderBindingRequestPo> findByStatusOrderByCreatedDateTimeUtcDesc(String status);

    boolean existsByApplicantUserIdAndElderIdCardAndStatus(Long applicantUserId, String elderIdCard, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM UserElderBindingRequestPo r WHERE r.id = :requestId")
    Optional<UserElderBindingRequestPo> findByIdForUpdate(@Param("requestId") Long requestId);
}
