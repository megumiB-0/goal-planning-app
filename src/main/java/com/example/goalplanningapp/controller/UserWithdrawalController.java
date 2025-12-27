package com.example.goalplanningapp.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.security.UserDetailsImpl;
import com.example.goalplanningapp.service.UserWithdrawalService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/user")
public class UserWithdrawalController {
	// DI
	private final UserWithdrawalService withdrawalService;
	public UserWithdrawalController(UserWithdrawalService withdrawalService) {
		this.withdrawalService = withdrawalService;
	}
	
	@GetMapping("/withdraw")
	public String confirmWithdraw() {
		return "user/withdraw_confirm";
	}
	
	@PostMapping("/withdraw")
	public String withdraw(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						   HttpServletRequest request,
						   HttpServletResponse response) {
		User user = userDetailsImpl.getUser();
		withdrawalService.withdraw(user);
		
		//ログアウト処理
		new SecurityContextLogoutHandler().logout(request, response, null);
		return "redirect:/login?withdraw";
		
	}

}
