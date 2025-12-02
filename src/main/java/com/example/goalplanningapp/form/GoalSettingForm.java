package com.example.goalplanningapp.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoalSettingForm {
	// 資格に関する項目（コントローラでセット）
	@NotNull(message = "目標資格を選択してください。")
	//資格id
	private String qualificationId;
	//資格名（資格IDがある場合はDBの名前、ない場合は手動入力で使う）
	private String customQualificationName;
	//必要時間(時間)（資格IDがある場合はDBの値、ない場合は手動入力）
	private Double customEstimatedHours;
	
	// ユーザー入力
	@NotNull(message = "開始日を設定してください。")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;
	@NotNull(message = "いつまでに目標達成したいか設定してください。")
	@Future(message = "目標達成日は未来の日付である必要があります。")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate goalDate;
	
	

}
