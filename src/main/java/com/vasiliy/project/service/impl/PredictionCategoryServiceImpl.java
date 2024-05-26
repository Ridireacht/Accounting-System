package com.vasiliy.project.service.impl;

import com.vasiliy.project.dto.info.PredictionCategoryDataDTO;
import com.vasiliy.project.entity.info.Product;
import com.vasiliy.project.entity.records.SoldRecord;
import com.vasiliy.project.entity.records.WrittenOffRecord;
import com.vasiliy.project.repository.ProductRepository;
import com.vasiliy.project.repository.SoldRecordRepository;
import com.vasiliy.project.repository.WrittenOffRecordRepository;
import com.vasiliy.project.service.PredictionCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionCategoryServiceImpl implements PredictionCategoryService {
    private final SoldRecordRepository soldRecordRepository;
    private final WrittenOffRecordRepository writtenOffRecordRepository;
    private final ProductRepository productRepository;

    private final PredictionCategoryDataDTO predictionCategoryDataDTO = new PredictionCategoryDataDTO();


    @Override
    public List<Integer> collectOutflowValueByProduct(Long productId, Integer numberOfLastWeeks) {
        int dayDifference;
        List<Integer> outflowValues = new ArrayList<>();


        // Определяем временные пределы, в которых собираются данные
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startDateTime = endDateTime.minusWeeks(numberOfLastWeeks).truncatedTo(ChronoUnit.DAYS);

        LocalDate startDate = startDateTime.toLocalDate();


        // Получаем записи о продажах конкретного товара в заданный период
        List<SoldRecord> soldRecords = soldRecordRepository.findAllByProductIdAndBetweenDates(productId, startDateTime, endDateTime);


        // Получаем записи о списаниях конкретного товара в заданный период
        List<WrittenOffRecord> writtenOffRecords = writtenOffRecordRepository.findAllByProductIdAndBetweenDates(productId, startDateTime, endDateTime);


        // Инициализируем список, заполненный нулями
        for (int i = 0; i < numberOfLastWeeks * 7; i++) {
            outflowValues.add(0);
        }


        // Проходимся по записям о продажах и дополняем список значениями
        for (SoldRecord obj : soldRecords) {
            dayDifference = (int) ChronoUnit.DAYS.between(startDate, obj.getSoldAt().toLocalDate());

            outflowValues.set(dayDifference, outflowValues.get(dayDifference) + obj.getQuantity().intValue());
        }

        // Проходимся по записям о списаниях и дополняем список значениями
        for (WrittenOffRecord obj : writtenOffRecords) {
            dayDifference = (int) ChronoUnit.DAYS.between(startDate, obj.getWrittenOffAt().toLocalDate());

            outflowValues.set(dayDifference, outflowValues.get(dayDifference) + obj.getQuantity().intValue());
        }


        return outflowValues;
    }

    @Override
    public List<Integer> collectOutflowValues(Long categoryId, Integer numberOfLastWeeks) {

        LocalDateTime endDateTime = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS);


        // Собираем препараты, входящие в фармакологическую группу
        List<Product> products = productRepository.findProductsByCategoryId(categoryId);


        // Собираем ID препаратов, входящих в фармакологическую группу
        List<Long> productIds = new ArrayList<>();

        for(Product product : products) {
            productIds.add(product.getId());
        }


        // Собираем и сохраняем названия препаратов
        predictionCategoryDataDTO.setProductNames(new ArrayList<>());

        for (Long id : productIds) {
            predictionCategoryDataDTO.getProductNames().add(productRepository.findNameById(id));
        }


        // Собираем информацию по расходу препаратов
        List<List<Integer>> outflowProductValueLists = new ArrayList<>();

        for (Long id : productIds) {
            outflowProductValueLists.add(collectOutflowValueByProduct(id, numberOfLastWeeks));
        }


        // Сохраняем эти данные в DTO
        predictionCategoryDataDTO.setOutflowValuesLists(outflowProductValueLists);


        // Собираем единый список по расходу фармакологической группы
        List<Integer> outflowValues = new ArrayList<>();

        for (List<Integer> innerList : outflowProductValueLists) {
            for (int i = 0; i < innerList.size(); i++) {
                // Если суммарный список еще не содержит элемент по текущему индексу, добавляем его
                if (outflowValues.size() <= i) {
                    outflowValues.add(innerList.get(i));
                } else {
                    // Иначе прибавляем значение к существующему элементу
                    outflowValues.set(i, outflowValues.get(i) + innerList.get(i));
                }
            }
        }


        // Высчитываем labels для вывода графика и записываем их в респонс
        LocalDate endDate = endDateTime.toLocalDate().minusDays(1);
        LocalDate currentDate = endDate.minusDays(outflowValues.size() - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        while (!currentDate.isAfter(endDate)) {
            predictionCategoryDataDTO.getLabels().add(currentDate.format(formatter));
            currentDate = currentDate.plusDays(1);
        }


        return outflowValues;
    }

    @Override
    public PredictionCategoryDataDTO getPredictionDTO(Long categoryId, Integer numberOfLastWeeks) {
        int currentOutflowValue;
        List<Integer> weekOutflowValues = new ArrayList<>();
        List<Integer> monthOutflowValues = new ArrayList<>();

        predictionCategoryDataDTO.setLabels(new ArrayList<>());
        predictionCategoryDataDTO.setOutflowValuesLists(new ArrayList<>());
        predictionCategoryDataDTO.setNextWeekOutflowPredictions(new ArrayList<>());
        predictionCategoryDataDTO.setNextMonthOutflowPredictions(new ArrayList<>());


        // Собираем данные о расходе товара за последние numberOfLastWeeks недель
        List<Integer> outflowValues = collectOutflowValues(categoryId, numberOfLastWeeks);


        // Если список пустой, то и анализировать нечего
        if (outflowValues.isEmpty()) {
            predictionCategoryDataDTO.getNextWeekOutflowPredictions().add(0.0);
            predictionCategoryDataDTO.getNextMonthOutflowPredictions().add(0.0);

            predictionCategoryDataDTO.setLabels(new ArrayList<>());
            predictionCategoryDataDTO.setProductNames(new ArrayList<>());
            predictionCategoryDataDTO.setOutflowValuesLists(new ArrayList<>());

            return predictionCategoryDataDTO;
        }


        // Собираем список расхода товаров по неделям.
        // Проход по неделям
        for (int i = 0; i < outflowValues.size() / 7; i++) {
            currentOutflowValue = 0;

            // Проход по дням в неделе
            for (int j = 0; j < 7; j++) {
                currentOutflowValue += outflowValues.get(i * 7 + j);
            }

            weekOutflowValues.add(currentOutflowValue);
        }


        // Проводим прогнозирование фармакологической группы на следующую неделю
        predictionCategoryDataDTO.getNextWeekOutflowPredictions().add(getNextWeekPrediction(weekOutflowValues));


        // Собираем список расхода товаров по месяцам.
        // Проход по месяцам
        for (int i = 0; i < weekOutflowValues.size() / 4; i++) {
            currentOutflowValue = 0;

            // Проход по неделям в месяце
            for (int j = 0; j < 4; j++) {
                currentOutflowValue += weekOutflowValues.get(i * 4 + j);
            }

            monthOutflowValues.add(currentOutflowValue);
        }


        // Проводим прогнозирование фармакологической группы на следующий месяц (если накопилось данных хотя бы на месяц)
        if (!monthOutflowValues.isEmpty()) {
            predictionCategoryDataDTO.getNextMonthOutflowPredictions().add(getNextMonthPrediction(monthOutflowValues));
        } else {
            predictionCategoryDataDTO.getNextMonthOutflowPredictions().add(0.0);
        }


        // Проводим прогнозирование по каждому препарату
        for(List<Integer> outflowValuesList : predictionCategoryDataDTO.getOutflowValuesLists()) {
            List<Integer> weekOutflowValuesProduct = new ArrayList<>();
            List<Integer> monthOutflowValuesProduct = new ArrayList<>();


            // Собираем список расхода препарата по неделям.
            // Проход по неделям
            for (int i = 0; i < outflowValuesList.size() / 7; i++) {
                currentOutflowValue = 0;

                // Проход по дням в неделе
                for (int j = 0; j < 7; j++) {
                    currentOutflowValue += outflowValuesList.get(i * 7 + j);
                }

                weekOutflowValuesProduct.add(currentOutflowValue);
            }

            // Делаем прогноз препарата на следующую неделю
            predictionCategoryDataDTO.getNextWeekOutflowPredictions().add(getNextWeekPrediction(weekOutflowValuesProduct));


            // Собираем список расхода препарата по месяцам.
            // Проход по месяцам
            for (int i = 0; i < weekOutflowValuesProduct.size() / 4; i++) {
                currentOutflowValue = 0;

                // Проход по неделям в месяце
                for (int j = 0; j < 4; j++) {
                    currentOutflowValue += weekOutflowValuesProduct.get(i * 4 + j);
                }

                monthOutflowValuesProduct.add(currentOutflowValue);
            }


            // Проводим прогнозирование препарата на следующий месяц (если накопилось данных хотя бы на месяц)
            if (!monthOutflowValuesProduct.isEmpty()) {
                predictionCategoryDataDTO.getNextMonthOutflowPredictions().add(getNextMonthPrediction(monthOutflowValuesProduct));
            } else {
                predictionCategoryDataDTO.getNextMonthOutflowPredictions().add(0.0);
            }
        }


        return predictionCategoryDataDTO;
    }

    @Override
    public Double getNextWeekPrediction(List<Integer> weekOutflowValues) {
        int outflowValue;


        // Параметр сглаживания (чем выше, тем больший вес имеют более новые значения, и тем меньший - существующий прогноз)
        double alpha = 0.3;

        // Вычисляем начальное значение прогноза
        double forecast = weekOutflowValues.get(0);


        // Применяем метод экспоненциального сглаживания для прогнозирования на следующую неделю
        for (int i = 1; i < weekOutflowValues.size(); i++) {
            outflowValue = weekOutflowValues.get(i);
            forecast = alpha * outflowValue + (1 - alpha) * forecast;
        }


        // Проводим округление до целого значения в большую сторону
        return forecast;
    }

    @Override
    public Double getNextMonthPrediction(List<Integer> monthOutflowValues) {
        int outflowValue;


        // Параметр сглаживания (чем выше, тем больший вес имеют более новые значения, и тем меньший - существующий прогноз)
        double alpha = 0.3;

        // Вычисляем начальное значение прогноза
        double forecast = monthOutflowValues.get(0);


        // Применяем метод экспоненциального сглаживания для прогнозирования на следующий месяц
        for (int i = 1; i < monthOutflowValues.size(); i++) {
            outflowValue = monthOutflowValues.get(i);
            forecast = alpha * outflowValue + (1 - alpha) * forecast;
        }


        // Проводим округление до целого значения в большую сторону
        return forecast;
    }
}
