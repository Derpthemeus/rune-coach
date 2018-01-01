let tags, selectedChampion, bestTagsByRune = {};

window.addEventListener("load", function () {
	loadJson("/getStaticData", (staticData) => {
		// TODO load announcement info

		// Load tag list
		tags = staticData.tags;

		// Populate champion list
		let championList = document.getElementById("champion-list");
		staticData.champions.forEach((champion) => {
			let div = document.createElement("div");
			div.className = "champion";
			let img = document.createElement("img");
			img.src = `https://ddragon.leagueoflegends.com/cdn/${staticData.ddragonVersion}/img/champion/${champion.image.full}`;
			div.appendChild(img);

			div.addEventListener("click", () => {
				setChampion(+champion.key);
				selectedChampion = champion;
			});

			championList.appendChild(div);
		});

		// Load best tags for each perk
		for (let tag of staticData.bestTags) {
			bestTagsByRune[tag.perkId] = tag;
		}

	}, (error, code) => {
		alert(`Error occurred loading static data: ${error} (${code})`);
	});
});

// `success` is called with parsed response if a 200 code is received; `error` is called with the raw body as 1st arg and status code as 2nd arg if a non-200 code is received
function loadJson(url, success, error) {
	let xhr = new XMLHttpRequest();

	xhr.addEventListener("load", function () {
		if (this.status === 200) {
			success(JSON.parse(this.responseText));
		} else if (error) {
			error(this.responseText, this.status);
		}
	});
	xhr.open("GET", url);
	xhr.send();
}

/** Rune tooltip objects cached by champion ID */
let performanceInfoByChampion = {};

let currentPerformanceInfo = null;

function setChampion(championId) {
	// Scroll to the rune builder
	document.getElementById("app-header").scrollIntoView();

	// Use cached tooltips if possible
	if (performanceInfoByChampion[championId]) {
		currentPerformanceInfo = performanceInfoByChampion[championId];
	} else {
		// Enable loading indicator
		let overlay = document.createElement("div");
		overlay.id = "loading-overlay";
		overlay.appendChild(document.createTextNode("Loading (This may take a moment)"));
		document.body.appendChild(overlay);

		loadJson(`/getChampionInfo?championId=${championId}`, (scoreArray) => {
			// Map tooltips by rune ID
			// TODO add support for SUBSTYLE
			currentPerformanceInfo = {"RELATIVE": {}, "SLOT": {}};

			// Setup perk scores
			scoreArray.forEach((performanceInfo) => {
				currentPerformanceInfo[performanceInfo.type][performanceInfo.perkId] = performanceInfo;
			});
			performanceInfoByChampion[championId] = currentPerformanceInfo;

			// Display style information
			for (let styleInfo of document.getElementsByClassName("styleInfo")) {
				styleInfo.innerHTML = "<br>" + getStyleInfo(+styleInfo.dataset.styleId, styleInfo.dataset.styleName);
			}

			document.getElementById("loading-overlay").remove();
		}, (error, code) => {
			alert(`An error occurred while loading rune data for champion: ${error} (${code})`);
			document.getElementById("loading-overlay").remove();
		});
	}
}

function getRuneTooltip(rune) {
	if (!currentPerformanceInfo) {
		return "<b>Select a champion to view information about this rune on them</b>";
	}

	let tooltip = "";

	let runeName = `<span class="tooltip-runeName">${rune.name}</span>`;
	let championName = `<span class="tooltip-championName">${selectedChampion.name}</span>`;

	// Yes, the absolute score is called relative. ty spaghetti code.
	let absoluteScore = currentPerformanceInfo["RELATIVE"][rune.runeId];
	let relativeScore = currentPerformanceInfo["SLOT"][rune.runeId];


	if (relativeScore) {
		let relativeRating = `<span class="tooltip-performance">${relativeScore.score < -0.015 ? "weak" : (relativeScore.score > 0.015) ? "strong" : "average"}</span>`;
		tooltip += `<b>Relative to other runes in this slot, ${runeName} is ${relativeRating} on ${championName}.<br><br>`;
	}

	if (absoluteScore) {
		let absoluteRating = `<span class="tooltip-performance">${absoluteScore.score < -0.015 ? "weak" : (absoluteScore.score > 0.015) ? "strong" : "average"}</span>`;
		tooltip += `Overall, ${runeName} is ${absoluteRating} on ${championName}.`;
	}

	if (!absoluteScore && !relativeScore) {
		tooltip += "No data about this rune on this champion is available";
	}

	if (bestTagsByRune[rune.runeId]) {
		let bestTagId = bestTagsByRune[rune.runeId].tagId;
		tooltip += `<br><br>${runeName} is usually strongest on ${getTagDescription(bestTagId)}.`;
	}
	return `<b>${tooltip}</b>`;
}

const possibleDescriptors = ["noun", "adjective", "verb"];

// TODO generate these when runes are loaded so they aren't constantly changing?
/**
 * Converts tag IDs into a user-friendly description of champions who have these tags
 * @param tag1Id The 1st tag ID (the most important of the 2)
 * @param tag2Id (optional) The 2nd tag ID
 */
function getTagDescription(tag1Id, tag2Id) {
	let tag1 = tags[tag1Id];
	let tag2 = tags[tag2Id];

	let tag1Options = Object.keys(tag1).filter((key) => possibleDescriptors.includes(key) && tag1[key]);
	let descriptorType1 = tag1Options[Math.floor(Math.random() * tag1Options.length)];

	let reasons = [{
		descriptorType: descriptorType1,
		descriptor: tag1[descriptorType1]
	}];

	if (tag2) {
		let tag2Options = Object.keys(tag2).filter((key) => possibleDescriptors.includes(key) && tag2[key] && key !== descriptorType1);
		let descriptorType2 = tag1Options[Math.floor(Math.random() * tag2Options.length)];
		reasons.push({
			descriptorType: descriptorType2,
			descriptor: tag2[descriptorType2]
		});
	}

	let description = "";

	for (let reason of reasons) {
		if (reason.descriptorType === "adjective") {
			description += reason.descriptor + " ";
			break;
		}
	}

	let noun = "champions";
	for (let reason of reasons) {
		if (reason.descriptorType === "noun") {
			noun = reason.descriptor;
			break;
		}
	}
	description += noun;

	for (let reason of reasons) {
		if (reason.descriptorType === "verb") {
			description += " " + reason.descriptor;
			break;
		}
	}

	return description;
}

function getStyleInfo(styleId, styleName) {
	if (!currentPerformanceInfo) {
		return "<b>Select a champion to view information about this style on them</b>";
	}

	styleName = `<span class="tooltip-runeName">${styleName}</span>`;
	let championName = `<span class="tooltip-championName">${selectedChampion.name}</span>`;

	let tooltip = "";

	let stylePerformance = currentPerformanceInfo["RELATIVE"][styleId];
	if (stylePerformance) {
		let rating = `<span class="tooltip-performance">${stylePerformance.score < -0.015 ? "weak" : (stylePerformance.score > 0.01) ? "strong" : "average"}</span>`;
		tooltip += `${styleName} is ${rating} on ${championName}.`;
	} else {
		tooltip += "<b>No data about this style on this champion is available</b>";
	}

	// Styles are too versatile to be categorized into a single tag
	/*
	let bestTagId = bestTagsByRune[styleId].tagId;
	tooltip += `<br><br>${styleName} is usually strongest on ${getTagDescription(bestTagId)}.`;
	*/

	return `<b>${tooltip}</b>`;
}
