package com.example.goalplanningapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.goalplanningapp.entity.DayOfWeek;
import com.example.goalplanningapp.entity.RoutineSchedule;
import com.example.goalplanningapp.entity.RoutineScheduleDay;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.form.RoutineForm;
import com.example.goalplanningapp.form.RoutineRowForm;
import com.example.goalplanningapp.repository.RoutineScheduleRepository;



@Service
public class RoutineScheduleService {

	//DI
	private final RoutineScheduleRepository routineScheduleRepository;
	public RoutineScheduleService(RoutineScheduleRepository routineScheduleRepository) {
		this.routineScheduleRepository = routineScheduleRepository;
	}
	
	// ルーティン作成
	public void createRoutines(User user,RoutineForm form) {
		for (RoutineRowForm row : form.getRows()) {
			//入力されていない行はスキップ
			if(row.getStartTime() == null ||
			   row.getEndTime() == null ||
			   row.getDays() == null ||
			   row.getDays().isEmpty()
			   ) {
				continue;
				}

			//曜日ごとに保存
			RoutineSchedule schedule = new RoutineSchedule();
				schedule.setUser(user);
				schedule.setTitle(row.getTitle());
				schedule.setStartTime(row.getStartTime());
				schedule.setEndTime(row.getEndTime());
				schedule.setEffectiveFrom(form.getEffectiveFrom());
				schedule.setEffectiveTo(null);

				for(DayOfWeek day : row.getDays()) {
				RoutineScheduleDay scheduleDay =new RoutineScheduleDay();
				scheduleDay.setDay(day);
				scheduleDay.setRoutineSchedule(schedule);
				schedule.getDays().add(scheduleDay);
				}
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