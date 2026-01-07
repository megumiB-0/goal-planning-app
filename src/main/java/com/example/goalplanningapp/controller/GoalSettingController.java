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

import com.example.goalplanningapp.entity.Goal;
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
	
	// 目標トップページ
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userDetailsImpl.getUser();
		
		boolean isEdit = goalService.hasActiveGoal(user);
		model.addAttribute("isEdit", isEdit); //修正(true)新規（false）
		
		try {
			Goal currentGoal = goalService.getCurrentGoal(user);
		// 有効な目標がある → 表示
			model.addAttribute("goal", currentGoal);
			return "user/goals/index";
		}catch (IllegalStateException e) {
		
		// 有効な目標がない → 新規登録
			return "redirect:/goals/setting";
		}

		
		
	}
	
	// 新規登録
	@GetMapping("/setting")
	public String showSettingForm(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
								  Model model,
								  RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		boolean hasActiveGoal = goalService.hasActiveGoal(user);
		if(hasActiveGoal) {
			redirectAttributes.addFlashAttribute("errorMessage","すでに有効な目標があります。修正してください。");
			return "redirect:/goals";
		}
		
		GoalSettingForm form = new GoalSettingForm();
		form.setStartDate(LocalDate.now()); // 登録日＝今日
		model.addAttribute("goalSettingForm", form);
		
		boolean isEdit = goalService.hasActiveGoal(user);
		model.addAttribute("isEdit", isEdit); //新規(false)
		
		// 資格一覧
		List<Qualification> qualifications = qualificationService.findSelectableQualifications(user);
		model.addAttribute("qualifications",qualifications);
		
		return "user/goals/setting";
		
	}
	// 修正
	@GetMapping("/edit")
	public String showEditForm(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
							   Model model) {
		User user = userDetailsImpl.getUser();
		Goal goal = goalService.getCurrentGoal(user);
		if(goal == null) {
			return "redirect:/goals/setting";
		}
		
		GoalSettingForm form = new GoalSettingForm();
		form.setQualificationId(-1);
		form.setCustomQualificationName(goal.getQualification().getName());
		form.setCustomEstimatedHours(goal.getQualification().getEstimatedMinutes() / 60);
		form.setStartDate(goal.getStartDate());
		form.setGoalDate(goal.getGoalDate());
		

		boolean isEdit = goalService.hasActiveGoal(user);
		model.addAttribute("isEdit", isEdit); //修正(true)
		model.addAttribute("goalSettingForm", form);
		model.addAttribute("isCustomQualification",true);
		return "user/goals/setting";
	}
	
	// 目標登録
	@PostMapping("/confirm")
	public String create(@ModelAttribute @Validated GoalSettingForm form,
						 @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						 BindingResult bindingResult,
						 Model model)
	{
		User user = userDetailsImpl.getUser();
		List<Qualification> qualifications = qualificationService.findSelectableQualifications(user);
		model.addAttribute("qualifications", qualifications);
		// 手動入力の資格名チェック
		if(form.getQualificationId() != null && form.getQualificationId() == -1) {
			if(form.getCustomQualificationName() == null || form.getCustomQualificationName().trim().isEmpty()) {
				bindingResult.rejectValue(
					"customQualificationName",
					"NotBlank",
					"資格名を入力してください。"
					);
			}
			if(form.getCustomEstimatedHours() == null) {
				bindingResult.rejectValue(
					"customEstimatedHours",
					"NotNull",
					"必要時間を入力してください。"
					);
			}			
		}
		// バリデーションエラーがあればフォームに返す
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
		
		boolean isEdit = goalService.hasActiveGoal(user);
		model.addAttribute("isEdit", isEdit); //新規(false)
		
		return "user/goals/setting"; //確認画面へ
	}
	
	
	@PostMapping("/save")
	public String saveGoal(@ModelAttribute GoalSettingForm form,
						   @AuthenticationPrincipal UserDetailsImpl userDetails,
						   RedirectAttributes redirectAttributes) {
		//ログインユーザーをセット
		User user = userDetails.getUser();
		// goal登録用のform,loginUserデータをメソッドに渡す
		try {
			if(goalService.hasActiveGoal(user)) {
				// 修正
				goalService.updateCurrentGoal(form, user); 
				//ホームに登録成功メッセージを表示する
				redirectAttributes.addFlashAttribute("successMessage","目標を修正しました！");
			}else {
				// 新規登録
				goalService.saveGoalWithQualification(form,user); 
				//ホームに登録成功メッセージを表示する
				redirectAttributes.addFlashAttribute("successMessage","目標登録に成功しました！");
			}
		}catch(IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage",e.getMessage());
		}
		return "redirect:/home";
	}
	
	// 修正保存
	@PostMapping("/update")
	public String update(@Validated @ModelAttribute GoalSettingForm form,
						 BindingResult bindingResult,
						 @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						 RedirectAttributes redirectAttributes) {
		if(bindingResult.hasErrors()) {
			return "user/goals/edit";
		}
		User user = userDetailsImpl.getUser();
		goalService.updateGoal(form, user);
		
		redirectAttributes.addFlashAttribute("successMessage","目標を修正しました");
		return "redirect:/goals";
	}
	
	//今の目標を終了
	@PostMapping("/finish")
	public String finish(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						 RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
//		Integer finishedRootId = goalService.finishCurrentGoal(user);
//		redirectAttributes.addFlashAttribute("finishedRootId", finishedRootId);
		goalService.finishCurrentGoal(user);
		redirectAttributes.addFlashAttribute("successMessage","現在の目標を終了しました。");
		return "redirect:/goals/setting";
	}
	
	// 目標時間達成し完了
	@PostMapping("/complete")
	public String completeGoal(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
								RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		goalService.completeGoal(user);
		redirectAttributes.addFlashAttribute("message","目標を達成しました！");
		return "redirect:/goals/setting";
	}
	
}

