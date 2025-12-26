package com.example.goalplanningapp.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.repository.GoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActiveGoalService {

    private final GoalRepository goalRepository;

    public boolean hasActiveGoal(User user) {
        return goalRepository
            .findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(user)
            .isPresent();
    }

    public Optional<Goal> getActiveGoal(User user) {
        return goalRepository
            .findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(user);
    }
}
