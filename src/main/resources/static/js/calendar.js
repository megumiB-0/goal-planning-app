document.addEventListener('DOMContentLoaded',function(){
	const calendarEl = document.getElementById('calendar');
	
	const calendar = new FullCalendar.Calendar(calendarEl,{
		initialView: 'timeGridWeek',			// １週間の縦型view
		allDaySlot: false, 						// 終日枠を表示しない
		nowIndicator: true,						// 現在時刻の赤線
		selectable: true,						// クリックで選択可能
//		events: events,							// SpringBootのAPI
		locale: 'ja', 							// 日本語表記 
		themeSystem:'bootstrap5',				// BootStrapデザイン
		editable: true,							// イベントをドラッグで動かせる
		slotDuration: '00:30:00',				// 30分刻み
		contentHeight: '350px'
		
	});
	
	calendar.render();

});