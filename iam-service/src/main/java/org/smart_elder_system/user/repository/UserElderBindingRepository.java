package org.smart_elder_system.user.repository;

import org.smart_elder_system.user.po.UserElderBindingPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserElderBindingRepository extends JpaRepository<UserElderBindingPo, Long> {

    List<UserElderBindingPo> findByUserIdOrderByCreatedDateTimeUtcDesc(Long userId);

    Optional<UserElderBindingPo> findByUserIdAndElderIdAndBindingType(Long userId, Long elderId, String bindingType);

    Optional<UserElderBindingPo> findTopByElderIdAndBindingType(Long elderId, String bindingType);
}
