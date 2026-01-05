package com.example.goalplanningapp.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeForm {
	@NotBlank
	private String currentPassword;
	@NotBlank
	@Size(min = 8, message = "8文字以上にしてください。")
	private String newPassword;
	@NotBlank
	private String confirmPassword;
}
