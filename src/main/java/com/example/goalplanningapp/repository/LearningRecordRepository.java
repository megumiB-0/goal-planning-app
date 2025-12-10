package com.example.goalplanningapp.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.goalplanningapp.entity.LearningRecord;
import com.example.goalplanningapp.entity.User;




public interface LearningRecordRepository extends JpaRepository<LearningRecord, Integer> {
		List<LearningRecord> findByUser(User user);
		
		//新規用
		@Query("""
				SELECT COUNT(r) > 0
				FROM LearningRecord r
				WHERE r.user = :user
				AND r.learningDay = :day
				AND(r.startTime<:end AND r.endTime>:start)
			""")
		boolean existsOverlap(
				@Param("user") User user,
				@Param("day") LocalDate day,
				@Param("start") LocalTime start,
				@Param("end") LocalTime end
				);
		//更新用
		@Query("""
				SELECT COUNT(r) > 0
				FROM LearningRecord r
				WHERE r.user = :user
				AND r.learningDay = :day
				AND(r.startTime<:end AND r.endTime>:start)
				AND r.id <> :excludeId
				
			""")
		boolean existsOverlapExcludingId(
				@Param("user") User user,
				@Param("day") LocalDate day,
				@Param("start") LocalTime start,
				@Param("end") LocalTime end,
				@Param("excludeId") Integer excludeId
				);
}
