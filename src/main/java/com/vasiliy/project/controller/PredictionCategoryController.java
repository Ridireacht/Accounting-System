package com.vasiliy.project.controller;

import com.vasiliy.project.dto.info.PredictionCategoryDataDTO;
import com.vasiliy.project.exception.CustomBadRequestException;
import com.vasiliy.project.repository.CategoryRepository;
import com.vasiliy.project.service.PredictionCategoryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/prediction-category")
public class PredictionCategoryController {

  private final PredictionCategoryService predictionCategoryService;

  private final CategoryRepository categoryRepository;


  @GetMapping("/{id}-{weeksNumber}")
  public PredictionCategoryDataDTO getPredictionResponse(@PathVariable("id") Long categoryId, @PathVariable("weeksNumber") Integer weeksNumber) {
    if (!categoryRepository.existsById(categoryId)) {
      throw new CustomBadRequestException();
    }

    return predictionCategoryService.getPredictionDTO(categoryId, weeksNumber);
  }
}
