package com.example.goalplanningapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.goalplanningapp.dto.DailyTotalDTO;
import com.example.goalplanningapp.entity.Goal;
import com.example.goalplanningapp.entity.Qualification;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.form.GoalSettingForm;
import com.example.goalplanningapp.repository.GoalRepository;
import com.example.goalplanningapp.repository.LearningPlanRepository;
import com.example.goalplanningapp.repository.LearningRecordRepository;
import com.example.goalplanningapp.repository.QualificationRepository;


@Service
public class GoalService {
	//DI
	private final GoalRepository goalRepository;
	private final QualificationRepository qualificationRepository;
	private final LearningPlanRepository learningPlanRepository;
	private final LearningRecordRepository learningRecordRepository;
	private final QualificationService qualificationService;

	
	public GoalService(GoalRepository goalRepository,
					   QualificationRepository qualificationRepository,
					   LearningPlanRepository learningPlanRepository,
					   LearningRecordRepository learningRecordRepository,
					   QualificationService qualificationService,
					   LearningRecordService learningRecordService) {
		this.goalRepository = goalRepository;
		this.qualificationRepository = qualificationRepository;
		this.qualificationService = qualificationService;
		this.learningPlanRepository = learningPlanRepository;
		this.learningRecordRepository = learningRecordRepository;
		
	}
	
	
	@Transactional
	// フォームからデータベース保存
	public Goal saveGoalWithQualification(GoalSettingForm form, User user) {
		// 未完了の目標がすでに存在するか確認
		Optional<Goal> activeGoal = goalRepository.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(user);
		// すでに未完了が1件あったら目標作成不可
		if(activeGoal.isPresent()) {
			throw new IllegalStateException("未完了の目標がすでに存在します。新規に目標作成することはできません。");
		}
		Qualification qualificationEntity;
		// 手動入力の場合はQualificationを作成
		if(form.getQualificationId() == -1) {
			Qualification newQualification = new Qualification();
			newQualification.setName(form.getCustomQualificationName());
			// 必要学習時間を登録
			if(form.getCustomEstimatedHours() != null) {
				newQualification.setEstimatedMinutes(form.getCustomEstimatedHours() * 60);
			}
			//ログインユーザーをセット
			newQualification.setUser(user);
			// 新規登録のNot Null対策
			newQualification.setRootQualificationId(0);
			
			// 新しい資格を保存
			Qualification saved = qualificationService.saveByUser(newQualification);
			saved.setRootQualificationId(saved.getId());
			qualificationEntity = qualificationService.saveByUser(saved);
		}else {
			//既存資格Idを取得
			Integer qualificationId = form.getQualificationId();
			qualificationEntity = qualificationRepository.findById(qualificationId)
							  .orElseThrow(()-> new IllegalArgumentException("資格が見つかりません。"));
		}
		// 目標設定を作成	
		// 各フィールドをフォームからコピー
		Goal goal = new Goal();
		goal.setQualification(qualificationEntity);
		goal.setStartDate(form.getStartDate());
		goal.setGoalDate(form.getGoalDate());
		goal.setUser(user); 

	// DBにエンティティを保存
    return goalRepository.save(goal);
    
    }
	
	//目標を更新（目標達成日を変更 or 必要時間変更した新規資格を作成）
	@Transactional
	public Goal updateCurrentGoal(GoalSettingForm form, User user) {
		Goal current = goalRepository
				.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(user)
				.orElseThrow(() -> new IllegalStateException("修正対象の目標が存在しません。"));
		
		int oldMinutes = (int)Math.round(current.getQualification().getEstimatedMinutes());
		int newMinutes = (int)Math.round(form.getCustomEstimatedHours() * 60);
		
		if(newMinutes == oldMinutes) {
			// 目標達成日だけ更新
			current.setGoalDate(form.getGoalDate());
			return goalRepository.save(current);			
		}
		// 既存目標を終了させる
		current.setEndedAt(LocalDateTime.now());
		goalRepository.save(current);
		
		// 追加目標（新規資格）を作成
		Qualification q = new Qualification();
		q.setName(form.getCustomQualificationName());		
		// 必要学習時間を登録
		q.setEstimatedMinutes(form.getCustomEstimatedHours() * 60);
		//ログインユーザーをセット
		q.setUser(user);
		// rootを設定
		q.setRootQualificationId(current.getQualification().getRootQualificationId());
		
		
		// 新しい資格を保存
		Qualification savedQ = qualificationService.saveByUser(q);

		// 追加目標（新規資格）を設定
		Goal additional = new Goal();
		additional.setUser(user); 
		additional.setQualification(savedQ);
		additional.setStartDate(form.getStartDate());
		additional.setGoalDate(form.getGoalDate());
		
		
	// DBにエンティティを保存
    return goalRepository.save(additional);
	}
	
	
	
