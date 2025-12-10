const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;


document.addEventListener('DOMContentLoaded',async function(){
	//カレンダー描画要素
	const calendarEl = document.getElementById('calendar');
	// FullCalendarのインスタンスを作る
	const calendar = new FullCalendar.Calendar(calendarEl,{
		initialView: 'timeGridWeek',			// １週間の縦型view
		allDaySlot: false, 						// 終日枠を表示しない
		nowIndicator: true,						// 現在時刻の赤線
		selectable: true,						// クリックで選択可能
		selectMirror: true,						// selectを使うには必須
		locale: 'ja', 							// 日本語表記 
		themeSystem:'bootstrap5',				// BootStrapデザイン
		contentHeight: '350px',
		//slotDuration: '00:30:00',				// 30分刻み
		editable: true,							// イベントをドラッグで動かせる
		//追加
		eventDurationEditable: true,
		eventStartEditable: true,
		unselectAuto: false,
		selectOverlap: true, 
		
		// 初期イベントの読み込み(GET) SpringBootのAPI
		events: '/api/learning-records/events',
		
		/*async function (fetchInfo, successCallback, failureCallback){
			try{
				const res = await fetch('/api/learning-records/events');
				const data = await res.json();
				//サーバーのレコードをFullCalender形式に変換
				const events = data.map(record =>({
					id: record.id,
					title: record.learningMinutes + "分",
					start: record.learningDay + "T" + record.startTime,
					end: record.learningDay + "T" + record.endTime
				}));
				//FullCalendarに渡す
				successCallback(events);
			}catch(error){
				failureCallback(error);
			}
		},*/
		
		// クリックで新規作成（POST）
		select: async function (info) {
			const minutes = (new Date(info.end) - new Date(info.start)) / 60000;
			
			const body = {
				learningDay: info.startStr.substring(0,10),
				startTime: info.startStr.substring(11,16),
				endTime: info.endStr.substring(11,16),
				learningMinutes: minutes
			};
			
			const res =await fetch('/api/learning-records/create',{
				method: 'POST',
				headers: {"Content-Type": "application/json",
						   [csrfHeader]: csrfToken
						  },
				body: JSON.stringify(body)
			});
			
			//エラーがあれば画面表示
			if(!res.ok){
				const err = await res.json(); 	// JSONを取得
				alert(err.message);				// サーバーからくるmessageを表示
				return;							// リターン（イベント追加しない）
			}
						
			const saved =await res.json();
			// カレンダーに即時反映（これにより複数登録可能）
			calendar.addEvent({
				id: saved.id,
				title: body.learningMinutes + "分",
				start: saved.start,
				end: saved.end
			});
		},

		//イベントクリックで削除(DELETE)(イベントクリックの誤動作防止)
		eventClick: async function(info){
			if(!confirm("この記録を削除しますか?")) return;
			info.jsEvent.preventDefault(); //accidental dragを無効化
			const id = info.event.id;
			const res = await fetch(`/api/learning-records/${id}`,{
				method:'DELETE',
				headers:{
					"Content-Type":"application/json",
					[csrfHeader]: csrfToken
				}
			});
			if(res.ok){
				info.event.remove();
			}else{
				alert("削除に失敗しました。");
			}
		},

		
		//ドラッグで時間変更（PUT）
		eventDrop: async function (info) {
			await updateEvent(info);
		},
		eventResize: async function(info){
			await updateEvent(info);
		}
	});
	
	calendar.render();
	
	// 共通の　PUT処理
	async function updateEvent(info){
		const id = info.event.id;
		const body = {
			learningDay: info.event.startStr.substring(0,10),
			startTime: info.event.startStr.substring(11,16),
			endTime: info.event.endStr.substring(11,16),
			learningMinutes:
				(new Date(info.event.end) - new Date(info.event.start))/60000
		};
		
		const res = await fetch(`/api/learning-records/${id}`,{
			method: 'PUT',
			headers: {"Content-Type": "application/json",
					  [csrfHeader]: csrfToken},
			body: JSON.stringify(body)
		});
		//エラーがあれば画面表示
		if(!res.ok){
			const err = await res.json();
			alert(err.message);			// エラー表示
			info.revert();				// FullCalendarのドラッグ変更を元に戻す
			return;
		}

	}
});