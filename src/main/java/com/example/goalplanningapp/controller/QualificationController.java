package com.example.goalplanningapp.controller;

import org.springframework.stereotype.Controller;

import com.example.goalplanningapp.service.AdminQualificationService;
import com.example.goalplanningapp.service.QualificationService;

@Controller
public class QualificationController {
	
	private final AdminQualificationService adminQualificationService;
	private final QualificationService qualificationService;
	
	public QualificationController(AdminQualificationService adminQualificationService,
								   QualificationService qualificationService) {
		this.adminQualificationService = adminQualificationService;
		this.qualificationService = qualificationService;
	}
	

	
}
