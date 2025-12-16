package com.example.goalplanningapp.service;

import java.time.LocalDate;
import java.time.LocalTime;
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
	private final GoalService goalService;
	public RoutineScheduleService(
			RoutineScheduleRepository routineScheduleRepository,
			GoalService goalService) {
		this.routineScheduleRepository = routineScheduleRepository;
		this.goalService = goalService;
	}
	
	// 睡眠用
	private RoutineRowForm sleepRow(String type) {
		RoutineRowForm row = new RoutineRowForm();
		row.setTitle("睡眠");	// 保存用
		row.setSleepType(type); // 表示用（起床・就寝）
		return row;
	}
	
	
	// デフォルトのrowsを作る
	public RoutineForm createInitialForm() {
		RoutineForm form = new RoutineForm();
		List<RoutineRowForm> rows = new ArrayList<>();
		rows.add(sleepRow("起床"));
//		rows.add(row("睡眠"));
		rows.add(row("朝食"));
		rows.add(row("昼食"));		
		rows.add(row("夕食"));
		rows.add(row("仕事"));
		rows.add(row("入浴"));
		rows.add(sleepRow("就寝"));		
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
		LocalDate effectiveFrom = goalService.getCurrentGoalStartDate(user);	
		if(effectiveFrom == null) {
			throw new IllegalStateException("effectiveFrom is null");
		}
		
	// 振り分け
		RoutineRowForm wakeRow = null;
		RoutineRowForm sleepRow = null;
		for (RoutineRowForm row : form.getRows()) {
			// スキップ条件設定
			if(
			   row.getDays() == null ||
			   row.getDays().isEmpty()
			   ) {
				continue;
				}
			
			// 睡眠
			if("睡眠".equals(row.getTitle())) {

				if("就寝".equals(row.getSleepType())) {
					//startTimeを使う
					sleepRow = row;
					}
					
				if("起床".equals(row.getSleepType())) {
					wakeRow = row;
					}
				continue;
			}
			// 通常ルーティン（起床・就寝以外）
			if(row.getStartTime() == null || row.getEndTime() == null) {
				continue;
			}
			RoutineSchedule schedule = new RoutineSchedule();
			schedule.setUser(user);
			schedule.setTitle(row.getTitle());
			schedule.setStartTime(row.getStartTime());
			schedule.setEndTime(row.getEndTime());
			schedule.setEffectiveFrom(effectiveFrom);
			schedule.setEffectiveTo(null);
			// 曜日ごとに保存
			for(DayOfWeek day : row.getDays()) {
			RoutineScheduleDay scheduleDay = new RoutineScheduleDay();
			scheduleDay.setDay(day);
			scheduleDay.setRoutineSchedule(schedule);
			schedule.getDays().add(scheduleDay);
			}
			routineScheduleRepository.save(schedule);
			continue;
		}
		//睡眠ルーティン保存
		if (wakeRow != null && sleepRow != null
				&& wakeRow.getEndTime() != null
				&& sleepRow.getStartTime() != null
				&& wakeRow.getDays() != null
				&& !wakeRow.getDays().isEmpty()) {		
		for(DayOfWeek sleepDay : sleepRow.getDays()) {
			DayOfWeek wakeDay = sleepDay.next();
			LocalTime sleepTime = sleepRow.getStartTime(); // 就寝
		    LocalTime wakeTime  = wakeRow.getEndTime();    // 起床（翌日）
		    //深夜就寝（0:00~）
		    if (sleepTime.isBefore(LocalTime.NOON)) {
		    	
		    saveSleep(
		         user,
		         sleepTime,
		         wakeTime,
		         wakeDay,        // ★ 起床日
		         effectiveFrom
	        );	
		    
		    }else {
		        // 就寝日（就寝〜24:00）
		        saveSleep(
		            user,
		            sleepTime,
		            LocalTime.MAX,
		            sleepDay,       // ★ 就寝日
		            effectiveFrom
		        );

		        // 起床日（0:00〜起床）
		        saveSleep(
		            user,
		            LocalTime.MIN,
		            wakeTime,
		            wakeDay,        // ★ 起床日
		            effectiveFrom
		        );	
		    }
		}
	}
	}
	// 睡眠保存用
	private void saveSleep(
		    User user,
		    LocalTime startTime,
		    LocalTime endTime,
		    DayOfWeek day,
		    LocalDate effectiveFrom
		) { 
			if(startTime == null || endTime == null) return;
		    RoutineSchedule schedule = new RoutineSchedule();
		    schedule.setUser(user);
		    schedule.setTitle("睡眠");
		    schedule.setStartTime(startTime);
		    schedule.setEndTime(endTime);
		    schedule.setEffectiveFrom(effectiveFrom);
		    schedule.setEffectiveTo(null);

		    RoutineScheduleDay sd = new RoutineScheduleDay();
		    sd.setDay(day);
		    sd.setRoutineSchedule(schedule);
		    schedule.getDays().add(sd);

		    routineScheduleRepository.save(schedule);
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