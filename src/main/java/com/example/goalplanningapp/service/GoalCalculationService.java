package com.example.goalplanningapp.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

@Service
public class GoalCalculationService {
	// 日割り学習時間を計算
	public double calculateHoursPerDay(Double estimatedMinutes, LocalDate start,LocalDate goal) {
		long diffDays = ChronoUnit.DAYS.between(start, goal);
		if (diffDays <= 0) throw new IllegalArgumentException("目標日は開始日より後に設定してください。");
		
		double hoursPerDay = (double) estimatedMinutes / 60 / diffDays; //分→時間
		hoursPerDay = Math.round(hoursPerDay * 10) / 10.0; //小数点以下１桁で四捨五入
		
		if (hoursPerDay > 24) {
			throw new IllegalArgumentException("1日に必要な学習時間が24時間を超えています。期間を長くするか、必要学習時間を減らしてください。");
		}
		return hoursPerDay;
	}

	// 週割り学習時間を計算
	public double calculateHoursPerWeek(double hoursPerDay ) {
		double hoursPerWeek = (double) hoursPerDay * 7;
		hoursPerWeek = Math.round(hoursPerWeek * 10) / 10.0; //小数点以下１桁で四捨五入
		return hoursPerWeek;
	}
}
