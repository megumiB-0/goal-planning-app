package com.example.goalplanningapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.goalplanningapp.entity.LearningRecord;

public interface LearningRecordRepository extends JpaRepository<LearningRecord, Integer> {

}
