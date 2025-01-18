var maxAverageOnline = 0.0;
var onlineRateColorScheme = ["#8b0000", "#ffd700", "#008000", "#4169e1"];
var serverId = new URLSearchParams(document.location.search).get("id");

function drawGraph(data) {
	var chart = anychart.column();
	var series = chart.column(data);

	series.fill(function () {
		if (this.value >= 0.8 * maxAverageOnline) {
			return onlineRateColorScheme[3];
		} else if (this.value <= 0.2 * maxAverageOnline) {
			return onlineRateColorScheme[0];
		} else if (
			this.value > 0.2 * maxAverageOnline &&
			this.value <= 0.5 * maxAverageOnline
		) {
			return onlineRateColorScheme[1];
		}

		return onlineRateColorScheme[2];
	});

	series.listen("pointClick", (e) => {
		window.open(
			"http://localhost:8080/details?id=" +
				serverId +
				"&date=" +
				e.point.get("x"),
		);
	});

	chart.container("container");
	chart.title("Средний онлайн в день");
	chart.draw();
}

function drawAvgOnlineGraph(data) {
	var chart = anychart.line();
	var series = chart.line(data);
	chart.container("avgOnline");
	chart.title("Средний онлайн поминутно");
	chart.draw();
}

function drawMapsOnlineGraph(data) {
	var chart = anychart.pie(data);
	chart.container("mapsOnline");
	chart.title("Любимые карты игроков (от 288 записей)");
	chart.draw();
}

async function getData() {
	let data = []; // objects array to be returned

	let response = await fetch("http://localhost:8080/graph?id=" + serverId);
	if (response.ok) {
		let json = await response.json();
		maxAverageOnline = json[0].compareOnline;

		for (var i = 0; i < json.length; i++) {
			let jsonEntry = json[i];
			let date = jsonEntry.day + "." + jsonEntry.month + "." + jsonEntry.year;

			let obj = {
				x: date,
				value: jsonEntry.online,
			};
			data.push(obj);
		}

		drawGraph(data);
	} else {
		alert(response.status);
	}
}

async function getAvgOnlineData() {
	let data = [];

	let response = await fetch(
		"http://localhost:8080/timestatsdata?id=" + serverId,
	);
	if (response.ok) {
		let json = await response.json();
		maxAverageOnline = json[0].compareOnline;

		for (var i = 0; i < json.length; i++) {
			let jsonEntry = json[i];

			let obj = {
				x: jsonEntry.time,
				value: jsonEntry.online,
			};
			data.push(obj);
		}

		drawAvgOnlineGraph(data);
	} else {
		alert(response.status);
	}
}

async function getMapsOnlineData(mapsAmount) {
	let data = [];

	let response = await fetch(
		"http://localhost:8080/mapstatsdata?id=" + serverId + "&maps=" + mapsAmount,
	);
	if (response.ok) {
		let json = await response.json();
		maxAverageOnline = json[0].compareOnline;

		for (var i = 0; i < json.length; i++) {
			let jsonEntry = json[i];

			if (jsonEntry.map !== "default") {
				let obj = {
					x: jsonEntry.map,
					value: jsonEntry.online,
				};
				data.push(obj);
			}
		}

		drawMapsOnlineGraph(data);
	} else {
		alert(response.status);
	}
}

document.getElementById("addInfo").innerHTML += " / id: " + serverId;
getData();
getAvgOnlineData();
getMapsOnlineData(10);
