package com.vasiliy.project.service;

import com.vasiliy.project.dto.info.PredictionProductDataDTO;

import java.util.List;

public interface PredictionProductService {

  List<Integer> collectOutflowValues(Long productId, Integer numberOfLastWeeks);

  PredictionProductDataDTO getPredictionDTO(Long productId, Integer numberOfLastWeeks);

  Double getNextWeekPrediction(List<Integer> weekOutflowValues);

  Double getNextMonthPrediction(List<Integer> monthOutflowValues);
}