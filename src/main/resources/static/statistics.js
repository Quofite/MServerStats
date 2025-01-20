function drawGraph(data) {
	document.getElementById("container").innerHTML = "";
	const chart = anychart.line(data);
	chart.container("container");
	chart.title("Средний онлайн поминутно");
	chart.draw();
}

function drawPieGraph(data) {
	document.getElementById("pie").innerHTML = "";
	const chart = anychart.pie(data);
	chart.container("pie");
	chart.title("Топ карт");
	chart.draw();
}

function isNumberKey(e) {
	const charCode = e.which ? e.which : e.keyCode;

	if (charCode > 31 && (charCode < 48 || charCode > 57)) return false;

	return true;
}

async function getData() {
	const data = [];

	const resposne = await fetch("http://localhost:8080/timestatsdata");
	if (resposne.ok) {
		const json = await resposne.json();

		for (let i = 0; i < json.length; i++) {
			const jsonEntry = json[i];

			const obj = {
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
	const data = [];

	const resposne = await fetch(
		"http://localhost:8080/mapstatsdata?count=" + mapsAmount,
	);
	if (resposne.ok) {
		const json = await resposne.json();

		for (let i = 0; i < json.length; i++) {
			const jsonEntry = json[i];

			const obj = {
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
