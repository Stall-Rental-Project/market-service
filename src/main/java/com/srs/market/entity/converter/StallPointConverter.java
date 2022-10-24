package com.srs.market.entity.converter;

import com.srs.market.common.dto.StallPoint;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Converter
public class StallPointConverter implements AttributeConverter<List<StallPoint>, String> {
    @Override
    public String convertToDatabaseColumn(List<StallPoint> stallPoints) {
        if (CollectionUtils.isEmpty(stallPoints)) {
            return "";
        }

        return stallPoints.stream().map(p -> p.getXAxis() + "," + p.getYAxis()).collect(Collectors.joining("|"));
    }

    @Override
    public List<StallPoint> convertToEntityAttribute(String s) {
        if (!StringUtils.hasText(s)) {
            return new ArrayList<>();
        }

        String[] points = s.split("\\|");

        List<StallPoint> stallPoints = new ArrayList<>();

        for (String point : points) {
            String[] axis = point.split(",");
            Double xAxis = Double.parseDouble(axis[0]);
            Double yAxis = Double.parseDouble(axis[1]);
            stallPoints.add(new StallPoint(xAxis, yAxis));
        }

        return stallPoints;
    }
}
