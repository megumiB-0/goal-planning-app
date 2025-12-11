package com.example.goalplanningapp.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.example.goalplanningapp.dto.DailyTotalDTO;
import com.example.goalplanningapp.dto.LearningRecordDTO;
import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.LearningRecord;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.repository.LearningRecordRepository;

import jakarta.transaction.Transactional;

@Service
public class LearningRecordService {
	// DI
	private final LearningRecordRepository learningRecordRepository;
	
	public LearningRecordService(LearningRecordRepository learningRecordRepository) {
		this.learningRecordRepository = learningRecordRepository;
	}
	
	//POST用
	public LearningRecord createRecord(LearningRecordDTO dto, User user, Goal goal) {
		// String →　　日付型へ
		LocalDate learningDay = LocalDate.parse(dto.getLearningDay());
		LocalTime startTime = LocalTime.parse(dto.getStartTime());
		LocalTime endTime = LocalTime.parse(dto.getEndTime());
		//重複チェック
		if(learningRecordRepository.existsOverlap(user,learningDay,startTime,endTime)) {
			throw new IllegalStateException("この時間帯はすでに登録されています。");
		}
		//未来の日付チェック
		validateNotFuture(learningDay); 
		
		// 学習時間を分で計算
		int learningMinutes = calculateMinutes(startTime,endTime);
		
		LearningRecord record = new LearningRecord();
		
		record.setUser(user);
		record.setGoal(goal);
		record.setLearningDay(learningDay);
		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setLearningMinutes(learningMinutes);
		
		return learningRecordRepository.save(record);
	}

	//GET用
	public List<LearningRecord> getRecords(User user) {
		return learningRecordRepository.findByUser(user);
	}
	

	//PUT用
	@Transactional
	public void updateRecord(Integer id, LearningRecordDTO dto, User user) {
		LearningRecord record = learningRecordRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Record not found"));
		if(!record.getUser().getId().equals(user.getId())) {
			throw new SecurityException("Not allowed");
		}
		//DTO →　LocalDate,LocalTimeへ変換
		LocalDate learningDay = LocalDate.parse(dto.getLearningDay());
		LocalTime startTime = LocalTime.parse(dto.getStartTime());
		LocalTime endTime = LocalTime.parse(dto.getEndTime());
		//重複チェック（自身のIDは除外）
		if(learningRecordRepository.existsOverlapExcludingId(user,learningDay,startTime,endTime,id)) {
			throw new IllegalStateException("この時間帯はすでに登録されています。");
		}
		//未来の日付チェック
		validateNotFuture(learningDay); 
		
		// 学習時間を分で計算
		int learningMinutes = calculateMinutes(startTime,endTime);
		//更新
		record.setLearningDay(learningDay);
		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setLearningMinutes(learningMinutes);
		
		learningRecordRepository.save(record);
	}

	//DELETE用
	@Transactional
	public void deleteRecord(Integer id, User user) {
		LearningRecord record = learningRecordRepository.findById(id)
					.orElseThrow(()-> new IllegalArgumentException("Record not found"));
		if(!record.getUser().getId().equals(user.getId())) {
			throw new SecurityException("Not allowed");
		}
		//物理削除
		learningRecordRepository.delete(record);
	}
	
	//共通メソッド（POSTとPUT）時間計算
	public int calculateMinutes(LocalTime startTime, LocalTime endTime) {
		int startTotalMinutes = startTime.getHour() * 60 + startTime.getMinute();
		int endTotalMinutes;
		// 0:00は24:00として扱う
		if(endTime.equals(LocalTime.MIDNIGHT)) {
			endTotalMinutes = 24 * 60; //24:00の代替
		}else {
			endTotalMinutes = endTime.getHour() * 60 + endTime.getMinute();
		}
		//経過時間（分）を計算
		return endTotalMinutes - startTotalMinutes;
		}
	
	//共通メソッド（POSTとPUT）未来の日付NG
	public void validateNotFuture(LocalDate targetDate) {
		LocalDate today = LocalDate.now();
		if(targetDate.isAfter(today)) {
			throw new IllegalStateException("未来の日付には登録できません。");
		}
	}
	
	//日ごとの合計学習時間計算
	public Map<LocalDate, Long> getDailyTotals(User user){
		List<DailyTotalDTO> dailyTotals = learningRecordRepository.findDailyTotals(user);
		Map<LocalDate, Long> map = new HashMap<>();
		for(DailyTotalDTO dto : dailyTotals) {
			map.put(dto.getLearningDay(), dto.getTotalMinutes());
		}
		return map;
	}
	
	//日ごとの累積学習時間を計算
	public Map<LocalDate,Long> getDailyCumulativeTotals(User user){
		List<DailyTotalDTO> dailyTotals = learningRecordRepository.findDailyTotals(user);
		Map<LocalDate, Long> map = new HashMap<>();
		Long runningTotalMinutes = (long) 0; // 累計学習時間初期値=0
		for(DailyTotalDTO dto : dailyTotals) {
			runningTotalMinutes += dto.getTotalMinutes();
			map.put(dto.getLearningDay(), runningTotalMinutes);
		}
		return map;
	}
	
	//今日時点での累計学習時間を計算
	public Long getTodaysCumulative(@AuthenticationPrincipal User user) {
		Map<LocalDate, Long> cumulativeMap = getDailyCumulativeTotals(user);
		LocalDate today = LocalDate.now();
		Long todaysCumulativeMinutes = cumulativeMap.get(today);
		return todaysCumulativeMinutes/60;
	}
	//今日時点での累計学習時間を計算
	public Long getTodaysRemaining(@AuthenticationPrincipal User user, Goal goal) {
		Long estimatedHours = Math.round(goal.getQualification().getEstimatedMinutes()/60);
		Long todaysCumulativeHours = getTodaysCumulative(user);
		Long remainingHours = estimatedHours - todaysCumulativeHours;
		return remainingHours;
	}
	
	// 今日時点での目標達成度(%)
	public Long getAchievementRate(@AuthenticationPrincipal User user, Goal goal) {
		Long estimatedHours = Math.round(goal.getQualification().getEstimatedMinutes()/60);
		Long todaysCumulativeHours = getTodaysCumulative(user);
		Long achievementRate =(long) Math.round((todaysCumulativeHours / estimatedHours) * 100);
		System.out.println("achievementRate:"+achievementRate);
		System.out.println("todaysCumulativeHours:"+todaysCumulativeHours);
		System.out.println("estimatedHours:"+estimatedHours);
		
		return achievementRate;		
	}
	
	
}


