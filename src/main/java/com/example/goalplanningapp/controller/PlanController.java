package com.example.goalplanningapp.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.security.UserDetailsImpl;
import com.example.goalplanningapp.service.GoalService;
import com.example.goalplanningapp.service.LearningRecordService;

@Controller
@RequestMapping("/plans")
public class PlanController {
	// DI
	private final LearningRecordService learningRecordService;
	private final GoalService goalService;
	public PlanController(LearningRecordService learningRecordService,GoalService goalService) {
		this.learningRecordService = learningRecordService;
		this.goalService = goalService;
	}
	
	//index
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		// ログインユーザーの取得
		User user = userDetailsImpl.getUser();
		Goal goal;
		// ゴールの取得
		try {
			goal = goalService.getCurrentGoal(user);
		}catch(IllegalStateException e) {
			model.addAttribute("errorMessage",e.getMessage());
			model.addAttribute("hasGoal",false);
			return "user/plans/index";
		}

		model.addAttribute("hasGoal",true);
		// 有効な目標を取得
		String qualigicationName = goal.getQualification().getName();
		//　1日当たりの必要学習時間を表示(分)（現時点での）
		Double estimatedPerDay = learningRecordService.getEstimatedPerDay(user, goal);
		// 週間目標時間を表示(分)
		Double estimatedPerWeek = estimatedPerDay * 7;
		// 合計学習時間
		Long todaysCumulativeHours = learningRecordService.getTodaysCumulative(user) / 60;
		model.addAttribute("todaysCumulativeHours", todaysCumulativeHours);
		//右２段目　（残りの学習時間）
		Long remainingHours = learningRecordService.getTodaysRemaining(user,goal);
		model.addAttribute("remainingHours", remainingHours);
		// 残りの学習必要時間
		Double hours = learningRecordService.getEstimatedPerDay(user, goal) / 60.0;
		Double estimatedPerDayHours = Math.round(hours * 10) / 10.0;
		Double hoursPerWeek = estimatedPerDay * 7;
		Double estimatedPerWeekHours = Math.round(hoursPerWeek * 10) / 10.0;

		model.addAttribute("estimatedperday", estimatedPerDay);
		model.addAttribute("estimatedperWeek", estimatedPerWeek);
		
		model.addAttribute("estimatedPerDay", estimatedPerDay);
		model.addAttribute("estimatedPerWeek", estimatedPerWeek);
		model.addAttribute("qualigicationName", qualigicationName);
		model.addAttribute("todaysCumulativeHours", todaysCumulativeHours);
		model.addAttribute("remainingHours", remainingHours);
		
		return "user/plans/index";
	}

}
