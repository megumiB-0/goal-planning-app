package com.example.goalplanningapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.goalplanningapp.form.SignupForm;

@Controller
public class AuthController {
	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("signupForm", new SignupForm());
		return "auth/signup";
	}
/*	
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
*/
}
