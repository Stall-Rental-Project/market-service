package com.srs.market.dto;

import com.querydsl.core.types.OrderSpecifier;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Min;

/**
 * @author duynt on 9/27/21
 */
@Getter
@Setter
public class PageRequest {
    public static final int DEFAULT_PAGE_SIZE = 20;

    @Min(1)
    protected Integer page = 1;
    @Min(1)
    protected Integer size = 20;
    protected String sort;
    protected String direction;

    // This will and should be set by SortingHelper only. Always not null
    protected Integer internalPage;
    protected Integer internalSize;
    protected Long internalOffset;
    protected String internalSort;
    protected Sort.Direction internalDirection;
    protected OrderSpecifier<?> internalOrderSpecifier;
    protected Pageable internalPageable;
}
