document.addEventListener("DOMContentLoaded",function() {
	const select = document.getElementById("qualificationSelect");
	const manualName = document.getElementById("manualNameArea");
	const manualHours = document.getElementById("manualHoursArea");
	const hoursInput = document.getElementById("customEstimatedHours");
		
	function updateQualificationUI(){
		const selectOption = select.options[select.selectedIndex];
		const minutes = selectOption?.getAttribute("data-minutes");
		const selectedValue = select.value;
		
		if(select.value ==="-1"){
			// 手動入力を選んだ時（新規・修正共通）
			manualName.classList.remove("d-none");
			manualHours.classList.remove("d-none");
			//value 触らない
			return;
    } else {
        // 手動入力以外（既存資格または未選択）
        manualName.classList.add("d-none");
        if (selectedValue === "") {
            // 未選択
            manualHours.classList.add("d-none");
            hoursInput.value = "";
        } else {
            // 既存資格
            manualHours.classList.remove("d-none");
            hoursInput.value = minutes ? (minutes / 60).toFixed(1) : "";
        }
    
		}
	}
	// 初期表示（戻る・修正・再表示全て対応)
	updateQualificationUI();
	// 変更時
	select.addEventListener("change", updateQualificationUI);
});
