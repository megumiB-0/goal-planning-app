package com.example.goalplanningapp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.form.GoalSettingForm;
import com.example.goalplanningapp.service.GoalCalculationService;
import com.example.goalplanningapp.service.GoalService;
import com.example.goalplanningapp.service.QualificationService;


@Controller
@RequestMapping("/goals")
public class GoalSettingController {
	private final QualificationService qualificationService;
	private final GoalCalculationService goalCalculationService;
	private final GoalService goalService;
	public GoalSettingController(QualificationService qualificationService,
								 GoalCalculationService goalCalculationService,
								 GoalService goalService) {
		this.qualificationService = qualificationService;
		this.goalCalculationService = goalCalculationService;
		this.goalService = goalService;
	}

	
	@GetMapping("/setting")
	public String showSettingForm(Model model) {
		model.addAttribute("goalSettingForm", new GoalSettingForm());
		
		// 資格一覧
		List<Qualification> qualifications = qualificationService.findAllQualifications();
		model.addAttribute("qualifications",qualifications);
		
		return "user/goals/setting";
		
	}
	
	// 目標登録
	@PostMapping("/confirm")
	public String create(@ModelAttribute @Validated GoalSettingForm form,
						 BindingResult bindingResult,
						 RedirectAttributes redirectAttributes,
						 Model model)
	{
		if (bindingResult.hasErrors()) {
			model.addAttribute("goalSettingForm", form);
			model.addAttribute("showConfirm",false);
			return "user/goals/setting";
		}
		Qualification qualification;
		Integer qualificationIdInt = null;
		
		// "manual"を判定してnull またはIntegerに変換
		if("manual".equals(form.getQualificationId()) || form.getQualificationId() == null || form.getQualificationId().isEmpty()) {
			qualification = new Qualification();
			qualification.setName(form.getCustomQualificationName());
			
			//時間　→　分　に変換して登録
			if(form.getCustomEstimatedHours() !=null) {
				Double minutes = (Double)(form.getCustomEstimatedHours() * 60);
				qualification.setEstimatedMinutes(minutes);
			}			
			qualificationService.save(qualification);
			qualificationIdInt = qualification.getId();
		}else {
			// 既存資格の ID を Integer に変換
			try {
				qualificationIdInt = Integer.parseInt(form.getQualificationId());
			}catch(NumberFormatException e) {
				bindingResult.rejectValue("qualificationId", "invalid", "資格IDが不正です。");
				model.addAttribute("goalSettingForm",form);
				model.addAttribute("showConfirm",false);
				return "user/goals/setting";
			}
			qualification= qualificationService.findQualificationById(qualificationIdInt)
						   .orElseThrow(() -> new IllegalArgumentException("資格が見つかりません。")); 
		}
		
		
		// ここから計算処理やモデルへのセット
		LocalDate start = LocalDate.parse(form.getStartDate());
		LocalDate goal = LocalDate.parse(form.getGoalDate());
	
		try {
			double hoursPerDay = goalCalculationService.calculateHoursPerDay(
					qualification.getEstimatedMinutes(),start,goal);
			double hoursPerWeek = goalCalculationService.calculateHoursPerWeek(hoursPerDay); 
			model.addAttribute("qualification",qualification);
			model.addAttribute("hoursPerDay",hoursPerDay);
			model.addAttribute("hoursPerWeek",hoursPerWeek);
			model.addAttribute("showConfirm", true);
			
		}catch(IllegalArgumentException e) {
			bindingResult.reject("calculateError",e.getMessage());
			model.addAttribute("goalSettingForm",form);
			model.addAttribute("showConfirm",false);
		}
		return "user/goals/setting"; //確認画面へ
	}
	
	
	@PostMapping("/save")
	public String saveGoal(@ModelAttribute GoalSettingForm form) {
		goalService.saveGoalWithQualification(form); //DBに保存
		return "redirect:/home";
	}
	
}

