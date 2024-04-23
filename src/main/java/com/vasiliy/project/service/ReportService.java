package com.vasiliy.project.service;


import com.vasiliy.project.dto.info.ReportDataDTO;

public interface ReportService {

    ReportDataDTO getReportDTO(Long productId, Integer numberOfLastWeeks);
}
