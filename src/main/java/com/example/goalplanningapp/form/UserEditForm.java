package com.example.goalplanningapp.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.goalplanningapp.entity.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

@Data
public class UserEditForm {
	
	@NotBlank
	private String name;
	
	@Email
	@NotBlank
	private String email;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull
	@Past
	private LocalDate dateOfBirth;
	
	private Gender gender;

}
