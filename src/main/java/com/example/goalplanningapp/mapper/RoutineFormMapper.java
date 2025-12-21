package com.example.goalplanningapp.mapper;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.goalplanningapp.entity.RoutineDayOfWeek;
import com.example.goalplanningapp.entity.RoutineSchedule;
import com.example.goalplanningapp.entity.RoutineScheduleDay;
import com.example.goalplanningapp.form.RoutineForm;
import com.example.goalplanningapp.form.RoutineRowForm;

@Component
public class RoutineFormMapper {
	public RoutineForm toForm(List<RoutineSchedule> schedules) {
	// 空のリストを作る
	RoutineForm form = new RoutineForm();
	List<RoutineRowForm> rows = new ArrayList<>();
	
	// ===通常ルーティン===
	schedules.stream()
			.filter(s -> !"睡眠".equals(s.getTitle()))
			.forEach(schedule -> {
				RoutineRowForm row = new RoutineRowForm();
				row.setTitle(schedule.getTitle());
				row.setStartTime(schedule.getStartTime());
				row.setEndTime(schedule.getEndTime());
			
			 List<RoutineDayOfWeek> days = schedule.getDays().stream()
					.map(RoutineScheduleDay::getDay)
					.distinct()
					.toList();
			row.setDays(days);
			rows.add(row);
			});	
	
	// ===睡眠ルーティン===
	rebuildSleepRows(schedules, rows);

	//並び順を変更
	sortRows(rows);
	
	
	form.setRows(rows);
	System.out.println("mapper:"+ rows);
	return form;
	}
	
	// 睡眠を就寝・起床に戻す
	private void rebuildSleepRows(List<RoutineSchedule> schedules,
								  List<RoutineRowForm> rows) {
		// 時刻　→　曜日
		Map<LocalTime, List<RoutineDayOfWeek>> sleepTimeMap = new LinkedHashMap<>();
		Map<LocalTime, List<RoutineDayOfWeek>> wakeTimeMap = new LinkedHashMap<>();	
		
		//DB上の睡眠を分類
		for(RoutineSchedule s : schedules) {
			if(!"睡眠".equals(s.getTitle())) continue;
			for(RoutineScheduleDay sd : s.getDays()) {
				RoutineDayOfWeek day = sd.getDay();
				LocalTime start = s.getStartTime();
				LocalTime end = s.getEndTime();
			
			// 日を跨がない睡眠(ex 1:00~9:00)
			if(start != null && end != null
							&& !LocalTime.MIN.equals(start)
							&& !start.isAfter(end)) {
				// 就寝は前日表示
				sleepTimeMap
					.computeIfAbsent(start, k -> new ArrayList<>())
					.add(day.prev());
				wakeTimeMap
				.computeIfAbsent(end, k -> new ArrayList<>())
				.add(day);
				
				continue;
			}
			// 就寝（23:00 → 23:59）当日の曜日　　DB上00:00:00で登録される
            if(start != null && LocalTime.MIN.equals(end)) {
                sleepTimeMap
                	.computeIfAbsent(start, k -> new ArrayList<>())
                	.add(day);
            }
            
            // 起床（00:00 → 07:00）
            if (end != null && LocalTime.MIN.equals(start)) {
                wakeTimeMap
                	.computeIfAbsent(end, k -> new ArrayList<>())
                	.add(day);
            }
		}
	}

        // ===== 就寝行 =====
		for(Map.Entry<LocalTime, List<RoutineDayOfWeek>> entity
				: sleepTimeMap.entrySet()) {
			LocalTime sleepTime = entity.getKey();
			List<RoutineDayOfWeek> days = entity.getValue();
			
	           RoutineRowForm row = new RoutineRowForm();
	            row.setTitle("睡眠");
	            row.setSleepType("就寝");
	            row.setStartTime(sleepTime);
	            row.setDays(days);
	            rows.add(row);
		}
        // ===== 起床行 =====
		for(Map.Entry<LocalTime, List<RoutineDayOfWeek>> entity
				: wakeTimeMap.entrySet()) {
			LocalTime wakeTime = entity.getKey();
			List<RoutineDayOfWeek> days = entity.getValue();
			
	           RoutineRowForm row = new RoutineRowForm();
	            row.setTitle("睡眠");
	            row.setSleepType("起床");
	            row.setEndTime(wakeTime);
	            row.setDays(days);
	            rows.add(row);
		}
	}
	
	private void sortRows(List<RoutineRowForm> rows) {
			// 順番を定義
			Map<String, Integer> orderMap = new LinkedHashMap<>();
			orderMap.put("起床", 0);
			orderMap.put("朝食", 1);			
			orderMap.put("昼食", 2);
			orderMap.put("夕食", 3);
			orderMap.put("仕事", 4);
			orderMap.put("入浴", 5);
			orderMap.put("就寝", 6);
			
			rows.sort((a,b) -> {
				int orderA = getOrder(a, orderMap);
				int orderB = getOrder(b, orderMap);
				return Integer.compare(orderA, orderB);
			});

	}
	
	private int getOrder(RoutineRowForm row, Map<String, Integer> orderMap) {
		//睡眠（起床・就寝）
		if("睡眠".equals(row.getTitle())){
			return orderMap.getOrDefault(row.getSleepType(), 999);
		}
		// 通常
		return orderMap.getOrDefault(row.getTitle(), 999);
	}
}
