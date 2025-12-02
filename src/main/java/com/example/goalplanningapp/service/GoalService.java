package com.example.goalplanningapp.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.form.GoalSettingForm;
import com.example.goalplanningapp.repository.GoalRepository;
import com.example.goalplanningapp.repository.QualificationRepository;


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
		Qualification qualificationEntity;
		// 手動入力の場合はQualificationを作成
		if("manual".equals(form.getQualificationId())) {
			Qualification newQualification = new Qualification();
			newQualification.setName(form.getCustomQualificationName());
			// 必要学習時間を登録
			if(form.getCustomEstimatedHours() != null) {
				newQualification.setEstimatedMinutes(form.getCustomEstimatedHours() * 60);
			}
			// 新しい資格を保存
			qualificationEntity = qualificationRepository.save(newQualification);
		}else {
			//既存資格Idを取得
			Integer qualificationId = Integer.parseInt(form.getQualificationId());
			qualificationEntity = qualificationRepository.findById(qualificationId)
							  .orElseThrow(()-> new IllegalArgumentException("資格が見つかりません。"));
		}
		// 目標設定を作成	
		// 各フィールドをフォームからコピー
		Goal goal = new Goal();
		goal.setQualification(qualificationEntity);
		
		LocalDate startDate = form.getStartDate();
		LocalDate goalDate = form.getGoalDate();

		goal.setStartDate(startDate);
		goal.setGoalDate(goalDate);
	
	// DBにエンティティを保存
    return goalRepository.save(goal);
	
}
}