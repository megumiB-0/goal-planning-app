package com.example.goalplanningapp.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.form.PasswordChangeForm;
import com.example.goalplanningapp.form.UserEditForm;
import com.example.goalplanningapp.security.UserDetailsImpl;
import com.example.goalplanningapp.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class EditUserController {
	private final UserService userService;
	
	@GetMapping("/edit")
	public String editUser(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						   Model model) {
		User user = userDetailsImpl.getUser();	
		UserEditForm form = new UserEditForm();
		form.setName(user.getName());
		form.setEmail(user.getEmail());
		form.setDateOfBirth(user.getDateOfBirth());
		form.setGender(user.getGender());
		
		model.addAttribute("userEditForm", form);
		return "user/edit";
	}
	
	@PostMapping("/update")
	public String updateUser(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
							 @Validated UserEditForm form,
							 BindingResult bindingResult,
							 RedirectAttributes redirectAttributes,
							 Model model) {
		// メール重複チェック
		if(userService.isEmailDuplicateForEdit(form.getEmail(), userDetailsImpl.getId())) {
			bindingResult.rejectValue(
					"email",
					"duplicate",
					"そのメールアドレスは既に使用されています。"
			);
		}
		
		// バリデーションエラーがある場合
		if(bindingResult.hasErrors()) {
			return "user/edit";
		}
		
		User user = userDetailsImpl.getUser();
		user.setName(form.getName());
		user.setEmail(form.getEmail());
		user.setDateOfBirth(form.getDateOfBirth());
		user.setGender(form.getGender());
		
		userService.updateUser(user,form);
		
		redirectAttributes.addFlashAttribute("successMessage", "会員情報を更新しました。");
		return "redirect:/user/edit";
	}
	
	// パスワード変更画面表示
	@GetMapping("/password")
	public String showPasswordForm(Model model) {
		// 新しいフォームオブジェクトを正しい名前で追加
		if(!model.containsAttribute("passwordChangeForm")) {
			model.addAttribute("passwordChangeForm", new PasswordChangeForm());			
		}
		return "user/password";
	}
	
	// 変更処理
	@PostMapping("/password")
	public String changePassword(
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@Validated @ModelAttribute PasswordChangeForm form,
			BindingResult result,
			RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		// 新パスワード確認
		if(!form.getNewPassword().equals(form.getConfirmPassword())) {
			result.rejectValue("confirmPassword", null, "パスワードが一致しません。");
		}
		
		// 現在のパスワード確認
		if(!userService.checkPassword(user, form.getCurrentPassword())) {
			result.rejectValue("currentPassword", null, "現在のパスワードが違います。");
		}
		
		if(result.hasErrors()) {
			return "user/password";
		}
		
		// 更新
		userService.updatePassword(user, form.getNewPassword());
		
		redirectAttributes.addFlashAttribute("success", "パスワードを変更しました。");
		return "redirect:/logout";
		
	}
	
}
