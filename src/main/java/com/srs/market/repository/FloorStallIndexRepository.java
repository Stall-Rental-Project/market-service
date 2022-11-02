package com.srs.market.repository;

import com.srs.market.entity.FloorStallIndexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;


public interface FloorStallIndexRepository extends JpaRepository<FloorStallIndexEntity, UUID> {
    @Query("select f.currentIndex from FloorStallIndexEntity f where f.floorCode = :code")
    Integer getCurrentIndexOfFloor(@Param("code") String code);

    @Modifying
    @Query("update FloorStallIndexEntity f set f.currentIndex = f.currentIndex + :num where f.floorCode = :code")
    void increaseFloorIndex(@Param("code") String code, @Param("num") Integer num);
}
