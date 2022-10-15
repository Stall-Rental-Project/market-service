package com.srs.market.repository;

import com.srs.market.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, UUID> {
    @Query("select l from LocationEntity l " +
            "where lower(l.province) = lower(:province) " +
            "and lower(l.city) = lower(:city) " +
            "and lower(l.ward) = lower(:ward) ")
    Optional<LocationEntity> findLocation(@Param("province") String province,
                                          @Param("city") String city, @Param("ward") String ward);

    @Query("select l from LocationEntity l " +
            "where l.locationId in (:ids)")
    List<LocationEntity> findAllByIds(@Param("ids") Collection<UUID> locationIds);

    @Query("SELECT l FROM LocationEntity l " +
            "WHERE l.locationId = :id")
    Optional<LocationEntity> findOneById(@Param("id") UUID locationId);
}
