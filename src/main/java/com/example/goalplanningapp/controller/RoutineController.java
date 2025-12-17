package com.example.goalplanningapp.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.goalplanningapp.dto.CalendarEventDTO;
import com.example.goalplanningapp.entity.RoutineDayOfWeek;
import com.example.goalplanningapp.entity.RoutineSchedule;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.form.RoutineForm;
import com.example.goalplanningapp.security.UserDetailsImpl;
import com.example.goalplanningapp.service.RoutineScheduleService;


@Controller
@RequestMapping("/routines")
public class RoutineController {
	//DI
	private final RoutineScheduleService routineScheduleService;
	public RoutineController(RoutineScheduleService routineScheduleService) {
		this.routineScheduleService = routineScheduleService;
	}
	
	
	// ログインユーザーの取得
	// ユーザーのルーティン一覧を取得
	// 一覧を画面に渡す
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userDetailsImpl.getUser();
		List<RoutineSchedule> routines = routineScheduleService.findByUser(user);
		model.addAttribute("routines",routines);
		return "user/routines/index";
	}
	
	@GetMapping("/calendar")
	@ResponseBody
	public List<CalendarEventDTO> routineCalendar(
	        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

		User user = userDetailsImpl.getUser();
	    return routineScheduleService
	            .getRoutineEvents(user);
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
		return "user/routines/new";
	}
	
	
	// ルーティン登録
	@PostMapping
	public String create(
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@ModelAttribute RoutineForm form,
			RedirectAttributes ra
			) {
	    System.out.println("form=" + form);
	    System.out.println("rows=" + form.getRows());
	    // サービス呼び出し
	    try {
	    routineScheduleService.createRoutines(userDetailsImpl.getUser(), form); 
		ra.addFlashAttribute("successMessage","ルーティン登録しました！");
		return "redirect:/routines";
	    }catch(IllegalStateException e) {
	    	ra.addFlashAttribute("errorMessage", e.getMessage());
	    	return "redirect:/routines/new";
	    	
	    }
		
	
	    
	}
	

}
