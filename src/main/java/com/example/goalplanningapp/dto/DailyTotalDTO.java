package com.example.goalplanningapp.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyTotalDTO {
	private LocalDate learningDay;
	private Long totalMinutes;

}
