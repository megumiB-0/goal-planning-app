package com.example.goalplanningapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.User;


public interface GoalRepository extends JpaRepository<Goal, Integer> {
	//ユーザーの未完了の目標のうち、開始日が一番新しいものを1件取得
	public Optional<Goal> findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(User user);

}
