package com.example.goalplanningapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.goalplanningapp.entity.Goal;

public interface GoalRepository extends JpaRepository<Goal, Integer> {

}
