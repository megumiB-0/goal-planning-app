package com.example.goalplanningapp.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.form.GoalSettingForm;
import com.example.goalplanningapp.service.QualificationService;

@Controller
@RequestMapping("/qualifications")
public class QualificationController {
	
	private final QualificationService qualificationService;
	
	public QualificationController(QualificationService qualificationService) {
		this.qualificationService =qualificationService;
	}
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
		form.setQualificationName(qualification.getQualificationName());
		form.setEstimatedTime(qualification.getEstimatedTime());
		model.addAttribute("goalSettingForm", form);
		
		// 目標設定画面へ
		return "goals/setting";
	}

}
