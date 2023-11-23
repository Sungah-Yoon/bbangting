package com.khu.bbangting.repository;

import com.khu.bbangting.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByStoreIdAndUserId(Long storeId, Long userId);
}
