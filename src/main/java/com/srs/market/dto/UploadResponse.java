package com.srs.market.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author duynt on 9/27/21
 */
@Getter
@Setter
public class UploadResponse implements Serializable {
    protected String fileName;
    protected Long fileSize; // in bytes
    protected List<String> fileErrors = new ArrayList<>();
    protected Integer numRows;
    protected Integer numValidRows;
    protected Integer numInvalidRows;
    protected List<Map<String, Object>> data = new ArrayList<>();
}
