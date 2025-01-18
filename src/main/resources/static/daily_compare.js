const colors = [
	"#00cc99",
	"#006699",
	"#ff0000",
	"#ffd700",
	"#8b008b",
	"#00ff00",
	"#0000ff",
	"#d2691e",
	"#00ffff",
];

var serversArraySC = new URLSearchParams(document.location.search).get(
	"servers",
);
var date = new URLSearchParams(document.location.search).get("date");

var serversArray = [];
var serversIDsArray = serversArraySC.split(";");

async function getHostnames() {
	for (var i = 0; i < serversIDsArray.length - 1; i++) {
		var hostname = await fetch(
			"http://localhost:8080/hostname?id=" + serversIDsArray[i],
		);

		serversArray.push(await hostname.text());
	}
}

function drawGraphs(data) {
	document.getElementById("container").innerHTML = "";
	document.getElementById("legend").innerHTML = "";

	var chart = anychart.line();

	for (var i = 0; i < data.length; i++) {
		var series = chart.line(data[i]);

		series.normal().stroke(colors[i], 1);
		series.hovered().stroke(colors[i], 2);
		series.selected().stroke(colors[i], 4);
	}

	chart.container("container");
	chart.title("Онлайн за день");

	var legend = anychart.standalones.legend();
	var legendItems = [];

	for (var i = 0; i < data.length; i++) {
		var item = {
			text: serversArray[i],
			iconType: "square",
			iconFill: { color: colors[i] },
		};

		legendItems.push(item);
	}

	legend.items(legendItems);
	legend.container("legend");
	legend.draw();

	chart.draw();
}

async function loadData() {
	var response = await fetch(
		"http://localhost:8080/dailycomparedata?servers=" +
			serversArraySC +
			"&date=" +
			date,
	);

	if (response.ok) {
		var responseBody = await response.json();

		let data = [];

		for (var i = 0; i < responseBody.length; i++) {
			let serverData = responseBody[i];
			let serverDataHandled = [];

			for (var j = 0; j < serverData.length; j++) {
				let jsonEntry = serverData[j];

				let x = jsonEntry.hour + ":" + jsonEntry.minute;
				let obj = {
					x: x,
					value: jsonEntry.online,
				};

				serverDataHandled.push(obj);
			}

			data.push(serverDataHandled);
		}

		drawGraphs(data);
	} else {
		alert("Словил маслину с кодом " + response.status);
	}
}

getHostnames();
loadData();
