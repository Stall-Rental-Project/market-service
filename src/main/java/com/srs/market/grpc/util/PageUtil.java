package com.srs.market.grpc.util;

import com.srs.common.CommonConstant;
import com.srs.common.PageRequest;

import java.util.List;

public class PageUtil {
    public static PageRequest normalizeRequest(PageRequest pageRequest, List<String> sorts) {
        int page = pageRequest.getPage() > 0 ? pageRequest.getPage() : CommonConstant.DEFAULT_PAGE;
        int size = pageRequest.getSize() > 0 ? pageRequest.getSize() : CommonConstant.DEFAULT_PAGE_SIZE;
        String sort = sorts.contains(pageRequest.getSort()) ? pageRequest.getSort() : sorts.get(0);
        String direction = "desc".equalsIgnoreCase(pageRequest.getDirection()) ? pageRequest.getDirection().toUpperCase() : "asc";

        return PageRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSort(sort)
                .setDirection(direction)
                .build();
    }

    public static int calcTotalPages(long totalElements, int pageSize) {
        return Math.toIntExact((totalElements + pageSize - 1) / pageSize);
    }

    public static long calcPageOffset(int page, int pageSize) {
        return (long) (page - 1) * pageSize;
    }
}
