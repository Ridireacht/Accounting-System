package com.vasiliy.project.dto.info;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PredictionCategoryDataDTO {
    private Double nextWeekOutflowPrediction;
    private Double nextMonthOutflowPrediction;

    private List<String> labels;
    private List<String> productNames;
    private List<List<Integer>> outflowValuesLists;
}
