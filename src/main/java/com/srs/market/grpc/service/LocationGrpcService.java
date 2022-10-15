package com.srs.market.grpc.service;

import com.srs.common.FindByIdsRequest;
import com.srs.common.ListResponse;
import com.srs.market.*;

public interface LocationGrpcService {
    ListProvinceResponse listProvinces(ListProvinceRequest request);

    ListCityResponse listCities(ListCityRequest request);

    ListWardResponse listWards(ListWardRequest request);

    GetLocationResponse getLocation(GetLocationRequest request);

    ListResponse listLocations(FindByIdsRequest request);

}
