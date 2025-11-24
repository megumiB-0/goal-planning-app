package com.example.goalplanningapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}
	
	@GetMapping("/login/success")
	public String loginSuccess(RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("successMessage","ログインしました。");
		return "redirect:/home";
	}
	
	@GetMapping("/logout/success")
	public String logoutSuccess(RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("successMessage", "ログアウトしました。");
		return "redirect:/index";
	}

}
