
function addRow(button){
	const tr =button.closest('[data-role="routine-row"]');
	if(!tr) return;
	const tbody= tr.parentElement;
	if(!tbody){
		console.error('tbody not found');
		return;
	}
	
	let currentIndex =tbody.querySelectorAll('[data-role="routine-row"]').length;
		
	const newRow = tr.cloneNode(true);
	
	//name/idを全て置換
	newRow.querySelectorAll('input').forEach(input =>{
		if(input.name){
			input.name =input.name.replace(/\[\d+\]/,`[${currentIndex}]`);
		}
		if(input.id){
			input.id = input.id.replace(/_\d+_/,`_${currentIndex}_`);
		}

		if(input.type ==='time') input.value='';
		if(input.type ==='checkbox') input.checked = false;
		
		//値リセット
//		if(input.type==='time' || input.type==='hidden'){
//			if(!input.name.endsWith('.title') && !input.name.endsWith('.sleepType')){
//				input.value='';
//			}
//		}
	});

	tbody.insertBefore(newRow, tr.nextSibling);
	currentIndex++;
}	
