const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

// カレンダー共通部分

async function initLearningCalendar({
	calendarEl,
	crudTarget = null,  // 'records'|'plans'|null
	showRoutines = true,
	showPlans = false,
	showRecords = false
}){
	const crudEnabled = crudTarget !== null;
	const dailyTotals = await fetchDailyTotals();
	
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
			editable: crudTarget === 'plans'
		})
	}
	
	// records
	if(showRecords){
		eventSources.push({
			url: '/api/learning-records/events',
			editable: crudTarget === 'records'
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
    const planned = typeof plannedTotals !== 'undefined' ? plannedTotals : {};
    const daily = typeof dailyTotals !== 'undefined' ? dailyTotals : {};

    // /planならplanned、/homeならdaily
    const totalsToUse = Object.keys(planned).length > 0 ? planned : daily;

    const year = info.date.getFullYear();
    const month = info.date.getMonth() + 1;
    const day1 = info.date.getDate();
    const dateStr = `${year}-${String(month).padStart(2,'0')}-${String(day1).padStart(2,'0')}`;

    const totalMin = totalsToUse[dateStr] || 0;

    if (totalMin > 0) {
        const totalDiv = document.createElement('div');
        totalDiv.style.fontSize = "0.8rem";
        totalDiv.style.marginTop = "2px";
        totalDiv.style.color = "#778899";
        totalDiv.innerText = `${totalMin}分`;
        info.el.appendChild(totalDiv);
    }
}





//			if(crudTarget !== null && dailyTotals){
//				const year = info.date.getFullYear();
//				const month = info.date.getMonth() + 1;
//				const day1 = info.date.getDate();
//				const dateStr = `${year}-${String(month).padStart(2,'0')}-${String(day1).padStart(2,'0')}`;
	
//				const totalMin = dailyTotals.get(dateStr);
//				if(totalMin != null){
//					const totalDiv = document.createElement('div')
//					totalDiv.style.fontSize = "0.8rem";
//					totalDiv.style.marginTop = "2px";
//					totalDiv.style.color = "#778899";
//					totalDiv.innerText = `${totalMin}分`;
//					info.el.appendChild(totalDiv);
//				}
//			}
		},

    eventDidMount: function(info){
      switch(info.event.title){
        case '睡眠':
          info.el.style.backgroundColor = '#A3D2CA';  // 背景
          info.el.style.borderColor = '#273C75';      // 枠線
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
				title: body.minutes + "分",
				start: saved.start,
				end: saved.end
			});
		} : undefined,

		//イベントクリックで削除(DELETE)(イベントクリックの誤動作防止) 編集可のみ有効
		eventClick: crudEnabled ?　async function(info){
			if(!confirm("この記録を削除しますか?")) return;
			
			const apiBase =
				crudTarget === 'plans'
				? '/api/learning-plans'
				: '/api/learning-records';	

			const res = await fetch(`/api/learning-records/${info.event.id}`,{
				method:'DELETE',
				headers:{[csrfHeader]: csrfToken}
			});
			if(res.ok){
				info.event.remove();
			}else{
				alert("削除に失敗しました。");
			}
		} : undefined,
		
	//ドラッグで時間変更（PUT）	 編集可のみ
	 	eventDrop: crudEnabled ? updateEvent : undefined,
		eventResize: crudEnabled ? updateEvent : undefined,
	});
	
	calendar.render();
	
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
	
	//日ごとの合計学習時間を取得
	async function fetchDailyTotals(){
		const res = await fetch('/api/learning-records/daily-totals');
		if(!res.ok)return new Map();
		const jsonData = await res.json();
//		const map = new Map();
//		Object.entries(jsonData).forEach(([day,totalMinutes]) => {map.set(day, totalMinutes)});
//		return map;
		return jsonData
	}
	
}