const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;


// カレンダー共通部分（初期化）
async function initLearningCalendar({
	calendarEl,
	crudTarget = null,  // 'records'|'plans'|null
	showRoutines = true,
	showPlans = false,
	showRecords = false
}){
	const crudEnabled = crudTarget !== null;
	// 実績の非同期取得
	const dailyTotals = await fetchDailyTotals();
	let plannedTotals = {};
	// 計画の非同期取得
	if(crudTarget === 'plans'){
		plannedTotals = await fetchPlannedTotals();
	}
	
	
	// eventSources
	const eventSources = [];
	
	// routines(常に表示のみ)
	if(showRoutines){
		eventSources.push({
			url: '/routines/calendar',
			editable: false
		});
	}
	// plans
	if(showPlans){
		eventSources.push({
			url: '/api/learning-plans/events',
			editable: crudTarget === 'plans',	// /plan のみ編集可
		})
	}
	
	// records
	if(showRecords){
		eventSources.push({
			url: '/api/learning-records/events',
			editable: crudTarget === 'records',	// /home のみ編集可
		})
	}
	
	// FullCalendarのインスタンスを作る
	const calendar = new FullCalendar.Calendar(calendarEl,{
		initialView: 'timeGridWeek',			// １週間の縦型view
		allDaySlot: false, 						// 終日枠を表示しない
		nowIndicator: true,						// 現在時刻の赤線
		locale: 'ja', 							// 日本語表記 
		firstDay: 1,							// 月曜はじまり
		themeSystem:'bootstrap5',				// BootStrapデザイン
		contentHeight: '800px',
		slotDuration: '00:15:00',				// 15分刻み
		slotLabelInterval: '01:00:00',			// 1時間ごとに時間表示
		
		eventSources,
		
		editable: crudEnabled,					// イベントをドラッグで動かせる	
		selectable: crudEnabled,				// クリックで選択可能
		selectMirror: crudEnabled, 				// selectを使うには必須
		eventDurationEditable: crudEnabled,		// イベント終了時間（duration）を変更できる
		eventStartEditable: crudEnabled,		// イベント開始時間を変更できる
		unselectAuto: false,
		selectOverlap: true, 


		//週末だけヘッダー色を変更する
		dayHeaderDidMount: function(info){
			//週末だけヘッダー色変更
			const day = info.date.getDay(); //0=日,6=土
			const headerEl = info.el.querySelector('a');
			if(!headerEl) return;
			if(day === 0) headerEl.style.color ="#E8B4B4";
			if(day === 6) headerEl.style.color="#A4C0C9";
			
			//ヘッダー合計学習時間表示
if (crudTarget !== null) {
    // 未定義の場合は空オブジェクトにする
    const planned = (typeof plannedTotals !== 'undefined') ? plannedTotals : {};
//    const daily = typeof dailyTotals !== 'undefined' ? dailyTotals : {};
	const daily = dailyTotals || {}; 
    // /planならplanned、/homeならdaily
    const totalsToUse = Object.keys(planned).length > 0 ? planned : daily;

	const year = info.date.getFullYear();
	const month = info.date.getMonth() + 1;
	const day1 = info.date.getDate();
	const dateStr = `${year}-${String(month).padStart(2,'0')}-${String(day1).padStart(2,'0')}`;
    const totalMin = totalsToUse[dateStr] || 0;
	//　計画の場合は今日以降のみ表示
	if(Object.keys(planned).length>0){
		const today = new Date(); today.setHours(0,0,0,0);
		const cellDate = new Date(info.date.getFullYear(), info.date.getMonth(), info.date.getDate());
		if(cellDate < today) return;
	}


    if (totalMin > 0) {
        const totalDiv = document.createElement('div');
        totalDiv.style.fontSize = "0.8rem";
        totalDiv.style.marginTop = "2px";
        totalDiv.style.color = "#778899";
        totalDiv.innerText = `${totalMin}分`;
        info.el.appendChild(totalDiv);
    }
}
		},
       // --- 色分け ---
        eventDidMount: function(info){
            const type = info.event.extendedProps?.type;

            if(type === 'plan'){
                info.el.style.backgroundColor = '#FFD700'; // 黄色
                info.el.style.borderColor = '#B8860B';
            } else if(type === 'record'){
                info.el.style.backgroundColor = '#87CEFA'; // 水色
                info.el.style.borderColor = '#4682B4';
            } else {
                // ルーティンは従来のタイトルで色分け
                switch(info.event.title){
                    case '睡眠':
                        info.el.style.backgroundColor = '#A3D2CA';
                        info.el.style.borderColor = '#273C75';
                        break;
                    case '朝食':
                    case '昼食':
                    case '夕食':
                        info.el.style.backgroundColor = '#d6ceeb';
                        info.el.style.borderColor = '#273C75';
                        break;
                    case '入浴':
                        info.el.style.backgroundColor = '#F28C8C';
                        info.el.style.borderColor = '#273C75';
                        break;
                    case '仕事':
                        info.el.style.backgroundColor = '#8FA998';
                        info.el.style.borderColor = '#273C75';
                        break;
                    default:
                        info.el.style.backgroundColor = '#E4B363';
                        info.el.style.borderColor = '#273C75';
                }
            }
        },
	
	// 新規作成
		// クリックで新規作成（POST）編集可のみ有効
		select: crudEnabled ? async function (info) {
			const apiBase =
				crudTarget === 'plans'
				? '/api/learning-plans'
				: '/api/learning-records';	
			
			const minutes = (new Date(info.end) - new Date(info.start)) / 60000;
			
			const body = {
				day: info.startStr.substring(0,10),
				startTime: info.startStr.substring(11,16),
				endTime: info.endStr.substring(11,16),
				minutes: minutes
			};
			
			const res =await fetch(`${apiBase}/create`,{
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
				title: saved.title,
				start: saved.start,
				end: saved.end,
				extendedProps: saved.extendedProps 
			});
		} : undefined,
		
	//イベントクリックで削除(DELETE)(イベントクリックの誤動作防止) crudEnabled = 'records' または 'plans' の場合のみ削除可能
		eventClick: crudEnabled ? eventClickForPlansOrRecords : undefined,
	//ドラッグで時間変更（PUT）	 編集可のみ
	 	eventDrop: crudEnabled ? updateEvent : undefined,
		eventResize: crudEnabled ? updateEvent : undefined,
	});
	return calendar;
//	calendar.render();
	
	// 共通の　PUT処理
	async function updateEvent(info){
		const apiBase =
		crudTarget === 'plans'
			? '/api/learning-plans'
			: '/api/learning-records';
			
		const body = {
			day: info.event.startStr.substring(0,10),
			startTime: info.event.startStr.substring(11,16),
			endTime: info.event.endStr.substring(11,16),
			minutes:
				(new Date(info.event.end) - new Date(info.event.start))/60000
		};
		
		const res = await fetch(`${apiBase}/${info.event.id}`,{
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
	
		//イベントクリックで削除(DELETE)(イベントクリックの誤動作防止) crudEnabled = 'records' または 'plans' の場合のみ削除可能
		async function eventClickForPlansOrRecords(info){
		
//			if(!confirm("この記録を削除しますか?")) return;
			//routineは操作不可
			if(info.event.extendedProps.type === 'routine')return;
			if(!confirm("この記録を削除しますか?")) return;
			
			const apiBase =
				crudTarget === 'plans'
				? '/api/learning-plans'
				: '/api/learning-records';	

			const res = await fetch(`${apiBase}/${info.event.id}`,{
				method:'DELETE',
				headers:{[csrfHeader]: csrfToken}
			});
			if(res.ok){
				info.event.remove();
			}else{
				alert("削除に失敗しました。");
			}
	}
	
	//日ごとの合計学習時間を取得
	async function fetchDailyTotals(){
		const res = await fetch('/api/learning-records/daily-totals');
		console.log('Response status:', res.status);
		if(!res.ok)return new Map();
		const jsonData = await res.json();
		console.log('Planned totals:', jsonData); 
//		const map = new Map();
//		Object.entries(jsonData).forEach(([day,totalMinutes]) => {map.set(day, totalMinutes)});
//		return map;
		return jsonData
	}
	//日ごとの合計学習計画を取得
	async function fetchPlannedTotals(){
		const res = await fetch('/api/learning-plans/planned-totals');
		if(!res.ok)return new Map();
		console.log('Response status:', res.status);
		const jsonData = await res.json();
		console.log('Planned totals:', jsonData); 
		return jsonData
	}
}