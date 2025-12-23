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
		model.addAttribute("estimatedPerDay", estimatedPerDay);
		model.addAttribute("estimatedPerWeek", estimatedPerWeek);
		model.addAttribute("qualigicationName", qualigicationName);
		
		
		return "user/plans/index";
	}

}
