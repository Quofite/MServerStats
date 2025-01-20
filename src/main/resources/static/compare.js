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
let serversArraySC = "";
const serversArray = [];

function deleteServer(serverName) {
	const serverIndex = serversArray.indexOf(serverName);
	serversArray.splice(serverIndex, 1);

	const serversIDsArray = serversArraySC.split(";");
	serversArraySC = "";
	for (let i = 0; i < serversIDsArray.length - 1; i++) {
		if (i !== serverIndex) {
			serversArraySC += serversIDsArray[i] + ";";
		} else {
			document.getElementById(serversIDsArray[i]).remove();
		}
	}
}

function serverChosen() {
	if (serversArray.length > 9) {
		alert("Достигнут максимум серверов для сравнения");
		return;
	}

	const server = serverMenu.options[serverMenu.selectedIndex];
	const serverName = server.text;
	const serverId = server.value;

	serversArraySC += serverId + ";";
	serversArray.push(serverName);

	const deconsteButton =
		`<button style="width=5%; margin-left: 10px;" onclick="deconsteServer('` +
		serverName + `')"> - </button><br>`;

	document.getElementById("serversList").innerHTML += "<div id=" + serverId +
		">" + serverName +
		deconsteButton + "</div>";
}

function drawGraphs(data) {
	document.getElementById("container").innerHTML = "";
	document.getElementById("legend").innerHTML = "";

	const chart = anychart.line();

	for (let i = 0; i < data.length; i++) {
		const series = chart.line(data[i]);

		series.normal().stroke(colors[i], 1);
		series.hovered().stroke(colors[i], 2);
		series.selected().stroke(colors[i], 4);

		series.listen("pointClick", (e) => {
			window.open(
				"http://localhost:8080/dailycompare?servers=" +
					serversArraySC + "&date=" + e.point.get("x"),
			);
		});
	}

	chart.container("container");
	chart.title("Средний онлайн ежедневно");

	const legend = anychart.standalones.legend();
	const legendItems = [];

	for (let i = 0; i < data.length; i++) {
		const item = {
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
	const response = await fetch(
		"http://localhost:8080/comparedata?servers=" + serversArraySC,
	);

	if (response.ok) {
		const responseBody = await response.json();

		const data = [];
		for (let i = 0; i < responseBody.length; i++) {
			const serverData = responseBody[i];
			const serverDataHandled = [];

			for (let j = 0; j < serverData.length; j++) {
				const jsonEntry = serverData[j];

				const x = jsonEntry.day + "." + jsonEntry.month + "." + jsonEntry.year;
				const obj = {
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
