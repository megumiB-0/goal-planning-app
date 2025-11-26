package com.example.goalplanningapp.form;

import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import com.example.goalplanningapp.entity.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

@Data
public class SignupForm {
	@NotBlank(message = "氏名を入力してください。")
	private String name;

	@NotNull(message = "生年月日を入力してください。")
	@Past(message = "生年月日は未来の日付にできません。")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfBirth;
	@NotNull(message = "性別を選択してください。")
	private Gender gender;
	
	@NotBlank(message = "メールアドレスを入力してください。")
	@Email(message = "メールアドレスは正しい形で入力してください。")
	private String email;
	@NotBlank(message = "パスワードを入力してください。")
	@Length(min = 8,message = "パスワードは8文字以上で入力してください。")
	private String password;
	@NotBlank(message = "パスワード(確認用)を入力してください。")
	private String passwordConfirmation;
	

}
