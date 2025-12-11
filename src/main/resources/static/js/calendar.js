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
		firstDay: 1,							// 月曜はじまり
		themeSystem:'bootstrap5',				// BootStrapデザイン
		contentHeight: '500px',
		slotDuration: '00:15:00',				// 15分刻み
		slotLabelInterval: '01:00:00',			// 1時間ごとに時間表示
		editable: true,							// イベントをドラッグで動かせる

		//週末だけヘッダー色を変更する
		dayHeaderDidMount: function(info){
			const day = info.date.getDay(); //0=日,6=土
			const headerEl = info.el.querySelector('a');
			if(!headerEl)return;
			if(day === 0){
				headerEl.style.color ="#E8B4B4";
			}
			if(day === 6){
				headerEl.style.color="#A4C0C9";
			}
		},


		//追加
		eventDurationEditable: true,			// イベント終了時間（duration）を変更できる
		eventStartEditable: true,				// イベント開始時間を変更できる
		unselectAuto: false,
		selectOverlap: true, 
		
		// 初期イベントの読み込み(GET) SpringBootのAPI
		events: '/api/learning-records/events',

		
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

			calendar.unselect();  // 選択状態を解除する（タイトルが表示されない解決策）
			
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