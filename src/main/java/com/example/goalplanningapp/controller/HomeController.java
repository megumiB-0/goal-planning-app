package com.example.goalplanningapp.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.repository.GoalRepository;
import com.example.goalplanningapp.security.UserDetailsImpl;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	//DI
	@Autowired
	private GoalRepository goalRepository;

	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	@GetMapping("/home")
	public String home(Model model,
					   @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
					   HttpSession session){
		// ログイン直後のメッセージ取得
		String loginMessege = (String) session.getAttribute("loginMessage");

		if(loginMessege != null) {
			model.addAttribute("loginMessage",loginMessege);
			session.removeAttribute("loginMessage"); // 一度表示したら削除
		}
		
		// ログインユーザー取得
		User loginUser = userDetailsImpl.getUser();
		// 有効な目標を取得
		Optional<Goal> activeGoal = goalRepository.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(loginUser);

		// hasGoal設定（ビューの場合分け用）
		model.addAttribute("hasGoal",activeGoal.isPresent());
		// 目標を渡す準備
		if(activeGoal.isPresent()) {		
			Goal goal = activeGoal.get();
			//メッセージに必要な値を取得
			LocalDate goalDate = goal.getGoalDate();
			Double estimatedHours = (goal.getQualification().getEstimatedMinutes() / 60);
			String qualigicationName = goal.getQualification().getName();
			
			String message =  goalDate + "までに" +
							  estimatedHours + "時間学習して" +
							  qualigicationName + "を取得する!";
			//モデルに渡す
			model.addAttribute("message",message);
			
			// グラフに必要なデータを取得
			LocalDate startDate = goal.getStartDate();
			
			List<Map<String,Object>> studyData =new ArrayList<>();	
			LocalDate date = startDate;
			// 実績ライン用
			while (!date.isAfter(goalDate)) {
				Map<String, Object> rec = new HashMap<>();
				rec.put("x", date.toString());
				rec.put("y", 0);     // 今は仮データをセットしている（要修正）
				studyData.add(rec);
				date = date.plusDays(1);	
			}
			model.addAttribute("studyData",studyData);
			//目標ライン用
			model.addAttribute("startDate",startDate.toString());
			model.addAttribute("goalDate",goalDate);
			model.addAttribute("estimatedHours",estimatedHours);
		}
		
		return "home";
	}

}
