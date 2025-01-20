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

const serversArraySC = new URLSearchParams(document.location.search).get(
	"servers",
);
const date = new URLSearchParams(document.location.search).get("date");

const serversArray = [];
const serversIDsArray = serversArraySC.split(";");

async function getHostnames() {
	for (let i = 0; i < serversIDsArray.length - 1; i++) {
		const hostname = await fetch(
			"http://localhost:8080/hostname?id=" + serversIDsArray[i],
		);

		serversArray.push(await hostname.text());
	}
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
				"http://localhost:8080/instant?servers=" +
					serversArraySC + "&date=" + date + "&time=" + e.point.get("x"),
			);
		});
	}

	chart.container("container");
	chart.title("Онлайн за день");

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
		"http://localhost:8080/dailycomparedata?servers=" +
			serversArraySC +
			"&date=" +
			date,
	);

	if (response.ok) {
		const responseBody = await response.json();

		const data = [];

		for (let i = 0; i < responseBody.length; i++) {
			const serverData = responseBody[i];
			const serverDataHandled = [];

			for (let j = 0; j < serverData.length; j++) {
				const jsonEntry = serverData[j];

				const x = jsonEntry.hour + ":" + jsonEntry.minute;
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

getHostnames();
loadData();
