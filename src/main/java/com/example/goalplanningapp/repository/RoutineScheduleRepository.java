package com.example.goalplanningapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.goalplanningapp.entity.RoutineSchedule;
import com.example.goalplanningapp.entity.User;

public interface RoutineScheduleRepository extends JpaRepository <RoutineSchedule, Integer>{
	// ルーティン一覧を取得
	List<RoutineSchedule> findByUserOrderByEffectiveFromDesc(User user);
	// ルーティンがあるかないか
	boolean existsByUser(User user);
	
}
