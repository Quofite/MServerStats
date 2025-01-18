document.getElementById("sendButton").addEventListener("click", () => {
	var serverId = document.getElementById("serverId").value;
	window.location.href = "http://localhost:8080/data?id=" + serverId;
});

function detailedData(serverId) {
	window.location.href = "http://localhost:8080/data?id=" + serverId;
}

function toMainPage() {
	window.location.href = "http://localhost:8080/";
}

function comp() {
	window.location.href = "http://localhost:8080/compare";
}

function mapsStats() {
	window.location.href = "http://localhost:8080/statistics";
}

function updateCache() {
	var result = confirm(
		"Этот процесс может занять несколько минут. Хотите продолжить?",
	);

	if (result) {
		document.body.innerHTML = "Обновление данных...";
		window.location.href = "http://localhost:8080/update";
	}
}
