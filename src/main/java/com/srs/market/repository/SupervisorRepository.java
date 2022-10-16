package com.srs.market.repository;

import com.srs.market.entity.SupervisorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupervisorRepository extends JpaRepository<SupervisorEntity, UUID> {
    @Query("select s from SupervisorEntity s where s.market.marketId = :id")
    Optional<SupervisorEntity> findByMarketId(@Param("id") UUID marketId);}
