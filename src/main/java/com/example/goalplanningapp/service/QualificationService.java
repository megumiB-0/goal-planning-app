package com.example.goalplanningapp.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.repository.QualificationRepository;

@Service
public class QualificationService {
	private final QualificationRepository qualificationRepository;
	
	public QualificationService(QualificationRepository qualificationRepository) {
			this.qualificationRepository = qualificationRepository;
	}
	
	// 全ての資格を取得する
	public List<Qualification> findAllQualifications(){
		return qualificationRepository.findAll();
	}

}
