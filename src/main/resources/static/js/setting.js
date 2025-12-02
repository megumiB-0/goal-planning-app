document.addEventListener("DOMContentLoaded",function(){
	const select = document.getElementById("qualificationSelect");
	const manualName = document.getElementById("manualNameArea");
	const manualHours = document.getElementById("manualHoursArea");
	const hoursInput = document.getElementById("customEstimatedHours");
	
	//初期状態　→　手動入力を非表示
	manualName.style.display="none";
	manualHours.style.display="none";
	
	select.addEventListener("change", function(){
		const selectedOption = select.options[select.selectedIndex];
		const minutes =selectedOption.getAttribute("data-minutes");
		
		    console.log("selectedOption:", selectedOption);
		    console.log("minutes:", minutes);
		
		
		if(select.value ==="manual"){
			// 手動入力を選んだ時
			manualName.style.display="block";
			manualHours.style.display="block";
			hoursInput.value = ""; //空にする
		}else if(minutes){
			//既存資格を選んだ時　→　minutesを時間に変換
			manualName.style.display="none";
			manualHours.style.display="block";
			hoursInput.value = (minutes / 60).toFixed(1); //時間換算
		}else{
			// 未選択時
			manualName.style.display="none";
			manualHours.style.display="none";
			hoursInput.value = "";		
		}
	});
});