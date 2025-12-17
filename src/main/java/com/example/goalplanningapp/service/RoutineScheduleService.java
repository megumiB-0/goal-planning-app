package com.example.goalplanningapp.service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.goalplanningapp.dto.CalendarEventDTO;
import com.example.goalplanningapp.entity.RoutineDayOfWeek;
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
		List<RoutineRowForm> wakeRows = new ArrayList<>();
		List<RoutineRowForm> sleepRows = new ArrayList<>();
//		RoutineRowForm wakeRow = null;
//		RoutineRowForm sleepRow = null;

		// 睡眠
		for(RoutineRowForm row : form.getRows()) {
			if("睡眠".equals(row.getTitle())) {
				if("就寝".equals(row.getSleepType())) {
					//startTimeを使う
					sleepRows.add(row);
//					sleepRow = row;
					}
					
				if("起床".equals(row.getSleepType())) {
					wakeRows.add(row);
//					wakeRow = row;
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
				if(row.getDays() == null ||row.getDays().isEmpty()) {
					continue;
					}


//			if(wakeRow == null || sleepRow == null) {
//				throw new IllegalStateException("睡眠ルーティンは「起床」と「就寝」を入力してください。");
//			}
//			if(wakeRow.getEndTime() == null || sleepRow.getStartTime() == null) {
//				throw new IllegalStateException("起床時間：就寝時間は必須です。");
//			}
//			if(wakeRow.getDays() == null || wakeRow.getDays().isEmpty()) {
//				throw new IllegalStateException("起床の曜日を選択してください");
//			}
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
			for(RoutineDayOfWeek day : row.getDays()) {
			RoutineScheduleDay scheduleDay = new RoutineScheduleDay();
			scheduleDay.setDay(day);
			scheduleDay.setRoutineSchedule(schedule);
			schedule.getDays().add(scheduleDay);
			}
			routineScheduleRepository.save(schedule);
			continue;
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
			        if (wakeRow == null) {
			            throw new IllegalStateException(wakeDay.next() + "の起床ルーティンがありません。");
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
	// ルーティン一覧取得
	public List<RoutineSchedule> findByUser(User user){
		return routineScheduleRepository.findByUserOrderByEffectiveFromDesc(user);
	}
	// ルーティンの有無確認
	public boolean existsByUser(User user) {
		return routineScheduleRepository.existsByUser(user);
	}
	
	// カレンダーにルーティン表示
    public List<CalendarEventDTO> getRoutineEvents(User user) {

        // 今週（月曜始まり）
        LocalDate startOfWeek =
            LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<RoutineSchedule> schedules = findByUser(user);

        List<CalendarEventDTO> events = new ArrayList<>();

        for (RoutineSchedule schedule : schedules) {
            for (RoutineScheduleDay day : schedule.getDays()) {

                // RoutineDayOfWeek → 今週の日付
                LocalDate date =
                    toDateInWeek(day.getDay(), startOfWeek);

                if (date.isBefore(startOfWeek)
                    || date.isAfter(endOfWeek)) {
                    continue;
                }

                LocalDateTime start =
                    LocalDateTime.of(date, schedule.getStartTime());
                LocalDateTime end =
                    LocalDateTime.of(date, schedule.getEndTime());

                events.add(new CalendarEventDTO(
                    schedule.getTitle(),
                    start.toString(),
                    end.toString()
                ));
            }
        }
        return events;
    }

    /**
     * RoutineDayOfWeek → 今週の LocalDate
     */
    private LocalDate toDateInWeek(
            RoutineDayOfWeek routineDay,
            LocalDate monday) {

        int diff =
            routineDay.ordinal()
            - RoutineDayOfWeek.MONDAY.ordinal();

        return monday.plusDays(diff);
    }
	
	
	
	
	
}