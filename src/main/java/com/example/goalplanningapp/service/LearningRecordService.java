package com.example.goalplanningapp.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.example.goalplanningapp.dto.DailyTotalDTO;
import com.example.goalplanningapp.dto.LearningTimeDTO;
import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.LearningRecord;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.repository.GoalRepository;
import com.example.goalplanningapp.repository.LearningRecordRepository;

import jakarta.transaction.Transactional;

@Service
public class LearningRecordService {
	// DI
	private final LearningRecordRepository learningRecordRepository;
	private final GoalRepository goalRepository;
	private final GoalService goalService;
	
	public LearningRecordService(LearningRecordRepository learningRecordRepository,
								 GoalRepository goalRepository,
								 GoalService goalService) {
		this.learningRecordRepository = learningRecordRepository;
		this.goalRepository = goalRepository;
		this.goalService = goalService;
	}
	
	//POST用
	public LearningRecord createRecord(LearningTimeDTO dto, User user, Goal goal) {
		// String →　　日付型へ
		LocalDate learningDay = LocalDate.parse(dto.getDay());
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
	public void updateRecord(Integer id, LearningTimeDTO dto, User user) {
		LearningRecord record = learningRecordRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Record not found"));
		if(!record.getUser().getId().equals(user.getId())) {
			throw new SecurityException("Not allowed");
		}
		//DTO →　LocalDate,LocalTimeへ変換
		LocalDate learningDay = LocalDate.parse(dto.getDay());
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
	
	// カレンダー用の学習記録を取得（現在進行中のrootに紐づくGoalの学習記録）
	 public List<LearningRecord> getCalendarEventsForActiveRoots(User user) {
	        // 1. endedAt が null の Goal を取得（現在進行中の root の判定）
	        Goal activeGoal = goalRepository.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(user)
	                .orElse(null);
	        if (activeGoal == null) {
	            return Collections.emptyList();
	        }

	        Integer rootId = activeGoal.getQualification().getRootQualificationId();
	        // 2. root に紐づく全 Goal を取得（終了済みも含む）
	        List<Goal> goalsInRoot = goalRepository.findAllByUserAndQualification_RootQualificationId(user, rootId);
	        List<Integer> goalIds = goalsInRoot.stream()
	                                           .map(Goal::getId)
	                                           .collect(Collectors.toList());

	        if (goalIds.isEmpty()) {
	            return Collections.emptyList();
	        }

	     // 3. LearningRecord を GoalId で取得
	        return learningRecordRepository.findByUserAndGoalIdInOrderByLearningDayAscStartTimeAsc(user, goalIds);
	 }


	
	
	
//	// 進行中のrootQualificationId に紐づくgoalのidを取得
//	private List<Integer> getActiveGoalIds(User user){
//		// 進行中のGoalを１件取得（EndedAtがnull）
//		Optional<Goal> activeGoalOpt = goalRepository.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(user);
//		if(activeGoalOpt.isEmpty()) {
//			return List.of();
//		}
//		Integer rootId = activeGoalOpt.get().getQualification().getRootQualificationId();
//		
//		List<Goal> goals = goalRepository.findAllByUserAndQualification_RootQualificationId(user, rootId);
//		List<Integer> goalIds = goals.stream()
//									 .map(Goal::getId)
//									 .collect(Collectors.toList());
//		return goalIds;
//	}
	
	
	// 共通メソッド（POSTとPUT）時間計算
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
		List<Integer> activeGoalIds = goalService.getActiveGoalIds(user);
		//結果を日付順で返したいのでLinkesHashMapを使う
		Map<LocalDate, Long> map = new LinkedHashMap<>();
		LocalDate today =LocalDate.now();
		//データが0件の時は今日だけ0で返す
		if(activeGoalIds.isEmpty()) {
			map.put(today,0L);
			return map;
		}
		List<DailyTotalDTO> dailyTotals = learningRecordRepository.findDailyTotalsByGoalIds(user,activeGoalIds);
				
		if(dailyTotals.isEmpty()) {
			map.put(today, 0L);
			return map;	
			}
		
		//日付順にソートをかける
		dailyTotals.sort(Comparator.comparing(DailyTotalDTO::getLearningDay));		
		//学習開始日と今日の日付を取得
		LocalDate start = dailyTotals.get(0).getLearningDay();
		//List(dailyTotals)をマップにする
		Map<LocalDate,Long>dailyMap = dailyTotals.stream().collect(Collectors.toMap(
				DailyTotalDTO::getLearningDay,
				DailyTotalDTO::getTotalMinutes));
		//学習開始日と今日まで1日ずつマップを埋める
		for(LocalDate date = start; !date.isAfter(today);date = date.plusDays(1)) {
			//データがある場合はその値、ない場合は0
			map.put(date, dailyMap.getOrDefault(date, 0L));
		}
		return map;
	}
	
	//日ごとの累積学習時間(分)を計算
	public Map<LocalDate,Long> getDailyCumulativeTotals(User user){
		Map<LocalDate,Long> dailyTotals = getDailyTotals(user);
		Map<LocalDate,Long> cumulativeMap = new LinkedHashMap<>();
		// 累積　初期は0
		long cumulative = 0L;
		for(Map.Entry<LocalDate, Long> entry : dailyTotals.entrySet()) {
			cumulative += entry.getValue();
			cumulativeMap.put(entry.getKey(), cumulative);
		}
		return cumulativeMap;
	}
		
	
	//今日時点での累計学習時間(分)を計算
	public Long getTodaysCumulative(User user) {
		Map<LocalDate, Long> map = getDailyCumulativeTotals(user);
		LocalDate today = LocalDate.now();
		Long todaysCumulativeMinutes = map.getOrDefault(today,0L);
		return todaysCumulativeMinutes;
	}
	
//--進捗計算--
	//共通部分
	private Map<String, Long> getProgressValues(User user,Goal goal){
		Long estimatedMinutes =  Math.round(goal.getQualification().getEstimatedMinutes());
		Long todaysCumulativeMinutes = getTodaysCumulative(user);
		Long remainingDays = ChronoUnit.DAYS.between(LocalDate.now().plusDays(1), goal.getGoalDate());
		Map<String, Long> map =new LinkedHashMap<>();
		map.put("estimatedMinutes", estimatedMinutes);
		map.put("todaysCumulativeMinutes",todaysCumulativeMinutes);
		map.put("remainingDays", remainingDays);
		return map;
	}
	
	// 今日時点での目標達成度(%)
	public Long getAchievementRate(User user, Goal goal) {
		Map<String, Long> v = getProgressValues(user, goal);
		double rate = (double)v.get("todaysCumulativeMinutes") / v.get("estimatedMinutes");
		Long achievementRate = Math.round(rate * 100);
		return achievementRate;
	}	

	
	//今日時点での残りの学習時間(時間)を計算
	public Long getTodaysRemaining(User user, Goal goal) {
		Map<String, Long> v = getProgressValues(user, goal);
		Long remainingHours = v.get("estimatedMinutes")/60 - v.get("todaysCumulativeMinutes")/60;
		return remainingHours;
	}

	// 目標達成日までの1日あたりの必要学習時間(分)	
	public Double getEstimatedPerDay(@AuthenticationPrincipal User user,Goal goal) {
		Map<String, Long> v = getProgressValues(user, goal);
		Long remainingMinutes = v.get("estimatedMinutes") - v.get("todaysCumulativeMinutes");
		Double estimatedPerDay = (double) (remainingMinutes/v.get("remainingDays"));
		return estimatedPerDay;
	}
	
	//進捗評価
	public String evaluateProgress(User user,Goal goal) {
		Map<String, Long> v = getProgressValues(user, goal);
		
		LocalDate start = goal.getStartDate();
		LocalDate end = goal.getGoalDate();
		LocalDate today = LocalDate.now();
		long totalDays = ChronoUnit.DAYS.between(start, end);
		long passedDays = ChronoUnit.DAYS.between(start, today);
		//期間の経過（％）		
		double dayProgress = (double) passedDays / totalDays * 100;
		//学習時間の進捗率(%)		
		double studyProgress = (double) v.get("todaysCumulativeMinutes")/v.get("estimatedMinutes") * 100;
		//評価
		double diff = studyProgress / dayProgress;
		if(diff >=1) {			//まる
			return "bi bi-sun-fill text-success︎";
		}else if(diff >= 0.8) {	//さんかく
			return "bi bi-cloud-fill text-warning";
		}else {					//ばつ
			return "bi bi-umbrella-fill text-dark";
		}
	}
	
}


