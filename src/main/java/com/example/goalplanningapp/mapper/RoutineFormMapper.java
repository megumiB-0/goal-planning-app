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
	
	form.setRows(rows);
	return form;
	}
	
	// 睡眠を就寝・起床に戻す
	private void rebuildSleepRows(List<RoutineSchedule> schedules,
								  List<RoutineRowForm> rows) {
		Map<RoutineDayOfWeek, LocalTime> sleepMap = new LinkedHashMap<>();
		Map<RoutineDayOfWeek, LocalTime> wakeMap = new LinkedHashMap<>();	
		
		//DB上の睡眠を分類
		for(RoutineSchedule s : schedules) {
			if(!"睡眠".equals(s.getTitle())) continue;
			
			RoutineDayOfWeek day = s.getDays().iterator().next().getDay();
            // 就寝（23:00 → 23:59）
            if (LocalTime.MAX.equals(s.getEndTime())) {
                sleepMap.put(day, s.getStartTime());
            }

            // 起床（00:00 → 07:00）
            if (LocalTime.MIN.equals(s.getStartTime())) {
                wakeMap.put(day, s.getEndTime());
            }
        }

        // ===== 就寝行 =====
        if (!sleepMap.isEmpty()) {
            RoutineRowForm sleepRow = new RoutineRowForm();
            sleepRow.setTitle("睡眠");
            sleepRow.setSleepType("就寝");

            sleepRow.setStartTime(
                sleepMap.values().iterator().next()
            );

            sleepRow.setDays(sleepMap.keySet().stream().toList());
            rows.add(sleepRow);
        }

        // ===== 起床行 =====
        if (!wakeMap.isEmpty()) {
            RoutineRowForm wakeRow = new RoutineRowForm();
            wakeRow.setTitle("睡眠");
            wakeRow.setSleepType("起床");

            wakeRow.setEndTime(
                wakeMap.values().iterator().next()
            );

            wakeRow.setDays(wakeMap.keySet().stream().toList());
            rows.add(wakeRow);
			
		}
	}	
}
