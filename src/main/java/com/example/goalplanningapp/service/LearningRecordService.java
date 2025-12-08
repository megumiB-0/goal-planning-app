package com.example.goalplanningapp.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.example.goalplanningapp.dto.LearningRecordDTO;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.repository.LearningRecordRepository;

@Service
public class LearningRecordService {
	// DI
	private final LearningRecordRepository learningRecordRepository;
	private LearningRecordService(LearningRecordRepository learningRecordRepository) {
		this.learningRecordRepository = learningRecordRepository;
	}
	
	
	public void createRecord(LearningRecordDTO dto, User user) {
		//String →　　日付型へ
		LocalDate learningDay = LocalDate.parse(dto.getLearningDay());
		LocalTime startTime = LocalTime.parse(dto.getStartTime());
		LocalTime endTime = LocalTime.parse(dto.getEndTime());

		
	}
	


}
