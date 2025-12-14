package com.example.goalplanningapp.entity;

public enum DayOfWeek {
	MONDAY("月"),
	TUESDAY("火"),
	WEDNESDAY("水"),
	THURSDAY("木"),
	FRIDAY("金"),
	SATURDAY("土"),
	SUNDAY("日");
	
	private final String label;
	
	private DayOfWeek(String label) {
		this.label = label;
	}
	public String getLabel() {
		return label;		
	}
	
}
