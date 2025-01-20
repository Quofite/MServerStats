let maxAverageOnline = 0.0;
const onlineRateColorScheme = ["#8b0000", "#ffd700", "#008000", "#4169e1"];
const serverId = new URLSearchParams(document.location.search).get("id");

function drawGraph(data) {
	const chart = anychart.column();
	const series = chart.column(data);

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
	const chart = anychart.line(data);
	chart.container("avgOnline");
	chart.title("Средний онлайн поминутно");
	chart.draw();
}

function drawMapsOnlineGraph(data) {
	const chart = anychart.pie(data);
	chart.container("mapsOnline");
	chart.title("Любимые карты игроков (от 288 записей)");
	chart.draw();
}

async function getData() {
	const data = []; // objects array to be returned

	const response = await fetch("http://localhost:8080/graph?id=" + serverId);
	if (response.ok) {
		const json = await response.json();
		maxAverageOnline = json[0].compareOnline;

		for (let i = 0; i < json.length; i++) {
			const jsonEntry = json[i];
			const date = jsonEntry.day + "." + jsonEntry.month + "." + jsonEntry.year;

			const obj = {
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
	const data = [];

	const response = await fetch(
		"http://localhost:8080/timestatsdata?id=" + serverId,
	);
	if (response.ok) {
		const json = await response.json();
		maxAverageOnline = json[0].compareOnline;

		for (let i = 0; i < json.length; i++) {
			const jsonEntry = json[i];

			const obj = {
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
	const data = [];

	const response = await fetch(
		"http://localhost:8080/mapstatsdata?id=" + serverId + "&maps=" + mapsAmount,
	);
	if (response.ok) {
		const json = await response.json();
		maxAverageOnline = json[0].compareOnline;

		for (let i = 0; i < json.length; i++) {
			const jsonEntry = json[i];

			if (jsonEntry.map !== "default") {
				const obj = {
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
