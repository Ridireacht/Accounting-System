package com.vasiliy.project.dto.info;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PredictionCategoryDataDTO {
    private List<Double> nextWeekOutflowPredictions;
    private List<Double> nextMonthOutflowPredictions;

    private List<String> labels;
    private List<String> productNames;
    private List<List<Integer>> outflowValuesLists;
}
