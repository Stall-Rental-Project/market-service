package com.srs.market.repository;

import com.srs.market.dto.projection.FloorStallCountProjection;
import com.srs.market.dto.projection.RefIdProjection;
import com.srs.market.entity.FloorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FloorRepository extends JpaRepository<FloorEntity,UUID> {
    @Modifying
    @Query("update FloorEntity f set f.deleted = true, f.state = 1 " +
            "where f.floorId = :id and f.deleted = false")
    void softDeleteById(@Param("id") UUID id);

    @Query("select f from FloorEntity f " +
            "where f.market.marketId = :id")
    List<FloorEntity> findAllByMarketId(@Param("id") UUID marketId);


    boolean existsByMarketMarketIdAndNameIgnoreCaseAndDeletedIsFalse(UUID marketId, String floor);

    @Query("select new com.srs.market.dto.projection.RefIdProjection(f.floorId, f.previousVersion) " +
            "from FloorEntity f " +
            "where f.market.marketId = (select ff.market.marketId " +
            "                           from FloorEntity ff " +
            "                           where ff.floorId = :id) " +
            "and f.floorId <> :id " +
            "and f.previousVersion <> :id " +
            "and lower(f.name) = lower(:name) ")
    List<RefIdProjection> findAllByNameIgnoreCaseAndFloorIdIsNot(@Param("name") String name, @Param("id") UUID fromString);
    boolean existsByPreviousVersion(UUID floorplanId);

    @Query("select f from FloorEntity f " +
            "where f.previousVersion = :id")
    Optional<FloorEntity> findDraftVersionById(@Param("id") UUID floorId);

    @Query("select f from FloorEntity f " +
            "join fetch f.market " +
            "where (f.floorId = :id " +
            "       or f.previousVersion = :id " +
            "       or exists (select 1 from FloorEntity ff " +
            "                  where ff.previousVersion = f.floorId " +
            "                  and ff.floorId = :id)) " +
            "and f.deleted = false")
    List<FloorEntity> findAllById4Delete(@Param("id") UUID id);

    @Query("select new com.srs.market.dto.projection.FloorStallCountProjection(s.floor.floorId, count(s.stallId)) " +
            "from StallEntity s " +
            "where s.market.marketId = :marketId " +
            "and s.previousVersion is null " +
            "and s.deleted = false " +
            "group by s.floor.floorId")
    List<FloorStallCountProjection> countTotalStalls4EachFloorInMarket(@Param("marketId") UUID marketId);

}
