package com.example.goalplanningapp.service;

import org.springframework.stereotype.Service;

import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.form.GoalSettingForm;
import com.example.goalplanningapp.repository.GoalRepository;
import com.example.goalplanningapp.repository.QualificationRepository;

import jakarta.transaction.Transactional;

@Service
public class GoalService {
	//DI
	private final GoalRepository goalRepository;
	private final QualificationRepository qualificationRepository;
	
	public GoalService(GoalRepository goalRepository,
					   QualificationRepository qualificationRepository) {
		this.goalRepository = goalRepository;
		this.qualificationRepository = qualificationRepository;
	}
	
	
	@Transactional
	// フォームからデータベース保存
	public Goal saveGoalWithQualification(GoalSettingForm form) {
		Integer qualificationId = form.getQualificationId();
		// 手動入力の場合はQualificationを作成
		if("manual".equals(qualificationId) || qualificationId == null) {
			Qualification newQualification = new Qualification();
			newQualification.setName(form.getCustomQualificationName());
			// 必要学習時間を登録
			if(form.getCustomEstimatedHours() != null) {
				newQualification.setEstimatedMinutes(form.getCustomEstimatedHours() * 60);
			}
			// 新しい資格を保存
			newQualification = qualificationRepository.save(newQualification);
			qualificationId = newQualification.getId();
		}
		// 目標設定を作成	
		// 各フィールドをフォームからコピー
		Goal goal = new Goal();
		goal.setQualificationId(qualificationId);
		goal.setCustomQualificationName(form.getCustomQualificationName());
		goal.setEstimatedMinutes(form.getCustomEstimatedHours() != null ? form.getCustomEstimatedHours() * 60 : null);
		goal.setStartDate(form.getStartDate());
		goal.setGoalDate(form.getGoalDate());
	
	// DBにエンティティを保存
    return goalRepository.save(goal);
	
}
}