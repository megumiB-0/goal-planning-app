package com.example.goalplanningapp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.form.GoalSettingForm;
import com.example.goalplanningapp.service.QualificationService;


@Controller
@RequestMapping("/goals")
public class GoalSettingController {
	private final QualificationService qualificationService;
	public GoalSettingController(QualificationService qualificationService) {
		this.qualificationService = qualificationService;
	}
	
	@GetMapping("/setting")
	public String showSettingForm(Model model) {
		model.addAttribute("goalSettingForm", new GoalSettingForm());
		
		// 資格一覧
		List<Qualification> qualifications = qualificationService.findAllQualifications();
		model.addAttribute("qualifications",qualifications);
		
		return "user/goals/setting";
		
	}

}
