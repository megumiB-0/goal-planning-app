package com.example.goalplanningapp.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.goalplanningapp.dto.EventDTO;
import com.example.goalplanningapp.dto.LearningTimeDTO;
import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.LearningPlan;
import com.example.goalplanningapp.security.UserDetailsImpl;
import com.example.goalplanningapp.service.GoalService;
import com.example.goalplanningapp.service.LearningPlanService;

@RestController
@RequestMapping("/api/learning-plans")
public class LearningPlanApiController {
	@Autowired
	private final LearningPlanService learningPlanService;
	private final GoalService goalService;
	
	public LearningPlanApiController(LearningPlanService learningPlanService,GoalService goalService) {
		this.learningPlanService = learningPlanService;
		this.goalService = goalService;
	}
	
	// 作成（POST）
	@PostMapping("/create")
	public ResponseEntity<?> create(@RequestBody LearningTimeDTO dto,
									@AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
		Goal goal = goalService.getCurrentGoal(userDetailsImpl.getUser());
		try {
			LearningPlan saved =
					learningPlanService.createPlan(dto, userDetailsImpl.getUser(), goal);
			EventDTO event = new EventDTO(
					saved.getId(),
					saved.getPlanningDay().toString() + "T" + saved.getStartTime().toString(),
					saved.getPlanningDay().toString() + "T" + saved.getEndTime().toString());
	System.out.println("event"+event);
			return ResponseEntity.ok(event);

		}catch(IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message",e.getMessage()));
		}
	}
	//取得
	@GetMapping("/events")
	public ResponseEntity<?> getEvents(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
		//ユーザーの全学習記録を取得
		List<LearningPlan> plans = learningPlanService.getPlans(userDetailsImpl.getUser());
		//FullCalendarのJSON形式に変換
		List<EventDTO> events = plans.stream().map(plan -> {
			return new EventDTO(
					plan.getId(),
					plan.getPlanningDay().toString()+"T"+ plan.getStartTime().toString(),
					plan.getPlanningDay().toString()+"T"+ plan.getEndTime().toString()
					);
		}).toList();
		return ResponseEntity.ok(events);
	}
	//更新(PUT)
	@PutMapping("/{id}")
	public ResponseEntity<?> updateRecord(@PathVariable Integer id,
										  @RequestBody LearningTimeDTO dto,
										  @AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
		try {
			learningPlanService.updatePlan(id, dto, userDetailsImpl.getUser());
			return ResponseEntity.ok().build();			
		}catch(IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message",e.getMessage()));
		}
	}
	
	//削除(Delete)
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable Integer id,
									@AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
		learningPlanService.deletePlan(id, userDetailsImpl.getUser());
		return ResponseEntity.ok().build();
	}
	
	//ヘッダーに合計時間表示
	@GetMapping("/planned-totals")
	@ResponseBody
	public Map<LocalDate, Long> getPlanedTotals(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
		return learningPlanService.getPlanDailyTotals(userDetailsImpl.getUser());
	}

}

