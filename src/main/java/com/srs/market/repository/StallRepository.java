package com.srs.market.repository;

import com.srs.market.dto.projection.StallWithDetailProjection;
import com.srs.market.entity.StallEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public interface StallRepository extends JpaRepository<StallEntity, UUID> {
    @Query("select s from StallEntity s " +
            "where s.market.marketId = :id ")
    List<StallEntity> findAllByMarketId(@Param("id") UUID primaryId);

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
    @Query("select new com.srs.market.dto.projection.StallWithDetailProjection(s.stallId, s.floor.floorId, s.name) "
            +
            "from StallEntity s " +
            "where s.market.marketId = :marketId " +
            "and s.previousVersion is null " +
            "and s.deleted = false")
    List<StallWithDetailProjection> checkPrimaryStallThatHasDetail(
            @Param("marketId") UUID fromString);

    @Query("select new com.srs.market.dto.projection.StallWithDetailProjection(s.stallId, s.floor.floorId, s.name, s.previousVersion) " +
            "from StallEntity s " +
            "where s.market.marketId = :marketId " +
            "and s.previousVersion is not null " +
            "and s.deleted = false")
    List<StallWithDetailProjection> checkDraftStallThatHasDetail(
            @Param("marketId") UUID fromString);

    boolean existsByPreviousVersion(UUID stallId);

    @Query("select s from StallEntity s where s.previousVersion = :id")
    Optional<StallEntity> findDraftVersionById(@Param("id") UUID stallId);

    @Query("select s from StallEntity s " +
            "inner join fetch s.market " +
            "where s.stallId = :id " +
            "and s.previousVersion is null " +
            "and (" +
            "   (s.deleted = false and s.state = 2) " +
            "   or (s.deleted = true and s.state = 1 and s.publishedAtLeastOnce = true)" +
            ")")
    Optional<StallEntity> findById4SubmitApplication(@Param("id") UUID fromString);

    @Query("select s from StallEntity s " +
            "where s.floor.floorId = :id ")
    List<StallEntity> findAllByFloorId(@Param("id") UUID primaryId);

    @Query("select s from StallEntity s " +
            "where s.floor.floorId = :id " +
            "and s.previousVersion is null " +
            "and (" +
            "   (s.state = 2 and s.deleted = false) " +
            "   or (s.state = 1 and s.deleted = true and s.publishedAtLeastOnce = true)" +
            ")")
    List<StallEntity> findAllPublishedStallsByFloorId(@Param("id") UUID floorplanId);

    @Modifying
    @Transactional
    @Query("update StallEntity s set s.deleted = true, s.state = 1 " +
            "where s.stallId = :id and s.previousVersion is null")
    void softDeleteNonDraftVersionByIds(@Param("id") UUID stallId);

    @Modifying
    @Transactional
    @Query("delete from StallEntity s " +
            "where s.previousVersion = :id " +
            "or (s.stallId = :id " +
            "and s.previousVersion is not null)")
    void hardDeleteDraftVersionByIds(@Param("id") UUID stallId);

    @Query("select s from StallEntity s " +
            "join fetch s.market " +
            "join fetch s.floor " +
            "where s.previousVersion is null " +
            "and s.clonedFrom is null " +
            "and concat(coalesce(s.market.code, ''), coalesce(s.floor.code, ''), coalesce(s.code, '')) = :searcher")
    Optional<StallEntity> findByMarketCodeAndFloorCodeAndStallCode(@Param("searcher") String searcher);


    @Query("select s from StallEntity s " +
            "join fetch s.market " +
            "join fetch s.floor " +
            "where s.previousVersion is null " +
            "and s.clonedFrom is null " +
            "and concat(coalesce(s.market.code, ''), coalesce(s.floor.code, ''), coalesce(s.code, '')) in (:collect)")
    List<StallEntity> findAnd4Rent(@Param("collect") Collection<String> collect);

    @Query("SELECT s FROM StallEntity s " +
            "WHERE s.market.marketId = (SELECT m.marketId FROM MarketEntity m " +
            "                         WHERE m.code = :marketCode " +
            "                         AND m.previousVersion IS NULL) " +
            "AND s.code = :stallCode " +
            "AND s.floor.floorId = (SELECT f.floorId FROM FloorEntity f " +
            "                               WHERE f.code = :floorCode " +
            "                               AND f.previousVersion IS NULL)")
    List<StallEntity> findAllByMarketCodeAndFloorCodeAndStallCode(
            @Param("marketCode") String marketCode,
            @Param("floorCode") String floorCode,
            @Param("stallCode") String stallCode);
}
