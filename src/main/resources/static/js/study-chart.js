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
								label:'学習実績',
								data: dataValues,
								borderWidth: 2,
								borderColor: '#F28C8C',	
								spanGaps: true,
							},{
								label:'目標ライン',
								data: [
									{x:startDate,y:0},
									{x:goalDate,y:estimatedHours}
								],
								borderWidth: 2,
								borderColor: '#2C363F',	
								spanGaps: true,
								tension: 0	
							}]
						},
					options: {
					responsive:true,
					locale: 'en-US',
					scales:{
					x:{
						type:'time',
						time:{
							unit:'day',
							displayFormats: {
          						day: 'M/dd'
        					}
        				},
						title: { display: true, text: '日付' }
						},
					y:{
					beginAtZero: true,
					title:{ display: true, text: '学習時間(時間)'}
				}
			},
			plugins:{
				legend: {display: true, position:'chartArea'}
			}
		}
	});	
});
