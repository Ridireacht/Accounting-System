package com.vasiliy.project.service;

import com.vasiliy.project.dto.info.PredictionCategoryDataDTO;

import java.util.List;

public interface PredictionCategoryService {
    List<Integer> collectOutflowValueByProduct(Long productId, Integer numberOfLastWeeks);

    List<Integer> collectOutflowValues(Long categoryId, Integer numberOfLastWeeks);

    PredictionCategoryDataDTO getPredictionDTO(Long categoryId, Integer numberOfLastWeeks);

    Double getNextWeekPrediction(List<Integer> weekOutflowValues);

    Double getNextMonthPrediction(List<Integer> monthOutflowValues);
}
