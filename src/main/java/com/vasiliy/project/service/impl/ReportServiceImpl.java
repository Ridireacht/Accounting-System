package com.vasiliy.project.service.impl;

import com.vasiliy.project.dto.info.PredictionDataDTO;
import com.vasiliy.project.dto.info.ReportDataDTO;
import com.vasiliy.project.entity.records.InflowRecord;
import com.vasiliy.project.entity.records.SoldRecord;
import com.vasiliy.project.entity.records.WrittenOffRecord;
import com.vasiliy.project.repository.InflowRecordRepository;
import com.vasiliy.project.repository.SoldRecordRepository;
import com.vasiliy.project.repository.WrittenOffRecordRepository;
import com.vasiliy.project.service.ReportService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final InflowRecordRepository inflowRecordRepository;
    private final SoldRecordRepository soldRecordRepository;
    private final WrittenOffRecordRepository writtenOffRecordRepository;

    private final ReportDataDTO reportDataDTO = new ReportDataDTO();


    @Override
    public ReportDataDTO getReportDTO(Long productId, Integer numberOfLastWeeks) {
        reportDataDTO.setLabels(new ArrayList<>());
        reportDataDTO.setOutflowValues(new ArrayList<>());

        collectOutflowValues(productId, numberOfLastWeeks);

        return reportDataDTO;
    }


    private void collectOutflowValues(Long productId, Integer numberOfLastWeeks) {
        int dayDifference;
        List<Integer> inflowValues = new ArrayList<>();
        List<Integer> soldValues = new ArrayList<>();
        List<Integer> writtenOffValues = new ArrayList<>();
        List<Integer> outflowValues = new ArrayList<>();


        // Определяем временные пределы, в которых собираются данные
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startDateTime = endDateTime.minusWeeks(numberOfLastWeeks).truncatedTo(ChronoUnit.DAYS);

        LocalDate startDate = startDateTime.toLocalDate();


        // Получаем записи о приходах конкретного товара в заданный период
        List<InflowRecord> inflowRecords = inflowRecordRepository.findAllByProductIdAndBetweenDates(productId, startDateTime, endDateTime);

        // Получаем записи о продажах конкретного товара в заданный период
        List<SoldRecord> soldRecords = soldRecordRepository.findAllByProductIdAndBetweenDates(productId, startDateTime, endDateTime);

        // Получаем записи о списаниях конкретного товара в заданный период
        List<WrittenOffRecord> writtenOffRecords = writtenOffRecordRepository.findAllByProductIdAndBetweenDates(productId, startDateTime, endDateTime);


        // Инициализируем 4 списка, заполненные нулями
        for (int i = 0; i < numberOfLastWeeks * 7; i++) {
            inflowValues.add(0);
            soldValues.add(0);
            writtenOffValues.add(0);
            outflowValues.add(0);
        }


        // Проходимся по записям о приходе и дополняем список значениями
        for (InflowRecord obj : inflowRecords) {
            dayDifference = (int) ChronoUnit.DAYS.between(startDate, obj.getWrittenAt().toLocalDate());

            inflowValues.set(dayDifference, inflowValues.get(dayDifference) + obj.getQuantity().intValue());
        }

        // Проходимся по записям о списаниях и дополняем список значениями
        for (WrittenOffRecord obj : writtenOffRecords) {
            dayDifference = (int) ChronoUnit.DAYS.between(startDate, obj.getWrittenOffAt().toLocalDate());

            soldValues.set(dayDifference, soldValues.get(dayDifference) + obj.getQuantity().intValue());
        }

        // Проходимся по записям о продажах и дополняем список значениями
        for (SoldRecord obj : soldRecords) {
            dayDifference = (int) ChronoUnit.DAYS.between(startDate, obj.getSoldAt().toLocalDate());

            writtenOffValues.set(dayDifference, writtenOffValues.get(dayDifference) + obj.getQuantity().intValue());
        }


        for (int i = 0; i < outflowValues.size(); i++) {
            if (i == 0) {
                outflowValues.set(0, inflowValues.get(0) - soldValues.get(0) - writtenOffValues.get(0));
            }

            else {
                // Переносим с прошлого дня
                outflowValues.set(i, outflowValues.get(i - 1));

                outflowValues.set(i, outflowValues.get(i) + inflowValues.get(i) - soldValues.get(i) - writtenOffValues.get(i));
            }
        }


        // Сразу записываем результаты в респонс
        reportDataDTO.setOutflowValues(outflowValues);


        // Генерируем labels
        LocalDate endDate = endDateTime.toLocalDate().minusDays(1);
        LocalDate currentDate = endDate.minusDays(outflowValues.size() - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        while (!currentDate.isAfter(endDate)) {
            reportDataDTO.getLabels().add(currentDate.format(formatter));
            currentDate = currentDate.plusDays(1);
        }
    }
}
