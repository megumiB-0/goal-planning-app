package com.example.goalplanningapp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.goalplanningapp.dto.CalendarEventDTO;
import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.RoutineDayOfWeek;
import com.example.goalplanningapp.entity.RoutineSchedule;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.form.RoutineForm;
import com.example.goalplanningapp.security.UserDetailsImpl;
import com.example.goalplanningapp.service.GoalService;
import com.example.goalplanningapp.service.RoutineScheduleService;


@Controller
@RequestMapping("/routines")
public class RoutineController {
	//DI
	private final RoutineScheduleService routineScheduleService;
	private final GoalService goalService;
	public RoutineController(RoutineScheduleService routineScheduleService,
							 GoalService goalService) {
		this.routineScheduleService = routineScheduleService;
		this.goalService = goalService;
	}
	
	// index
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		// ログインユーザーの取得
		User user = userDetailsImpl.getUser();
		// ユーザーのルーティン一覧を取得	
		List<RoutineSchedule> routines = routineScheduleService.findByUser(user);
		model.addAttribute("routines",routines);
		// ルーティン有無でボタン表示を変える
		boolean isUpdate = routineScheduleService.existsByUser(user);
		model.addAttribute("isUpdate", isUpdate); //true=更新　false=新規
		// 一覧を画面に渡す
		return "user/routines/index";
	}
	
	@GetMapping("/calendar")
	@ResponseBody
	public List<CalendarEventDTO> routineCalendar(
	        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

		User user = userDetailsImpl.getUser();
		Goal goal = goalService.getCurrentGoal(user);
	    return routineScheduleService
	            .getRoutineEvents(user, goal.getGoalDate());
	}
	
	//ルーティン初回登録のためのページ表示
	@GetMapping("/new")
	public String newForm(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			Model model) {
		User user = userDetailsImpl.getUser();
		RoutineForm form = routineScheduleService.createInitialForm();
		
		if(routineScheduleService.existsByUser(user)) {
			return "redirect:/routines";
		}
		model.addAttribute("routineForm", form);
		model.addAttribute("days",RoutineDayOfWeek.values());
		model.addAttribute("isUpdate",false);
		return "user/routines/routine-form";
	}
	
	// 初回ルーティン登録
	@PostMapping
	public String createRoutine(
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@ModelAttribute RoutineForm form,
			Model model
			) {
	    // サービス呼び出し
	    try {
	    routineScheduleService.createInitialRoutines(userDetailsImpl.getUser(), form); 
		model.addAttribute("successMessage","ルーティン登録しました！");
		return "redirect:/routines/";
	    }catch(IllegalStateException e) {
	    	model.addAttribute("errorMessage", e.getMessage());
	    	return "user/routines/routine-form";
	    	
	    }
	}
	//ルーティン更新時、前回ルーティンを取得
	@GetMapping("/update")
	public String showUpdateForm(
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			Model model
			) {
		User user = userDetailsImpl.getUser();
		RoutineForm form= routineScheduleService.getCurrentRoutineForm(user);
		model.addAttribute("routineForm", form);
		model.addAttribute("days", RoutineDayOfWeek.values());
		model.addAttribute("isUpdate",true);
		model.addAttribute("effectiveFrom", LocalDate.now()); // デフォルト値
		return "user/routines/routine-form";
	}
	
	
	
	// ルーティン更新
	@PostMapping("/update")
	public String updateRoutine(
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@RequestParam("effectiveFrom") LocalDate effectiveFrom,
			@ModelAttribute RoutineForm form,
			Model model
			) {
		//バリデーション
		if(effectiveFrom == null) {
			model.addAttribute("errorMessage","適用開始日を入力してください。");
			return "user/routines/routine-form";
		}
		
	    // サービス呼び出し
	    try {
	    routineScheduleService.updateRoutines(userDetailsImpl.getUser(), form, effectiveFrom); 
	    model.addAttribute("successMessage","ルーティン更新しました！");
		return "redirect:/routines";
	    }catch(IllegalStateException e) {
	    	model.addAttribute("errorMessage", e.getMessage());
	    	return "user/routines/routine-form";
	    	
	    }
	}
	
	
	
}