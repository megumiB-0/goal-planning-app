package com.example.goalplanningapp.form;

import java.time.LocalTime;
import java.util.List;

import com.example.goalplanningapp.entity.DayOfWeek;

import lombok.Data;

@Data
public class RoutineRowForm {
	private String title;
	private LocalTime startTime;
	private LocalTime endTime;
	private List<DayOfWeek> days;

}
