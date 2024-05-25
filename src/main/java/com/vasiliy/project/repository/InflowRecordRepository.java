package com.vasiliy.project.repository;

import com.vasiliy.project.entity.records.InflowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InflowRecordRepository extends JpaRepository<InflowRecord, Long> {

    @Query("SELECT ir FROM InflowRecord ir JOIN ir.storageProduct sp WHERE sp.product.id = :productId AND ir.writtenAt BETWEEN :startDate AND :endDate ORDER BY ir.writtenAt ASC")
    List<InflowRecord> findAllByProductIdAndBetweenDates(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT MIN(ir.writtenAt) FROM InflowRecord ir WHERE ir.storageProduct.product.id = :productId")
    LocalDateTime findEarliestDateByProductId(@Param("productId") Long productId);
}
