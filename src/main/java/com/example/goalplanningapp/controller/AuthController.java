package com.example.goalplanningapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.goalplanningapp.form.SignupForm;
import com.example.goalplanningapp.service.UserService;

@Controller
public class AuthController {
	private UserService userService;
	public AuthController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping("/login")
	public String login(
			@RequestParam(value = "withdraw", required = false) String withdraw,
			Model model
	) {
		if(withdraw != null) {
			model.addAttribute("successMessage","退会しました。これまでありがとうございました。");
		}
		return "auth/login";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("signupForm", new SignupForm());
		return "auth/signup";
	}
	
	@PostMapping("/signup")
	public String signup(@ModelAttribute @Validated SignupForm signupForm,
						 BindingResult bindingResult,
						 RedirectAttributes redirectAttributes,
						 Model model) 
	{
		// メールアドレスが登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
		if(userService.isEmailRegistrated(signupForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのアドレスです。");
			bindingResult.addError(fieldError);
			}
		// パスワードとパスワード（確認用）が一致するかチェックする
		if(!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())){
			FieldError fieldError =new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
			bindingResult.addError(fieldError);
		}
		if(bindingResult.hasErrors()) {
			model.addAttribute("signupForm", signupForm);
			return "auth/signup";
		}
		
		userService.createUser(signupForm);
		redirectAttributes.addFlashAttribute("successMessage","会員登録が完了しました。");
		
		return "redirect:/login";
		
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
