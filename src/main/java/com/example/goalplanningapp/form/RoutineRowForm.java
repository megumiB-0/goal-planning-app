package com.example.goalplanningapp.form;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.example.goalplanningapp.entity.RoutineDayOfWeek;

import lombok.Data;

@Data
public class RoutineRowForm {
	private String title;
	
	// 表示用（起床・就寝のみ）
	private String sleepType;
	
	//通常イベント用
	private LocalTime startTime; //開始(睡眠なら就寝)
	private LocalTime endTime;   //終了(睡眠なら起床)
	
	private List<RoutineDayOfWeek> days = new ArrayList<>();

}
