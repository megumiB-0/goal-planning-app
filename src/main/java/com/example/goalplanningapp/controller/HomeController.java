package com.example.goalplanningapp.controller;

import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.repository.GoalRepository;
import com.example.goalplanningapp.security.UserDetailsImpl;

@Controller
public class HomeController {
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	@GetMapping("/home")
	public String home(Model model,
					   GoalRepository goalRepository,
					   @AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
		// ログインユーザー取得
		User loginUser = userDetailsImpl.getUser();
		// 有効な目標を取得
		Optional<Goal> activeGoal = goalRepository.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(loginUser);

		// hasGoal設定（ビューに渡す準備）
		model.addAttribute("hasGoal",activeGoal.isPresent());
		// 目標を渡す準備
		if(activeGoal.isPresent()) {		
			Goal goal = activeGoal.get();
			model.addAttribute("goal",goal);
		

			//
		}
		
		return "home";
	}

}
