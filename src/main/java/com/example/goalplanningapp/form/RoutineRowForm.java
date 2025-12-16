package com.example.goalplanningapp.form;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.example.goalplanningapp.entity.DayOfWeek;

import lombok.Data;

@Data
public class RoutineRowForm {
	private String title;
	
	//通常イベント用
	private LocalTime startTime;
	private LocalTime endTime;
	
	//睡眠用（起床就寝）
	private LocalTime sleepTime; 
	private List<DayOfWeek> days = new ArrayList<>();

}
