package com.vasiliy.project.controller;

import com.vasiliy.project.dto.info.ReportDataDTO;
import com.vasiliy.project.exception.CustomBadRequestException;
import com.vasiliy.project.repository.ProductRepository;
import com.vasiliy.project.service.ReportService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/report-product")
public class ReportController {
    private final ReportService reportService;

    private final ProductRepository productRepository;


    @GetMapping("/{id}-{weeksNumber}")
    public ReportDataDTO getReportResponse(@PathVariable("id") Long productId, @PathVariable("weeksNumber") Integer weeksNumber) {
        if (!productRepository.existsById(productId)) {
            throw new CustomBadRequestException();
        }

        return reportService.getReportDTO(productId, weeksNumber);
    }
}
