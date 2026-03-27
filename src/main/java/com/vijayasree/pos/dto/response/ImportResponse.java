package com.vijayasree.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportResponse {
    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<String> errors;
}