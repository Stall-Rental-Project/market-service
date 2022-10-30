package com.srs.market.repository;

import com.srs.market.dto.projection.StallWithDetailProjection;
import com.srs.market.entity.StallEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StallRepository extends JpaRepository<StallEntity, UUID> {
    @Modifying
    @Query("update StallEntity s set s.deleted = true, s.state = 1 " +
            "where s.floor.floorId = :id " +
            "and s.deleted = false ")
    void softDeleteNonDraftVersionByFloorId(@Param("id") UUID id);

    @Modifying
    @Query("delete from StallEntity s " +
            "where s.floor.floorId = :id " +
            "and s.previousVersion is not null ")
    void hardDeleteDraftVersionByFloorId(@Param("id") UUID id);

    @Modifying
    @Query("update StallEntity s set s.deleted = true, s.state = 1 " +
            "where s.floor.floorId = :id " +
            "and s.previousVersion is null and s.deleted = false")
    void softDeleteNonDraftVersionByFloorIdId(@Param("id") UUID originalId);
    @Query("select new com.srs.market.dto.projection.StallWithDetailProjection(s.stallId, s.floor.floorId, s.name, s.shape) "
            +
            "from StallEntity s " +
            "where s.market.marketId = :marketId " +
            "and s.previousVersion is null " +
            "and s.deleted = false")
    List<StallWithDetailProjection> checkPrimaryStallThatHasDetail(
            @Param("marketId") UUID fromString);

    @Query("select new com.srs.market.dto.projection.StallWithDetailProjection(s.stallId, s.floor.floorId, s.name, s.shape, s.previousVersion) " +
            "from StallEntity s " +
            "where s.market.marketId = :marketId " +
            "and s.previousVersion is not null " +
            "and s.deleted = false")
    List<StallWithDetailProjection> checkDraftStallThatHasDetail(
            @Param("marketId") UUID fromString);
}
