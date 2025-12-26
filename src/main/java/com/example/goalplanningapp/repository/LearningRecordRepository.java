package com.example.goalplanningapp.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.goalplanningapp.dto.DailyTotalDTO;
import com.example.goalplanningapp.entity.LearningRecord;
import com.example.goalplanningapp.entity.User;




public interface LearningRecordRepository extends JpaRepository<LearningRecord, Integer> {
	// ログインユーザーのデータリストを抽出する
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
		
	//日ごとの合計時間抽出
	@Query("""
			SELECT new com.example.goalplanningapp.dto.DailyTotalDTO(
				r.learningDay,
				SUM(r.learningMinutes)
			)
			FROM LearningRecord r
			WHERE r.user = :user
			GROUP BY r.learningDay
			ORDER BY r.learningDay
			""")
	List<DailyTotalDTO>findDailyTotals(@Param("user")User user);
	
	// GoalIdリスト(同一rootを持つもの)に紐づく日ごとの合計学習時間
	@Query("""
			SELECT new com.example.goalplanningapp.dto.DailyTotalDTO(
				l.learningDay,
				SUM(l.learningMinutes)	
			)
			FROM LearningRecord l 
			WHERE l.user = :user AND l.goal.id IN :goalIds
			GROUP BY l.learningDay
			ORDER BY l.learningDay ASC
			""")
	List<DailyTotalDTO> findDailyTotalsByGoalIds(@Param("user") User user,
												 @Param("goalIds") List<Integer> goalIds);
	
	// goalId リストに紐づく学習記録を取得
	List<LearningRecord> findByUserAndGoalIdInOrderByLearningDayAscStartTimeAsc(User user, List<Integer> goalIds);
		
}
