package com.vasiliy.project.repository;

import com.vasiliy.project.entity.records.SoldRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SoldRecordRepository extends JpaRepository<SoldRecord, Long> {

  @Query("SELECT sr FROM SoldRecord sr JOIN sr.storageProduct sp WHERE sp.product.id = :productId AND sr.soldAt BETWEEN :startDate AND :endDate ORDER BY sr.soldAt ASC")
  List<SoldRecord> findAllByProductIdAndBetweenDates(
      @Param("productId") Long productId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT MIN(sr.soldAt) FROM SoldRecord sr WHERE sr.storageProduct.product.id = :productId")
  LocalDateTime findEarliestDateByProductId(@Param("productId") Long productId);
}
