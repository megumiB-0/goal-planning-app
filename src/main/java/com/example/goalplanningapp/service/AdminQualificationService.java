package com.example.goalplanningapp.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.repository.QualificationRepository;

@Service
public class AdminQualificationService {
	// DI
	private final QualificationRepository qualificationRepository;
	
	public AdminQualificationService(QualificationRepository qualificationRepository) {
		this.qualificationRepository = qualificationRepository;
	}
	
	// 管理者資格一覧
	public Page<Qualification> findAdminQualifications(int page, int size){
		Pageable pageable = PageRequest.of(page, size);
		return qualificationRepository.findByUserIsNullAndDeletedAtIsNull(pageable);
	}
	
	// 管理者が登録
	public Qualification saveByAdmin(Qualification qualification) {
		qualification.setUser(null); // 管理者は UserId は null
	    // 一旦保存して ID を取得
	    qualification = qualificationRepository.save(qualification);

	    // root_qualification_id を ID と同じに設定
	    if (qualification.getRootQualificationId() == null) {
	        qualification.setRootQualificationId(qualification.getId());
	        qualification = qualificationRepository.save(qualification); // 更新
	    }
	    return qualification;
	}
	
	// 管理者が論理削除
	public void delete(Integer id) {
		Qualification q = qualificationRepository.findById(id).orElseThrow();
		q.setDeletedAt(LocalDate.now());
		qualificationRepository.save(q);
	}
	
	// 検索一覧表示
	public Page<Qualification> findAdminQualifications(String keyword, int page, int size){
		Pageable pageable = PageRequest.of(page, size);
		
		if(keyword == null || keyword.isBlank()) {
			return qualificationRepository.findByUserIsNullAndDeletedAtIsNull(pageable);
		}else {
		return qualificationRepository.findByUserIsNullAndDeletedAtIsNullAndNameContaining(keyword, pageable);
		}
	}

}
