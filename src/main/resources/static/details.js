const errorText =
	"Произошла ошибка при выполнении запроса. Возможно, не все данные еще обновились или их попросту нет об этом сервере.";
const onlineRateColorScheme = ["#8b0000", "#ffd700", "#008000", "#4169e1"];

let maxOnline = 0;
const serverId = new URLSearchParams(document.location.search).get("id");
const date = new URLSearchParams(document.location.search).get("date");

function drawGraph(data) {
	const chart = anychart.column();
	const series = chart.column(data);

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

	const labels = chart.xAxis().labels();
	labels.enabled(false);

	chart.container("container");
	chart.title("Онлайн поминутно");
	chart.draw();
}

async function getData() {
	const data = []; // objects array to be returned

	const response = await fetch(
		"http://localhost:8080/graphdaily?id=" + serverId + "&date=" + date,
	);

	if (response.ok) {
		const responseBody = await response.json();
		maxOnline = responseBody[0];

		const json = responseBody[1];

		for (let i = 0; i < json.length; i++) {
			const jsonEntry = json[i];

			const x = jsonEntry.hour + ":" + jsonEntry.minute + "; " + jsonEntry.map;
			const obj = {
				x: x,
				value: jsonEntry.online,
			};

			data.push(obj);
		}

		drawGraph(data);
	} else {
		alert(errorText);
	}
}

getData();
