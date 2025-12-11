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
import com.example.goalplanningapp.dto.LearningRecordDTO;
import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.LearningRecord;
import com.example.goalplanningapp.security.UserDetailsImpl;
import com.example.goalplanningapp.service.GoalService;
import com.example.goalplanningapp.service.LearningRecordService;


@RestController
@RequestMapping("/api/learning-records")
public class LearningRecordApiController {
	@Autowired
	private final LearningRecordService learningRecordService;
	private final GoalService goalService;
	
	public LearningRecordApiController(LearningRecordService learningRecordService,
									   GoalService goalService) {
		this.learningRecordService = learningRecordService;
		this.goalService = goalService;
	}
	//作成
	@PostMapping("/create")
	public ResponseEntity<?> create(@RequestBody LearningRecordDTO dto,
									@AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
		Goal goal = goalService.getCurrentGoal(userDetailsImpl.getUser());
		try {
			// 保存して帰ってくる
			LearningRecord saved =learningRecordService.createRecord(dto, userDetailsImpl.getUser(), goal);
			//保存した内容をDTOに詰めて返す
			EventDTO event = new EventDTO(
					saved.getId(),
					saved.getLearningDay().toString() + "T" + saved.getStartTime().toString(),
					saved.getLearningDay().toString() + "T" + saved.getEndTime().toString());
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
		List<LearningRecord> records = learningRecordService.getRecords(userDetailsImpl.getUser());
		//FullCalendarのJSON形式に変換
		List<EventDTO> events = records.stream().map(record -> {
			return new EventDTO(
					record.getId(),
					record.getLearningDay().toString()+"T"+ record.getStartTime().toString(),
					record.getLearningDay().toString()+"T"+ record.getEndTime().toString()
					);
		}).toList();
		return ResponseEntity.ok(events);
	}
	//更新
	@PutMapping("/{id}")
	public ResponseEntity<?> updateRecord(@PathVariable Integer id,
										  @RequestBody LearningRecordDTO dto,
										  @AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
		try {
			learningRecordService.updateRecord(id, dto, userDetailsImpl.getUser());
			return ResponseEntity.ok().build();			
		}catch(IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message",e.getMessage()));
		}
	}
	
	//削除
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable Integer id,
									@AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
		learningRecordService.deleteRecord(id, userDetailsImpl.getUser());
		return ResponseEntity.ok().build();
	}
	
	//ヘッダーに合計時間表示
	@GetMapping("/daily-totals")
	@ResponseBody
	public Map<LocalDate, Long> getDailyTotals(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
		return learningRecordService.getDailyTotals(userDetailsImpl.getUser());
	}

}
