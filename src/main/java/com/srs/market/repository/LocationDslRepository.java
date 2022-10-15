package com.srs.market.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.market.ListCityRequest;
import com.srs.market.ListWardRequest;
import com.srs.market.entity.LocationEntity;
import com.srs.market.entity.QLocationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Log4j2
public class LocationDslRepository {
    private final QLocationEntity location = QLocationEntity.locationEntity;
    private final JPAQueryFactory queryFactory;

    public List<String> findAllProvinces(String searchTerm) {
        var query = queryFactory.selectDistinct(location.province)
                .from(location)
                .orderBy(location.province.asc());

        if (StringUtils.isNotBlank(searchTerm)) {
            query.where(location.province.containsIgnoreCase(searchTerm));
        }

        return query.fetch();
    }


    public List<String> findAllCitiesByProvince(ListCityRequest request) {
        var province = request.getProvince();
        var searchTerm = request.getSearchTerm();

        var query = queryFactory.selectDistinct(location.city)
                .from(location)
                .where(location.province.equalsIgnoreCase(province))
                .orderBy(location.city.asc());

        if (StringUtils.isNotBlank(searchTerm)) {
            query.where(location.city.containsIgnoreCase(searchTerm));
        }

        return query.fetch();
    }


    public List<String> findAllBarangaysByProvinceAndCity(ListWardRequest request) {
        var city = request.getCity();
        var province = request.getProvince();
        var searchTerm = request.getSearchTerm();

        var query = queryFactory.select(location.ward)
                .distinct()
                .from(location)
                .where(location.province.equalsIgnoreCase(province))
                .where(location.city.equalsIgnoreCase(city))
                .orderBy(location.ward.asc());

        if (StringUtils.isNotBlank(searchTerm)) {
            query.where(location.ward.containsIgnoreCase(searchTerm));
        }

        return query.fetch();
    }

    public Optional<LocationEntity> findByProvinceAndCityAndWard(String province, String city, String ward) {
        var query = queryFactory.select(location)
                .from(location)
                .where(location.province.equalsIgnoreCase(province))
                .where(location.city.equalsIgnoreCase(city))
                .where(location.ward.equalsIgnoreCase(ward));

        return Optional.ofNullable(query.fetchFirst());
    }
}
