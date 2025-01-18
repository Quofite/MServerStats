var onlineRateColorScheme = ["#8b0000", "#ffd700", "#008000", "#4169e1"];

var maxOnline = 0;
var serverId = new URLSearchParams(document.location.search).get("id");
var date = new URLSearchParams(document.location.search).get("date");

function drawGraph(data) {
	var chart = anychart.column();
	var series = chart.column(data);

	series.fill(function () {
		if (this.value >= 0.8 * maxOnline) {
			return onlineRateColorScheme[3];
		} else if (this.value <= 0.2 * maxOnline) {
			return onlineRateColorScheme[0];
		} else if (this.value > 0.2 * maxOnline && this.value <= 0.5 * maxOnline) {
			return onlineRateColorScheme[1];
		}

		return onlineRateColorScheme[2];
	});

	var labels = chart.xAxis().labels();
	labels.enabled(false);

	chart.container("container");
	chart.title("Онлайн поминутно");
	chart.draw();
}

async function getData() {
	let data = []; // objects array to be returned

	var response = await fetch(
		"http://localhost:8080/graphdaily?id=" + serverId + "&date=" + date,
	);

	if (response.ok) {
		var responseBody = await response.json();
		maxOnline = responseBody[0];

		var json = responseBody[1];

		for (var i = 0; i < json.length; i++) {
			let jsonEntry = json[i];
			let x = jsonEntry.hour + ":" + jsonEntry.minute + "; " + jsonEntry.map;

			let obj = {
				x: x,
				value: jsonEntry.online,
			};
			data.push(obj);
		}

		drawGraph(data);
	} else {
		alert(response.status);
	}
}

getData();
