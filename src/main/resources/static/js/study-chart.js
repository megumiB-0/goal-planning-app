document.addEventListener('DOMContentLoaded', () => {
	
					const labels = studyData.map(d => d.x);
					const dataValues = studyData.map(d => d.y);
	
					const ctx = document.getElementById('myChart').getContext('2d');
	
					new Chart(ctx,{
						type: 'line', //'barにすると棒グラフ'
						data: {
							labels: labels,
							datasets:
							[{
								label:'学習時間(実績)',
								data: dataValues,
								borderWidth: 2,
								borderColor: 'blue',	
								spanGaps: true,
							},{
								label:'目標ライン',
								data: [
									{x:startDate,y:0},
									{x:goalDate,y:estimatedHours}
								],
								borderWidth: 2,
								borderColor: 'black',	
								spanGaps: true,
								tension: 0	
							}]
						},
					options: {
					responsive:true,
					scales:{
					x:{
						type:'time',
						time:{unit:'day'},
						title: { display: true, text: '日付' }
						},
					y:{
					beginAtZero: true,
					title:{ display: true, text: '学習時間(時間)'}
				}
			},
			plugins:{
				legend: {display: true}
			}
		}
	});	
});
