
// 資格選択ドロップダウン
const qualificationSelect = document.getElementById("qualificationSelect");
// 手動入力エリア
const manualInputArea = document.getElementById("manualInputArea");
// 必要学習時間
const estimatedTime = document.getElementById("estimatedTime");

// 計算ボタン
const calculateButton = document.getElementById("calculateButton");
// 日割り計算メッセージ表示エリア
const msgArea = document.getElementById("studyMessage");
// 資格を選んだ時の処理
qualificationSelect.addEventListener("change", () => {
		const selected = qualificationSelect.value;
	// その他（手動入力）　を選んだ時
	if(selected === "manual"){
		manualInputArea.style.display = "block";	// 手動入力欄を表示
		estimatedTime.value = "";	// 時間を空にする
		estimatedTime.readOnly = false; // 編集可能
		return;
	}
	// DB資格を選んだ時、自動入力モードに戻す
	manualInputArea.style.display = "none";
	
	// 選択したoptionの学習時間（分）を取得
	const option = qualificationSelect.options[qualificationSelect.selectedIndex];
	const minutes = option.dataset.minutes;
	// 時間に変換し自動入力
	if(minutes){
	const hours = (minutes / 60).toFixed(2);
	estimatedTime.value = hours;
	estimatedTime.readOnly = true;		
	}else{
		estimatedTime.value = "";
		estimatedTime.readOnly = false;
	}
});	
// 計算ボタン（1日・１週間の学習時間を表示）
calculateButton.addEventListener("click", function(){
	const start = new Date(document.getElementById("startDate").value);
	const goal = new Date(document.getElementById("goalDate").value);
	const estimatedHours = parseFloat(estimatedTime.value);
	
	if (!start || !goal || isNaN(estimatedHours)){
		alert("開始日・目標び・必要学習時間を全て入力してください。");
		return;
	}
	//　日数計算
	const diffDays = Math.ceil((goal - start) / (1000 * 60 * 60 *24))
	if (diffDays <= 0){
		alert("目標達成日は開始日より後の日付を設定してください。");
		return
	}
	
	const hoursPerDay = (estimatedHours / diffDays).toFixed(2);
	const hoursPerWeek = (hoursPerDay * 7).toFixed(2);
	msgArea.style.display ="block";
	msgArea.innerHTML =
	`【計算結果】
	<br>目標達成までに必要学習時間：<strong>${estimatedHours}時間</strong>
	<br>日数<strong>${diffDays}日</strong>
	<br><br>
	1日に必要な学習時間<strong>${hoursPerDay}時間</strong>
	<br>1週間に必要な学習時間：<strong>${hoursPerWeek}時間</strong>`;
})
	

