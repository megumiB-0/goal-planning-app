package com.example.goalplanningapp.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.security.UserDetailsImpl;
import com.example.goalplanningapp.service.ActiveGoalService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor

public class GlobalHeaderAdvice {

    private final ActiveGoalService activeGoalService;

    @ModelAttribute
    public void addHeaderAttributes(
        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
        Model model
    ) {
    	
        // ★ 未ログイン対策
        if (userDetailsImpl == null) {
            model.addAttribute("hasActiveGoal", false);
            return;
        }
    	
    	User user = userDetailsImpl.getUser();

        model.addAttribute(
            "hasActiveGoal",
            activeGoalService.hasActiveGoal(user)
        );
    }
}
