package com.example.goalplanningapp.form;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RoutineForm {
	private LocalDate effectiveFrom;
	private List<RoutineRowForm> rows = new ArrayList<>();
}
