function drawGraph(data) {
	document.getElementById("container").innerHTML = "";
	var chart = anychart.line();
	var series = chart.line(data);
	chart.container("container");
	chart.title("Средний онлайн поминутно");
	chart.draw();
}

function drawPieGraph(data) {
	document.getElementById("pie").innerHTML = "";
	var chart = anychart.pie(data);
	chart.container("pie");
	chart.title("Топ карт");
	chart.draw();
}

function isNumberKey(e) {
	var charCode = e.which ? e.which : e.keyCode;

	if (charCode > 31 && (charCode < 48 || charCode > 57)) return false;

	return true;
}

async function getData() {
	let data = [];

	let resposne = await fetch("http://localhost:8080/timestatsdata");
	if (resposne.ok) {
		let json = await resposne.json();

		for (var i = 0; i < json.length; i++) {
			let jsonEntry = json[i];

			let obj = {
				x: jsonEntry.time,
				value: jsonEntry.online,
			};
			data.push(obj);
		}

		drawGraph(data);
	} else {
		alert(resposne.status);
	}
}

async function getPieData(mapsAmount) {
	let data = [];

	let resposne = await fetch(
		"http://localhost:8080/mapstatsdata?count=" + mapsAmount,
	);
	if (resposne.ok) {
		let json = await resposne.json();

		for (var i = 0; i < json.length; i++) {
			let jsonEntry = json[i];

			let obj = {
				x: jsonEntry.map,
				value: jsonEntry.online,
			};
			data.push(obj);
		}

		drawPieGraph(data);
	} else {
		alert(resposne.status);
	}
}

getData();
getPieData(10);
