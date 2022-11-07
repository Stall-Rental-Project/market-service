package com.srs.market.repository;

import com.srs.market.entity.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketRepository extends JpaRepository<MarketEntity, UUID> {
    @Query("select m from MarketEntity m " +
            "where lower(m.name) = lower(:name) " +
            "and lower(m.location.city) = lower(:city) " +
            "and m.deleted = false")
    List<MarketEntity> findAllByNameAndCity(@Param("name") String name, @Param("city") String city);

    boolean existsByNameIgnoreCaseAndLocation_CityIgnoreCaseAndDeletedFalse(String name, String city);

    @Query("select m.marketId from MarketEntity m " +
            "where m.marketId in (:ids) " +
            "and (m.state <> 2 or exists (select 1 from MarketEntity mm where mm.previousVersion = m.marketId))")
    List<UUID> findAllHasDraftIn(@Param("ids") Collection<UUID> marketIds);

    @Query("select m from MarketEntity m " +
            "join fetch m.location " +
            "join fetch m.supervisor " +
            "where m.marketId = :id " +
            "or m.previousVersion = :id " +
            "or exists (select 1 from MarketEntity mm " +
            "           where mm.previousVersion = m.marketId " +
            "           and mm.marketId = :id)")
    List<MarketEntity> findAllByIdFetchLocationAndSupervisor(@Param("id") UUID id);

    @Query("select m from MarketEntity m " +
            "join fetch m.location " +
            "join fetch m.supervisor " +
            "where m.code = :code")
    List<MarketEntity> findAllByCodeFetchLocationAndSupervisor(@Param("code") String code);

    @Query("select m from MarketEntity m " +
            "where (m.marketId = :id " +
            "       or m.previousVersion = :id " +
            "       or exists (select 1 from MarketEntity mm " +
            "                  where mm.previousVersion = m.marketId " +
            "                  and mm.marketId = :id)) " +
            "and m.deleted = false")
    List<MarketEntity> findAllVersionsById(@Param("id") UUID givenMarketId);
    @Query("select m from MarketEntity m " +
            "left join fetch m.supervisor " +
            "join fetch m.location " +
            "where m.marketId = :id")
    Optional<MarketEntity> findByIdFetchLocationAndSupervisor(@Param("id") UUID marketId);

    @Query("select m from MarketEntity m " +
            "where exists (select 1 " +
            "              from MarketEntity d " +
            "              where d.marketId = :id " +
            "               and d.previousVersion = m.marketId)")
    Optional<MarketEntity> findPrimaryVersionById(@Param("id") UUID marketId);

    @Query("select m from MarketEntity m " +
            "left join fetch m.supervisor " +
            "join fetch m.location " +
            "where m.previousVersion = :id")
    Optional<MarketEntity> findDraftVersionByIdFetchLocationAndSupervisor(@Param("id") UUID marketId);

    Optional<MarketEntity> findByMarketIdAndDeletedIsFalse(UUID marketId);


    @Query("select m from MarketEntity m " +
            "join fetch m.location " +
            "where (m.marketId = :id " +
            "       or m.previousVersion = :id " +
            "       or m.marketId = (select mm.previousVersion " +
            "                        from MarketEntity mm " +
            "                        where mm.marketId = :id)" +
            ")" +
            "and (m.deleted = false " +
            "     or (m.deleted = true and m.state = 1)" +
            ")")
    List<MarketEntity> findAllPublishableMarketsById(@Param("id") UUID marketId);
}
