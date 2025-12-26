package com.example.goalplanningapp.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QualificationRegisterForm {
	private Integer id; // 編集時のみ使用
	@NotBlank(message = "資格名を入れてください。")
	private String name;
	@NotNull(message = "必要学習時間を入力してください。")
	@Min(value = 1, message = "必要学習時間は1時間以上に設定してください。")
	private Double estimatedHours;
	
	private Integer rootQualificationId;
}
