package com.example.goalplanningapp.service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.goalplanningapp.dto.CalendarEventDTO;
import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.RoutineDayOfWeek;
import com.example.goalplanningapp.entity.RoutineSchedule;
import com.example.goalplanningapp.entity.RoutineScheduleDay;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.form.RoutineForm;
import com.example.goalplanningapp.form.RoutineRowForm;
import com.example.goalplanningapp.mapper.RoutineFormMapper;
import com.example.goalplanningapp.repository.RoutineScheduleRepository;

import jakarta.transaction.Transactional;



@Service
public class RoutineScheduleService {

	//DI
	private final RoutineScheduleRepository routineScheduleRepository;
	private final GoalService goalService;
	private final RoutineFormMapper routineFormMapper;
	public RoutineScheduleService(
			RoutineScheduleRepository routineScheduleRepository,
			GoalService goalService, RoutineFormMapper routineFormMapper) {
		this.routineScheduleRepository = routineScheduleRepository;
		this.goalService = goalService;
		this.routineFormMapper = routineFormMapper;
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
	

	
	
	
	// ルーティン作成(初回・更新 共通)
	@Transactional
	public void createRoutines(User user,RoutineForm form, LocalDate effectiveFrom) {
		if(effectiveFrom == null) {
			throw new IllegalArgumentException("effectiveFrom is null");
		}
		//時間帯map(同一項目時間被り確認用)
		Map<String, List<TimeRange>> timeRangeMap = new HashMap<>();

		
		
		// 振り分け
		List<RoutineRowForm> wakeRows = new ArrayList<>();
		List<RoutineRowForm> sleepRows = new ArrayList<>();

		// 睡眠
		for(RoutineRowForm row : form.getRows()) {
			if("睡眠".equals(row.getTitle())) {
				if("就寝".equals(row.getSleepType())) {
					//startTimeを使う
					sleepRows.add(row);
					}
					
				if("起床".equals(row.getSleepType())) {
					wakeRows.add(row);
					}
			}
			
		}
			// 全曜日で睡眠チェック
			for(RoutineDayOfWeek day : RoutineDayOfWeek.values()) {
				RoutineRowForm wakeRowForDay  = findRowForDay(wakeRows, day);
				RoutineRowForm sleepRowForDay = findRowForDay(sleepRows, day);
				if ((wakeRowForDay == null && sleepRowForDay == null)) {
					throw new IllegalStateException(day + "の睡眠ルーティンがありません。");
				}
				if ((wakeRowForDay != null && sleepRowForDay == null)) { 
					throw new IllegalStateException(day + "の睡眠ルーティンに就寝がありません。"); }
				if ((wakeRowForDay == null && sleepRowForDay != null)) {
					throw new IllegalStateException(day + "の睡眠ルーティンに起床がありません。"); } 
			}
	

			for (RoutineRowForm row : form.getRows()) {
				
			// スキップ条件設定
			//通常ルーティン（起床・就寝以外）
			if("睡眠".equals(row.getTitle())) {
				continue;
			}
			if(row.getDays() == null || row.getDays().isEmpty()) {
				continue;
			}
			// 通常ルーティン（起床・就寝以外）
			if(row.getStartTime() == null || row.getEndTime() == null) {
				continue;
			}
			// 開始＜終了チェック
			if(!row.getStartTime().isBefore(row.getEndTime())){
				throw new IllegalStateException(
					row.getTitle() + "の開始時刻は終了時刻よりも前である必要があります。");
			}
			
			for(RoutineDayOfWeek day : row.getDays()) {
				String key = row.getTitle() + "_" + day;
				TimeRange newRange = new TimeRange(row.getStartTime(), row.getEndTime());
				
				List<TimeRange> existingRanges = timeRangeMap.computeIfAbsent(key, k -> new ArrayList<>());
				
				for(TimeRange existing : existingRanges) {
					if(existing.overlaps(newRange)) {
						throw new IllegalStateException(
								row.getTitle() + "は" + day +
								" に時間帯が重複しています。");
					}
				}
				existingRanges.add(newRange);
				}
			
			RoutineSchedule schedule = new RoutineSchedule();
			schedule.setUser(user);
			schedule.setTitle(row.getTitle());
			schedule.setStartTime(row.getStartTime());
			schedule.setEndTime(row.getEndTime());
			schedule.setEffectiveFrom(effectiveFrom);
			schedule.setEffectiveTo(null);
			// 曜日ごとに保存
			for(RoutineDayOfWeek day : row.getDays()) {
			RoutineScheduleDay scheduleDay = new RoutineScheduleDay();
			scheduleDay.setDay(day);
			scheduleDay.setRoutineSchedule(schedule);
			schedule.getDays().add(scheduleDay);
			}
			routineScheduleRepository.save(schedule);
		}
		//睡眠ルーティン保存
			Map<String, RoutineSchedule> sleepScheduleMap = new HashMap<>();
			// 各就寝ルーティンをループ
			for (RoutineRowForm sleepRow : sleepRows) {
			    LocalTime sleepTime = sleepRow.getStartTime(); // 就寝時間
			    
			    // 就寝ルーティンの各曜日をループ
			    for (RoutineDayOfWeek sleepDay : sleepRow.getDays()) {
			    	// 起床は必ず翌日の曜日
			        RoutineDayOfWeek wakeDay = sleepDay.next();
			        // 対応する起床ルーティンを探す
			        RoutineRowForm wakeRow = findRowForDay(wakeRows, wakeDay);
			        
			        if (sleepRow == null || wakeRow == null) {
			        	throw new IllegalStateException("就寝・起床時間を両方入力してください。");
			        }
			        
			        LocalTime wakeTime = wakeRow.getEndTime(); // 起床時間
			        // 跨日しない（1:00 → 7:00 など）→ 起床日の睡眠として登録
			        if (!sleepTime.isAfter(wakeTime)) {
			            addSleep(
			            	sleepScheduleMap,
			                user,
			                sleepTime,
			                wakeTime,
			                wakeDay,          // ← 起床日の曜日
			                effectiveFrom
			            );
			        }
			        // 跨日する（23:00 → 翌日 7:00）
			        else {
			            // 就寝日：23:00 ～ 23:59
			            addSleep(
			            	sleepScheduleMap,
			                user,
			                sleepTime,
			                LocalTime.MAX,
			                sleepDay,
			                effectiveFrom
			            );
			            // 起床日：0:00 ～ 起床時間
			            addSleep(
				            sleepScheduleMap,
			                user,
			                LocalTime.MIN,
			                wakeTime,
			                wakeDay,
			                effectiveFrom
			            );
			        }
			    }
			}
			for(RoutineSchedule schedule : sleepScheduleMap.values()) {
				routineScheduleRepository.save(schedule);
			}
	}
	
	//睡眠ルーティン保存
	private void addSleep(
		    Map<String, RoutineSchedule> map,
		    User user,
		    LocalTime startTime,
		    LocalTime endTime,
		    RoutineDayOfWeek day,
		    LocalDate effectiveFrom) {

		    String key = startTime + "_" + endTime;

		    RoutineSchedule schedule = map.get(key);
		    if (schedule == null) {
		        schedule = new RoutineSchedule();
		        schedule.setUser(user);
		        schedule.setTitle("睡眠");
		        schedule.setStartTime(startTime);
		        schedule.setEndTime(endTime);
		        schedule.setEffectiveFrom(effectiveFrom);
		        schedule.setEffectiveTo(null);
		        map.put(key, schedule);
		    }

		    // 曜日追加
		    RoutineScheduleDay sd = new RoutineScheduleDay();
		    sd.setDay(day);
		    sd.setRoutineSchedule(schedule);
		    schedule.getDays().add(sd);
		}
	
	// ヘルパー
	private RoutineRowForm findRowForDay(List<RoutineRowForm> rows, RoutineDayOfWeek day) {
	    for (RoutineRowForm row : rows) {
	        if (row.getDays() != null && row.getDays().contains(day)) return row;
	    }
	    return null;
	}
	
	// 初回登録用ラッパー
	@Transactional
	public void createInitialRoutines(User user, RoutineForm form) {		
		LocalDate effectiveFrom = goalService.getCurrentGoalStartDate(user);	
		if(effectiveFrom == null) {
			throw new IllegalStateException("goal start is null");
		}
		createRoutines(user, form, effectiveFrom);
	}
	
	// 更新用ラッパー
 	@Transactional
 	public void updateRoutines(User user, RoutineForm form, LocalDate newEffectiveFrom) {
 		//現在のルーティンを終了させる
 		List<RoutineSchedule> current = routineScheduleRepository.findCurrentByUser(user);
 		LocalDate endDate = newEffectiveFrom.minusDays(1);

 		for(RoutineSchedule r : current){
 			r.setEffectiveTo(endDate);
 		}
 		
 		// ルーティン登録
 		createRoutines(user, form, newEffectiveFrom);
 		
 		}
	

	// ルーティン一覧取得
	public List<RoutineSchedule> findByUser(User user){
		return routineScheduleRepository.findByUserOrderByEffectiveFromDesc(user);
	}
	// ルーティンの有無確認
	public boolean existsByUser(User user) {
		return routineScheduleRepository.existsByUser(user);
	}
	
	
	
	
	
	
	// カレンダーにルーティン表示
    public List<CalendarEventDTO> getRoutineEvents(User user, LocalDate goalDate) {
    	Goal goal = goalService.getCurrentGoal(user);
    	LocalDate periodStart = goal.getStartDate();
    	LocalDate periodEnd = goalDate;    	

        List<RoutineSchedule> schedules = findByUser(user);
        List<CalendarEventDTO> events = new ArrayList<>();


        for (RoutineSchedule schedule : schedules) {
        	
        	LocalDate effectiveFrom = schedule.getEffectiveFrom();
        	LocalDate effectiveTo = schedule.getEffectiveTo() !=null
        							? schedule.getEffectiveTo()
        							: periodEnd; //nullなら目標達成日まで
        	
        	
        	LocalDate start = periodStart.isAfter(effectiveFrom)
        					  ? periodStart
        					  : effectiveFrom;
        	LocalDate end = periodStart.isAfter(effectiveTo)
        				   	  ? periodEnd
        					  : effectiveTo;
        	if(start.isAfter(end)) continue;
        		// 全日程チェック
        		for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
        			DayOfWeek dow = date.getDayOfWeek();
        			for (RoutineScheduleDay day : schedule.getDays()) {
        				if (dow == day.getDay().toJavaDay()) {
        					LocalDateTime startTime = LocalDateTime.of(date, schedule.getStartTime());
        					LocalDateTime endTime = LocalDateTime.of(date, schedule.getEndTime());
        					events.add(new CalendarEventDTO(
        							schedule.getTitle(),
        							startTime.toString(),
        							endTime.toString()
        					));
        				}
        			}
        		}
        }
        return events;
    }
	
 // 最新ルーティン取得後フォームへ戻す
 	public RoutineForm getCurrentRoutineForm(User user) {
 		List<RoutineSchedule> schedules = routineScheduleRepository.findCurrentByUser(user);
 		return routineFormMapper.toForm(schedules);
 		
 	}
 	
	//同一項目時間被り確認用
	private static class TimeRange{
		LocalTime start;
		LocalTime end;
		TimeRange(LocalTime start, LocalTime end){
			this.start = start;
			this.end = end;
		}
		boolean overlaps(TimeRange other) {
			return this.start.isBefore(other.end)
				&& other.start.isBefore(this.end);
		}
		
	}

	
	
	
}