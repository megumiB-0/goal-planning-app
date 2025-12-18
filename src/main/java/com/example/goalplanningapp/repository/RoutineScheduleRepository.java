package com.example.goalplanningapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.goalplanningapp.entity.RoutineSchedule;
import com.example.goalplanningapp.entity.User;

public interface RoutineScheduleRepository extends JpaRepository <RoutineSchedule, Integer>{
	// ルーティン一覧を取得
	List<RoutineSchedule> findByUserOrderByEffectiveFromDesc(User user);
	// ルーティンがあるかないか
	boolean existsByUser(User user);
	// 最新のルーティン取得（effective_toがnullのみ）
	@Query("""
			select r from RoutineSchedule r 
			where r.user = :user
				and r.effectiveTo is null
			""")
	List<RoutineSchedule> findCurrentByUser(@Param("user")User user);

}
