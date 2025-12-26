package com.example.goalplanningapp.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.service.AdminQualificationService;
import com.example.goalplanningapp.service.QualificationService;



@Controller
public class AdminHomeController {

		private final QualificationService qualificationService;
		private final AdminQualificationService adminQualificationService;
		public  AdminHomeController(QualificationService qualificationService,
											AdminQualificationService adminQualificationService) {
			this.qualificationService = qualificationService;
			this.adminQualificationService = adminQualificationService;
		}

	
	@GetMapping("/admin/home")
	public String home(@RequestParam(defaultValue = "0") int page, Model model) {
	    int pageSize = 10;
	    Page<Qualification> qualificationsPage =
	            adminQualificationService.findAdminQualifications(null, page, pageSize);

	    model.addAttribute("qualificationsPage", qualificationsPage);
	    model.addAttribute("keyword", null);

	    return "admin/qualifications/list"; // list.html を再利用
	}
}
