package com.example.goalplanningapp.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.goalplanningapp.dto.LearningTimeDTO;
import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.LearningPlan;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.repository.LearningPlanRepository;

import jakarta.transaction.Transactional;

@Service
public class LearningPlanService {
	//DI
	private final LearningPlanRepository learningPlanRepository;
	
	public LearningPlanService(LearningPlanRepository learningPlanRepository) {
		this.learningPlanRepository = learningPlanRepository;
	}
	
	//POST用
	public LearningPlan createPlan(LearningTimeDTO dto, User user,Goal goal) {
		// String →　日付型へ
		LocalDate planningDay = LocalDate.parse(dto.getDay());
		LocalTime startTime = LocalTime.parse(dto.getStartTime());
		LocalTime endTime = LocalTime.parse(dto.getEndTime());
		// 重複チェック
		if(learningPlanRepository.existsOverlap(user, planningDay, startTime, endTime)) {
			throw new IllegalStateException("この時間帯はすでに登録されています。");
		}
		// 過去の日付チェック
		validateNotPast(planningDay);
		
		//学習予定時間を分で計算
		int planningMinutes = calculateMinutes(startTime,endTime);
		
		LearningPlan plan = new LearningPlan();
		
		plan.setUser(user);
		plan.setGoal(goal);
		plan.setPlanningDay(planningDay);
		plan.setStartTime(startTime);
		plan.setEndTime(endTime);
		plan.setPlanningMinutes(planningMinutes);
		
		return learningPlanRepository.save(plan);
	}
	
	//GET用
	public List<LearningPlan> getPlans(User user){
		return learningPlanRepository.findByUser(user);
	}
	
	// PUT用
	@Transactional
	public void updatePlan(Integer id, LearningTimeDTO dto, User user) {
		LearningPlan plan = learningPlanRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Plan not found"));
		if(!plan.getUser().getId().equals(user.getId())) {
			throw new SecurityException("Not allowed");
		}
		// String →　日付型へ変換
		LocalDate planningDay = LocalDate.parse(dto.getDay());
		LocalTime startTime = LocalTime.parse(dto.getStartTime());
		LocalTime endTime = LocalTime.parse(dto.getEndTime());
		// 重複チェック（自分のIDは除外）
		if(learningPlanRepository.existsOverlapExcludingId(user, planningDay, startTime, endTime,id)) {
			throw new IllegalStateException("この時間帯はすでに登録されています。");
		}
		// 過去の日付チェック
		validateNotPast(planningDay);
		//学習予定時間を分で計算
		int planningMinutes = calculateMinutes(startTime,endTime);
		
		// 更新
		plan.setPlanningDay(planningDay);
		plan.setStartTime(startTime);
		plan.setEndTime(endTime);
		plan.setPlanningMinutes(planningMinutes);
		
		learningPlanRepository.save(plan);
	}
	// DELETE用
	public void deletePlan(Integer id, User user) {
		LearningPlan plan = learningPlanRepository.findById(id)
				.orElseThrow(()-> new IllegalArgumentException("Plan not found"));
		if(!plan.getUser().getId().equals(user.getId())) {
			throw new SecurityException("Not allowed");
		}
		//物理削除
		learningPlanRepository.delete(plan);
	}
	
	//共通メソッド（POSTとPUT）時間計算
	public int calculateMinutes(LocalTime startTime, LocalTime endTime) {
		int startTotalMinutes = startTime.getHour() * 60 + startTime.getMinute();
		int endTotalMinutes;
		// 0:00は24:00として扱う
		if(endTime.equals(LocalTime.MIDNIGHT)) {
			endTotalMinutes = 24 * 60; //24:00の代替
		}else {
			endTotalMinutes = endTime.getHour() * 60 + endTime.getMinute();
		}
		//経過時間（分）を計算
		return endTotalMinutes - startTotalMinutes;
		}
	//共通メソッド（POSTとPUT）過去の日付NG
	public void validateNotPast(LocalDate targetDate) {
		LocalDate today = LocalDate.now();
		if(targetDate.isBefore(today)) {
			throw new IllegalStateException("過去の日付には登録できません。");
		}
	}
}