    //現在のgoalを取得する
    public Goal getCurrentGoal(User loginUser) {
    	return goalRepository.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(loginUser)
    						 .orElseThrow(()-> new IllegalStateException("現在の目標が見つかりません。"));
    }
    //現在のgoalの開始日を取得する
    public LocalDate getCurrentGoalStartDate(User loginUser) {
    	return goalRepository.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(loginUser)
    						 .map(Goal :: getStartDate)
    						 .orElseThrow(()-> new IllegalStateException("現在の目標が見つかりません。"));
    }
    // 有効目標があるか
	public boolean hasActiveGoal(User user) {
		return goalRepository
				.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(user)
				.isPresent();
	}

	// 目標修正用
	public void updateGoal(GoalSettingForm form, User user) {
		// 現在の有効な目標を取得
		Goal goal = goalRepository
				.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(user)
				.orElseThrow(() -> 
						new IllegalStateException("有効な目標が存在しません。"));
		//目標達成日の更新
		goal.setGoalDate(form.getGoalDate());
		// 必要時間（資格）の更新
		Qualification qualification;
		if(form.getQualificationId() !=null
				&& form.getQualificationId() == -1) {
			// 現在紐づいている資格
			Qualification currentQualification = goal.getQualification();
			// 自分が作った資格かどうか
			boolean isMyQualification =
					currentQualification.getUser() != null
					&& currentQualification.getUser().equals(user.getId());
			if(isMyQualification) {
				//自分が作った資格なら更新
				qualification = currentQualification;
			}else {
				// 公式資格ならコピーして新規作成
				qualification = new Qualification();
				qualification.setName(currentQualification.getName()+ "追加");
				qualification.setUser(user);
			}
			// 時間を更新
			if(form.getCustomEstimatedHours() !=null) {
				qualification.setEstimatedMinutes(
						form.getCustomEstimatedHours() * 60);
			}
			// 保存（更新 & 新規）
			qualificationService.saveByUser(qualification);
		}else {
			//既存資格を選択した場合（時間変更なし）
			qualification = qualificationService
					.findQualificationById(form.getQualificationId())
					.orElseThrow(() ->
					new IllegalArgumentException("資格が見つかりません。"));
		}
		// Goalに資格を再紐づけ
		goal.setQualification(qualification);
		// Goal保存
		goalRepository.save(goal);
	}

	// 目標終了時のgoal,planの処理
	@Transactional
	public void finishCurrentGoal(User user) {
		Goal current = getCurrentGoal(user);
		if(current == null) {
			throw new IllegalStateException("有効な目標がありません。");
		}
		//goalを終了（EndedAtを入力）
		current.setEndedAt(LocalDateTime.now());
		goalRepository.save(current);
		// このroot以外の計画を削除（計画物理削除）
		deletePlansNotInActiveRoot(user);
		
		return;
	}
	
	// 進行中のrootQualificationId に紐づくgoalのidを取得
	List<Integer> getActiveGoalIds(User user){
		// 進行中のGoalを１件取得（EndedAtがnull）
		Optional<Goal> activeGoalOpt = goalRepository.findFirstByUserAndEndedAtIsNullOrderByStartDateDesc(user);
		if(activeGoalOpt.isEmpty()) {
			return List.of();
		}
		Integer rootId = activeGoalOpt.get().getQualification().getRootQualificationId();
		
		List<Goal> goals = goalRepository.findAllByUserAndQualification_RootQualificationId(user, rootId);
		List<Integer> goalIds = goals.stream()
									 .map(Goal::getId)
									 .collect(Collectors.toList());
		return goalIds;
	}
	// 終了したrootに紐づく計画を削除する
	 public void deletePlansNotInActiveRoot(User user) {
		List<Integer> activeGoalIds = getActiveGoalIds(user);
		if(activeGoalIds.isEmpty()) {
			// 進行中Goalがない　＝　全削除
			learningPlanRepository.deleteByUser(user);
			return;
		}
		//activeGoalIdsに含まれない計画を削除
		learningPlanRepository
				.deleteByUserAndGoalIdNotIn(user, activeGoalIds);
	}
	
		//日ごとの合計学習時間計算
		public Map<LocalDate, Long> getDailyTotals(User user){
			List<Integer> activeGoalIds = getActiveGoalIds(user);
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
			if(diff >=1.1) {	//二重まる
				return "bi bi-fire";
			}else if(diff >= 1) {	//まる
				return "bi bi-sun-fill︎";
			}else if(diff > 0.9) {	//さんかく
				return "bi bi-cloud-fill";
			}else {					//ばつ
				return "bi bi-umbrella-fill";
			}
		}	
		
		
	// 目標達成判定
	public boolean isGoalAchieved(User user, Goal goal) {
		Long cumulative = getTodaysCumulative(user);
		Long estimated = Math.round(goal.getQualification().getEstimatedMinutes());
		return cumulative >= estimated;
	}
	 
	// 目標達成時の処理
	@Transactional
	public void completeGoal(User user) {
		finishCurrentGoal(user);
	}
	
	
}