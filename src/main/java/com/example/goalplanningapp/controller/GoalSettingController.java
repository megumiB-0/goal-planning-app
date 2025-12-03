package com.example.goalplanningapp.controller;


import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.form.GoalSettingForm;
import com.example.goalplanningapp.security.UserDetailsImpl;
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
						 Model model)
	{

		
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("goalSettingForm", form);
			model.addAttribute("showConfirm",false);
			return "user/goals/setting";
		}

		Qualification qualification;
		
		// 資格ID取得
		try {		
		// 手動入力かどうか
		if(form.getQualificationId() != null && form.getQualificationId() == -1) {
			qualification = new Qualification();
			qualification.setName(form.getCustomQualificationName());
			
			//時間　→　分　に変換して登録
			if(form.getCustomEstimatedHours() !=null) {
				qualification.setEstimatedMinutes(form.getCustomEstimatedHours() * 60);
			}
			/*
			qualificationService.save(qualification);
			*/
		}else {
			
			qualification= qualificationService.findQualificationById(form.getQualificationId())
						   .orElseThrow(() -> new IllegalArgumentException("資格が見つかりません。")); 
		}
		//フォームから取得したデータ
		LocalDate start =form.getStartDate(); // 開始日
		LocalDate goal = form.getGoalDate(); //終了日

		
		// ここから画面表示用の計算処理やモデルへのセット
			
			double hoursPerDay = goalCalculationService.calculateHoursPerDay(
			qualification.getEstimatedMinutes(),start,goal);
			double hoursPerWeek = goalCalculationService.calculateHoursPerWeek(hoursPerDay); 

			model.addAttribute("qualificationName",qualification.getName());
			model.addAttribute("hoursPerDay",hoursPerDay);
			model.addAttribute("hoursPerWeek",hoursPerWeek);
			model.addAttribute("showConfirm", true);
			
		}catch(Exception e) {
			bindingResult.reject("calculateError",e.getMessage());
			model.addAttribute("goalSettingForm",form);
			model.addAttribute("showConfirm",false);
		}
		return "user/goals/setting"; //確認画面へ
	}
	
	
	@PostMapping("/save")
	public String saveGoal(@ModelAttribute GoalSettingForm form,
						   @AuthenticationPrincipal UserDetailsImpl userDetails,
						   RedirectAttributes redirectAttributes) {
		//ログインユーザーIDをセット
		User loginUser = userDetails.getUser();
		// goal登録用のform,loginUserデータをメソッドに渡す
		try {
			goalService.saveGoalWithQualification(form,loginUser); 
			//ホームに登録成功メッセージを表示する
			redirectAttributes.addFlashAttribute("goalMessage","目標登録に成功しました！");
			}catch(IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage",e.getMessage());
			return "redirect:/home";
		}
		
		return "redirect:/home";
	}
	
}

