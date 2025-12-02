package com.example.goalplanningapp.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
	// 指定したidを持つ資格を取得する
	public Optional<Qualification> findQualificationById(Integer id){
		return qualificationRepository.findById(id);
	}

	// 資格を登録する
	@Transactional
	public Qualification save(Qualification qualification){
		return qualificationRepository.save(qualification);
	}

}
