package com.example.goalplanningapp.entity;

import java.time.DayOfWeek;

public enum RoutineDayOfWeek {
	MONDAY("月"),
	TUESDAY("火"),
	WEDNESDAY("水"),
	THURSDAY("木"),
	FRIDAY("金"),
	SATURDAY("土"),
	SUNDAY("日");
	
	private final String label;
	
	private RoutineDayOfWeek(String label) {
		this.label = label;
	}
	public String getLabel() {
		return label;
	}
	public RoutineDayOfWeek next() {
		int nextOrdinal = (this.ordinal() + 1) % values().length;
		return values()[nextOrdinal];
	}
    // java.time.DayOfWeek→　　アプリ独自ではなく、javaのDayOfWeek
    public static RoutineDayOfWeek fromJavaDay(java.time.DayOfWeek javaDay) {
        return RoutineDayOfWeek.valueOf(javaDay.name());
    }
    // →java.time.DayOfWeek　変換
    public DayOfWeek toJavaDay() {
        return DayOfWeek.valueOf(this.name());
    }

}
