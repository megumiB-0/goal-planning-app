package com.example.goalplanningapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventDTO {
	private Integer id;
	private String start;
	private String end;
}