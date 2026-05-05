package com.college.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDto {

    private List<String> labels;
    private List<Object> datasets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dataset {
        private String label;
        private List<Object> data;
        private List<String> backgroundColor;
        private String borderColor;
        private boolean fill;
        private float tension;
    }
}
