package com.example.goalplanningapp.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.goalplanningapp.dto.DailyTotalDTO;
import com.example.goalplanningapp.entity.LearningPlan;
import com.example.goalplanningapp.entity.User;

public interface LearningPlanRepository extends JpaRepository<LearningPlan, Integer> {
	// ログインユーザーのデータリストを抽出する
	List<LearningPlan> findByUser(User user);
		
		//新規用
		@Query("""
				SELECT COUNT(p) > 0
				FROM LearningPlan p
				WHERE p.user = :user
				AND p.planningDay = :day
				AND(p.startTime<:end AND p.endTime>:start)
			""")
		boolean existsOverlap(
				@Param("user") User user,
				@Param("day") LocalDate day,
				@Param("start") LocalTime start,
				@Param("end") LocalTime end
				);
		//更新用
		@Query("""
				SELECT COUNT(p) > 0
				FROM LearningPlan p
				WHERE p.user = :user
				AND p.planningDay = :day
				AND(p.startTime<:end AND p.endTime>:start)
				AND p.id <> :excludeId
				
			""")
		boolean existsOverlapExcludingId(
				@Param("user") User user,
				@Param("day") LocalDate day,
				@Param("start") LocalTime start,
				@Param("end") LocalTime end,
				@Param("excludeId") Integer excludeId
				);
		
	//日ごとの合計時間抽出
	@Query("""
			SELECT new com.example.goalplanningapp.dto.DailyTotalDTO(
				p.planningDay,
				SUM(p.planningMinutes)
			)
			FROM LearningPlan p
			WHERE p.user = :user
			GROUP BY p.planningDay
			ORDER BY p.planningDay
			""")
	List<DailyTotalDTO>findDailyTotals(@Param("user")User user);

		
}