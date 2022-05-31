package com.example.demo.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GpuRecordRepository extends JpaRepository<GpuRecord, Long> {

    Optional<GpuRecord> findByUrl(String url);
}
