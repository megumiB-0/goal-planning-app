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
import com.example.goalplanningapp.repository.RoutineScheduleRepository;

@Service
public class RoutineScheduleService {

	//DI
	private final RoutineScheduleRepository routineScheduleRepository;
	public RoutineScheduleService(RoutineScheduleRepository routineScheduleRepository) {
		this.routineScheduleRepository = routineScheduleRepository;
	}
	
	public RoutineSchedule createRoutine(
			User user,
			String title,
			LocalTime startTime,
			LocalTime endTime,
			LocalDate from,
			List<DayOfWeek> days
	) {
		//親
		RoutineSchedule schedule = new RoutineSchedule();
		schedule.setUser(user);
		schedule.setTitle(title);
		schedule.setStartTime(startTime);
		schedule.setEndTime(endTime);
		schedule.setEffectiveFrom(from);
		schedule.setEffectiveTo(null);

		//子
		List<RoutineScheduleDay> dayEntities = new ArrayList<>();
		
		for(DayOfWeek day : days) {
			RoutineScheduleDay d = new RoutineScheduleDay();
			d.setDayOfWeek(day);
			d.setRoutineSchedule(schedule);
			dayEntities.add(d);
		}
		
		//親に子をセットする
		schedule.setRoutineScheduleDays(dayEntities);
		//保存
		return routineScheduleRepository.save(schedule);

	};

	
}
