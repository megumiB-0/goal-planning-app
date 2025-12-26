package com.example.goalplanningapp.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.entity.User;


public interface QualificationRepository extends JpaRepository<Qualification, Integer>{
	// 管理者資格（全体マスタ）
	Page<Qualification> findByUserIsNullAndDeletedAtIsNull(Pageable pageable);
	
	// 会員資格
	List<Qualification> findByUserAndDeletedAtIsNull(User user);
	
	// 資格名検索（部分一致・管理者資格のみ）
	Page<Qualification> findByUserIsNullAndDeletedAtIsNullAndNameContaining(String name, Pageable pageable);

}
