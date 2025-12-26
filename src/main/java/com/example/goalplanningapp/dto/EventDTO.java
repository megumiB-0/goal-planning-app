package com.example.goalplanningapp.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventDTO {
	private Integer id;
	private String title;
	private String start;
	private String end;
    private Map<String,Object> extendedProps; // type などをここに 'plan', 'record', 'routine' をセット
}