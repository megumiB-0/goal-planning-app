package com.example.goalplanningapp.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		//結果を日付順で返したいのでLinkesHashMapを使う
		Map<LocalDate, Long> map = new LinkedHashMap<>();
		//データが0件の時は今日だけ0で返す
		if(dailyTotals == null || dailyTotals.isEmpty()) {
			map.put(LocalDate.now(),0L);
			return map;
		}
		//日付順にソートをかける
		dailyTotals.sort(Comparator.comparing(DailyTotalDTO::getLearningDay));		
		//学習開始日と今日の日付を取得
		LocalDate start = dailyTotals.get(0).getLearningDay();
		LocalDate today = LocalDate.now();
		//List(dailyTotals)をマップにする
		Map<LocalDate,Long>dailyTotalMap = dailyTotals.stream().collect(Collectors.toMap(
				DailyTotalDTO::getLearningDay,
				DailyTotalDTO::getTotalMinutes));
		//学習開始日と今日まで1日ずつマップを埋める
		for(LocalDate date = start; !date.isAfter(today);date = date.plusDays(1)) {
			//データがある場合はその値、ない場合は0
			Long minutes = dailyTotalMap.getOrDefault(date, 0L);
			map.put(date, minutes);
		}
		return map;
		/*
		for(DailyTotalDTO dto : dailyTotals) {
			map.put(dto.getLearningDay(), dto.getTotalMinutes());
		}
		return map;
		*/
	}
	
	//日ごとの累積学習時間(分)を計算
	public Map<LocalDate,Long> getDailyCumulativeTotals(User user){
		List<DailyTotalDTO> dailyTotals = learningRecordRepository.findDailyTotals(user);
		
		//データが0件の場合、空のマップを返す
		if(dailyTotals.isEmpty()) {
			 return new LinkedHashMap<>();
		}
		//get(0)が安全に呼べるようにする
		Map<LocalDate, Long> map = new LinkedHashMap<>();
		//ソートをかける
		dailyTotals.sort(Comparator.comparing(DailyTotalDTO::getLearningDay));
		// 累計学習時間初期値=0
		Long cumulativeMinutes = (long) 0;
		//学習開始日と今日の日付を取得
		LocalDate start = dailyTotals.get(0).getLearningDay();
		LocalDate today = LocalDate.now();
		Map<LocalDate, Long> dailyMap = dailyTotals.stream().collect(Collectors.toMap(
				DailyTotalDTO::getLearningDay,
				DailyTotalDTO::getTotalMinutes
			));
		// 学習開始日から直近学習した日までをfor分で回す
		for(LocalDate date = start; !date.isAfter(today);date = date.plusDays(1)) {
			//データがある場合は累積に加算
			if(dailyMap.containsKey(date)) {
				cumulativeMinutes += dailyMap.get(date);
			}
			//データがない日は　cumulativeMinutesのまま（=直前の値）
			map.put(date, cumulativeMinutes);
		}
			System.out.println("cumulativeMinutes="+ cumulativeMinutes);

		return map;
		}
	
	
	
	
/*	public Map<LocalDate,Long> getDailyCumulativeTotals(User user){
		List<DailyTotalDTO> dailyTotals = learningRecordRepository.findDailyTotals(user);
		Map<LocalDate, Long> map = new HashMap<>();
		Long runningTotalMinutes = (long) 0; // 累計学習時間初期値=0
		for(DailyTotalDTO dto : dailyTotals) {
			runningTotalMinutes += dto.getTotalMinutes();
			map.put(dto.getLearningDay(), runningTotalMinutes);
		}
		return map;
	}
*/	
	//今日時点での累計学習時間(分)を計算
	public Long getTodaysCumulative(@AuthenticationPrincipal User user) {
		Map<LocalDate, Long> map = getDailyCumulativeTotals(user);
		LocalDate today = LocalDate.now();
		Long todaysCumulativeMinutes = map.getOrDefault(today,0L);
		return todaysCumulativeMinutes;
	}
	
	//今日時点での残りの学習時間(時間)を計算
	public Long getTodaysRemaining(@AuthenticationPrincipal User user, Goal goal) {
		Long estimatedMinutes = Math.round(goal.getQualification().getEstimatedMinutes());
		Long todaysCumulativeMinutes = getTodaysCumulative(user);
		Long remainingHours = estimatedMinutes/60 - todaysCumulativeMinutes/60;
		return remainingHours;
	}
	
	// 今日時点での目標達成度(%)
	public Long getAchievementRate(@AuthenticationPrincipal User user, Goal goal) {
		Double estimatedMinutes = goal.getQualification().getEstimatedMinutes();
		Long todaysCumulativeMinutes = getTodaysCumulative(user);
		double rate = (double)todaysCumulativeMinutes/estimatedMinutes;
		Long achievementRate =(long) Math.round(rate * 100);
		System.out.println("achievementRate:"+achievementRate);
		System.out.println("todaysCumulativeMinutes:"+todaysCumulativeMinutes);
		System.out.println("estimatedMinutes:"+estimatedMinutes);
		
		return achievementRate;		
	}
	
	
}


