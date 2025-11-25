package com.example.goalplanningapp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.form.QualificationRegisterForm;
import com.example.goalplanningapp.service.QualificationService;


@Controller
@RequestMapping("/admin/qualigications")
public class AdminQualificationController {
	private final QualificationService qualificationService;
	public AdminQualificationController(QualificationService qualificationService) {
		this.qualificationService = qualificationService;
	}
	@GetMapping
	public String index(Model model) {
		List<Qualification> qualifications = qualificationService.findAllQualifications();
		model.addAttribute("qualifications",qualifications);
		
		return "admin/qualifications/index";
	}
	
	
	// 資格情報登録のためフォームの入力項目とフォームクラスのフィールドをバインドする
	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("qualificationRegisterFoem", new QualificationRegisterForm());
		return "admin/qualifications/register";
	}
	
}
