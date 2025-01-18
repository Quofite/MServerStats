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
const serverMenu = document.server.servers;
var serversArraySC = "";
var serversArray = [];

function serverChosen() {
	if (serversArray.length > 9) {
		alert("Достигнут максимум серверов для сравнения");
		return;
	}

	const server = serverMenu.options[serverMenu.selectedIndex];
	var serverName = server.text;
	var serverId = server.value;

	serversArraySC += serverId + ";";
	serversArray.push(serverName);
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

		series.listen("pointClick", (e) => {
			window.open(
				"http://localhost:8080/dailycompare?servers=" +
					serversArraySC +
					"&date=" +
					e.point.get("x"),
			);
		});
	}

	chart.container("container");
	chart.title("Средний онлайн ежедневно");

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
		"http://localhost:8080/comparedata?servers=" + serversArraySC,
	);

	if (response.ok) {
		var responseBody = await response.json();

		let data = [];

		for (var i = 0; i < responseBody.length; i++) {
			let serverData = responseBody[i];
			let serverDataHandled = [];

			for (var j = 0; j < serverData.length; j++) {
				let jsonEntry = serverData[j];

				let x = jsonEntry.day + "." + jsonEntry.month + "." + jsonEntry.year;
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
