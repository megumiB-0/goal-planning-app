package com.example.goalplanningapp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.goalplanningapp.entity.DayOfWeek;
import com.example.goalplanningapp.entity.RoutineSchedule;
import com.example.goalplanningapp.entity.RoutineScheduleDay;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.form.RoutineForm;
import com.example.goalplanningapp.form.RoutineRowForm;
import com.example.goalplanningapp.repository.RoutineScheduleRepository;

import jakarta.transaction.Transactional;



@Service
public class RoutineScheduleService {

	//DI
	private final RoutineScheduleRepository routineScheduleRepository;
	public RoutineScheduleService(RoutineScheduleRepository routineScheduleRepository) {
		this.routineScheduleRepository = routineScheduleRepository;
	}
	
	// デフォルトのrowsを作る
	public RoutineForm createInitialForm() {
		RoutineForm form = new RoutineForm();
		List<RoutineRowForm> rows = new ArrayList<>();
		rows.add(row("起床"));
		rows.add(row("朝食"));
		rows.add(row("昼食"));		
		rows.add(row("夕食"));
		rows.add(row("仕事"));
		rows.add(row("入浴"));
		rows.add(row("就寝"));
		
		form.setRows(rows);
		return form;
	}
	
	// rowにタイトルを追加する
	private RoutineRowForm row(String title) {
		RoutineRowForm row = new RoutineRowForm();
		row.setTitle(title);
		return row;
	}
	
	
	
	// ルーティン作成
	@Transactional
	public void createRoutines(User user,RoutineForm form) {
		
	// 振り分け
		for (RoutineRowForm row : form.getRows()) {
			// 入力されていない行はスキップ
			if(row.getStartTime() == null ||
			   row.getEndTime() == null ||
			   row.getDays() == null ||
			   row.getDays().isEmpty()
			   ) {
				continue;
				}
			
			// 睡眠
			if(row.getTitle().equals("起床")) {
				// sleepEndとして扱う
				handleWakeUp(user,row,form);
				continue;
				}
			if(row.getTitle().equals("就寝")) {
				// sleepEndとして扱う
				handleSleep(user,row,form);
				continue;
				}
				// 通常ルーティン
				handleNormalRoutine(user,row,form); 
			}
	}
	
	// 通常ルーティン（起床・就寝以外）
	private void handleNormalRoutine(
			User user,RoutineRowForm row,RoutineForm form) {
		if(row.getStartTime() == null || row.getEndTime() == null) {
			return;
		}
		
		
		// 曜日ごとに保存
		for(DayOfWeek day : row.getDays()) {
			RoutineSchedule schedule = new RoutineSchedule();
			schedule.setUser(user);
			schedule.setTitle(row.getTitle());
			schedule.setStartTime(row.getStartTime());
			schedule.setEndTime(row.getEndTime());
			schedule.setEffectiveFrom(form.getEffectiveFrom());
			schedule.setEffectiveTo(null);

			RoutineScheduleDay scheduleDay =new RoutineScheduleDay();
			scheduleDay.setDay(day);
			scheduleDay.setRoutineSchedule(schedule);
			schedule.getDays().add(scheduleDay);
	
			routineScheduleRepository.save(schedule);	
		}
	}
	
	// 就寝
	private void handleSleep(
			User user, RoutineRowForm row, RoutineForm form) {
		if(row.getStartTime() == null) {
			return;
		}
		// 曜日ごとに保存
		for(DayOfWeek day : row.getDays()) {
			RoutineSchedule schedule = new RoutineSchedule();
			schedule.setUser(user);
			schedule.setTitle("就寝");
			schedule.setStartTime(row.getStartTime());
			schedule.setEndTime(null);
			schedule.setEffectiveFrom(form.getEffectiveFrom());
			schedule.setEffectiveTo(null);

			RoutineScheduleDay scheduleDay =new RoutineScheduleDay();
			scheduleDay.setDay(day);
			scheduleDay.setRoutineSchedule(schedule);
			schedule.getDays().add(scheduleDay);
	
			routineScheduleRepository.save(schedule);
		}
	}
	// 起床
	private void handleWakeUp(
			User user, RoutineRowForm row, RoutineForm form) {
		if(row.getStartTime() == null) {
			return;
		}
		// 曜日ごとに保存
		for(DayOfWeek day : row.getDays()) {
			RoutineSchedule schedule = new RoutineSchedule();
			schedule.setUser(user);
			schedule.setTitle("起床");
			schedule.setStartTime(row.getStartTime());
			schedule.setEndTime(null);
			schedule.setEffectiveFrom(form.getEffectiveFrom());
			schedule.setEffectiveTo(null);

			RoutineScheduleDay scheduleDay =new RoutineScheduleDay();
			scheduleDay.setDay(day);
			scheduleDay.setRoutineSchedule(schedule);
			schedule.getDays().add(scheduleDay);
	
			routineScheduleRepository.save(schedule);
		}
	}	
	
	
	// ルーティン一覧取得
	public List<RoutineSchedule> findByUser(User user){
		return routineScheduleRepository.findByUserOrderByEffectiveFromDesc(user);
	}
	// ルーティンの有無確認
	public boolean existsByUser(User user) {
		return routineScheduleRepository.existsByUser(user);
	}
	
	
	
}