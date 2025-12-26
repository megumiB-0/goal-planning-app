document.addEventListener("DOMContentLoaded",function(){
	const select = document.getElementById("qualificationSelect");
	const manualName = document.getElementById("manualNameArea");
	const manualHours = document.getElementById("manualHoursArea");
	const hoursInput = document.getElementById("customEstimatedHours");
		
	function updateQualificationUI(){
		const selectOption = select.options[select.selectedIndex];
		const minutes = selectOption?.getAttribute("data-minutes");
		
		if(select.value ==="-1"){
			// 手動入力を選んだ時（新規・修正共通）
			manualName.classList.remove("d-none");
			manualHours.classList.remove("d-none");
			//value 触らない
			return;
		}
		if(minutes){
			//既存資格を選んだ時　→　minutesを時間に変換
			manualName.classList.add("d-none");
			manualHours.classList.remove("d-none");
			hoursInput.value = (minutes / 60).toFixed(1); //時間換算
			return;
		}
			// 未選択時
			manualName.classList.add("d-none");
			manualHours.classList.add("d-none");
			hoursInput.value = "";		
	}	
	// 初期表示（戻る・修正・再表示全て対応
	updateQualificationUI();
	// 変更時
	select.addEventListener("change", updateQualificationUI);
});