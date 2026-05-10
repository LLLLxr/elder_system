package org.smart_elder_system.elder.repository;

import jakarta.persistence.LockModeType;
import org.smart_elder_system.elder.po.ElderProfilePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ElderProfileRepository extends JpaRepository<ElderProfilePo, Long> {

    Optional<ElderProfilePo> findByIdCard(String idCard);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM ElderProfilePo e WHERE e.idCard = :idCard")
    Optional<ElderProfilePo> findByIdCardForUpdate(@Param("idCard") String idCard);
}
