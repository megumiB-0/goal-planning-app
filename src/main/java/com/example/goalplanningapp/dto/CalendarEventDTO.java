package com.example.goalplanningapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarEventDTO {
    private String title;
    private String start;
    private String end;

}
