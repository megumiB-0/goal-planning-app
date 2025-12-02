package com.example.goalplanningapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.goalplanningapp.service.QualificationService;

@Controller
@RequestMapping("/qualifications")
public class QualificationController {
	
	private final QualificationService qualificationService;
	
	public QualificationController(QualificationService qualificationService) {
		this.qualificationService =qualificationService;
	}
	/*
	// 資格の詳細ページを表示する
	@GetMapping("/{id}")
	
	public String show(@PathVariable(name = "id")Integer id, RedirectAttributes redirectAttributes,Model model) {
		Optional<Qualification> optionalQualification = qualificationService.findQualificationById(id);
		
		if(optionalQualification.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage","該当する資格が見つかりません。");
			return "redirect:/qualifications";
		}
		
		Qualification qualification = optionalQualification.get();
		// GoalSettingFormに資格情報をセット
		GoalSettingForm form = new GoalSettingForm();
		form.setQualificationId(qualification.getId());
		form.setCustomQualificationName(qualification.getName());
		form.setCustomEstimatedMinutes(qualification.getEstimatedMinutes());
		model.addAttribute("goalSettingForm", form);
		
		// 目標設定画面へ
		return "goals/setting";
	}
*/
}
