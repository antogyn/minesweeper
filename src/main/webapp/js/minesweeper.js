var ws = null;

var width = 32;
var height = 24;
var pixelSize = 22;
var size = "GIANT";
var field;

function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
	$("#sendText").prop("disabled", !connected);
}

function connect() {
	
	if (ws !== null && ws.readyState !== 3) {
		return;
	}

	var nicknameVal = $("#nickname").val();
	var errorPanel = $("#error-panel");
	
	if (nicknameVal == "") {
		errorPanel.text("Please inform your nickname.");
		errorPanel.css("display", "block");
		return false;
	} else {
		errorPanel.text("");
		errorPanel.css("display", "none");
	}
	$("#instructions").hide();
	var target = "ws://localhost:8080/minesweeper/websocket/" + nicknameVal;
	if ("WebSocket" in window) {
		ws = new WebSocket(target);
	} else if ("MozWebSocket" in window) {
		ws = new MozWebSocket(target);
	} else {
		alert("WebSocket is not supported by this browser.");
		return;
	}
	ws.onopen = function() {
		setConnected(true);
		startGame();
		addToChat("Connection successful !");
	};
	ws.onmessage = function(messageJson) {
		message = JSON.parse(messageJson.data);
		switch (message.type) {
		// message in the chat
		case "chat":
			addToChat(message.data);
			break;
		// new user list
		case "userlist":
			updateUserlist(message.data);
			break;
		case "minefield":
			drawMinefield(message.data);
			break;
		}
	};
	ws.onclose = function(event) {
		console.log(event);
		setConnected(false);
		addToChat("Connection closed.");
	};
	ws.onerror = function(event) {
		console.log(event);
		setConnected(false);
		addToChat('Error, Code: ' + event.code
				+ (event.reason == "" ? "" : ", Reason: " + event.reason));
	}
	
}

function disconnect() {
	console.log("Disconnection (user)")
	if (ws !== null) {
		ws.close();
		ws = null;
	}
	$("#userlist").empty();
//	canvas.clear();
	setConnected(false);
}

/**
 * Send the text message to the server
 */
function sendText() {
	if (ws !== null) {
		var message = $("#message");
		var jsonText =
			{
				type:"chat",
				data:message.val()
			};
		ws.send(JSON.stringify(jsonText));
		
		message.val("");
		message.attr("placeholder", "");
		message.focus();
	} else {
		alert("Connection not established, please connect.");
	}
}

/**
 * Updates ws target
 * 
 * @param target
 */
function updateTarget(target) {
	if ($(location).attr("protocol") == "http:") {
		$("#target").val("ws://" + $(location).attr("host") + target);
	} else {
		$("#target").val("wss://" + $(location).attr("host") + target);
	}
}

/**
 * Adds a message to the chat
 * 
 * @param message
 */
function addToChat(message) {
	var $chat = $("#chat");
	$chat.append($("<p></p>").text(message));
	while ($chat.children().length > 25) {
		$chat.children().first().remove();
	}
	$chat.scrollTop($chat.prop('scrollHeight'));
}

/**
 * Updates the client-side userlist
 * 
 * @param userlist
 */
function updateUserlist(userlist) {
	$("#userlist").empty();
	for (var i = 0; i < userlist.length; i++) {
		addUser(userlist[i]);
	}
}

function addUser(nickname) {
	$("#userlist").append($("<p></p>").text(nickname));
}

function startGame() {
	if (ws != null) {
		var data = {
				"click":"start",
				"size":size
		};
		var jsonText =
			{
				type:"minefield",
				data:data
			};
		ws.send(JSON.stringify(jsonText));	
	} else {
		alert("WebSocket connection not established, please connect.");
	}
}

function stopGame() {
	if (ws != null) {
		var data = {
				"click":"stop"
		};
		var jsonText =
			{
				type:"minefield",
				data:data
			};
		ws.send(JSON.stringify(jsonText));
	} else {
		alert("WebSocket connection not established, please connect.");
	}
}

function restartGame() {
	if (ws != null) {
		var data = {
				"click":"restart",
				"size":size
		};
		var jsonText =
			{
				type:"minefield",
				data:data
			};
		ws.send(JSON.stringify(jsonText));
	} else {
		alert("WebSocket connection not established, please connect.");
	}
}

/**
 * redraws the minefield based on the json
 * @param boxes
 */
function drawMinefield(data) {
	//var deb = new Date().getTime();
	drawBoxes(data.boxes);
	$("#flags").text(data.flags);
	$("#lives").text(data.lives + (data.lives == 0 ? " :(" : "") + (data.win ? " :D" : ""));
	//console.log("time to draw : " + (new Date().getTime() - deb));
}

function drawBoxes(boxes) {
		
	var boxesGroup = field.selectAll("g").data(boxes, function(box) { return box.x + ' ' + box.y; });
	
	var boxesGroupEnter = boxesGroup.enter().append("g")
		.attr("transform", function(box) {
			return "translate("+ box.x*pixelSize +"," + box.y*pixelSize + ")";
		})
		.attr("id", function(box) {return 'g ' + box.x + ' ' + box.y;})
		.attr("play_x", function(box) {return box.x;})
		.attr("play_y", function(box) {return box.y;})
		.on("click", function() {
			var box = d3.select(this);
			var data = {
				"click":"left",
				"x":box.attr('play_x'),
				"y":box.attr('play_y')
			};
			var jsonText =
				{
					type:"minefield",
					data:data
				};
			console.log(JSON.stringify(jsonText));
			ws.send(JSON.stringify(jsonText));
		})
		.on("contextmenu", function() {
			d3.event.preventDefault();
			var box = d3.select(this);
			var data = {
				"click":"right",
				"x":box.attr('play_x'),
				"y":box.attr('play_y')
			};
			var jsonText =
				{
					type:"minefield",
					data:data
				};
			ws.send(JSON.stringify(jsonText));
		});
	
	boxesGroupEnter.append("rect")
		.attr('id',  function(box) {return 'rect ' + box.x + ' ' + box.y;})
		.attr('x', function(box) {return 0})
		.attr('y', function(box) {return 0})
		.attr('width',  pixelSize)
		.attr('height', pixelSize)
		.attr('stroke-width', 1)
		.attr('stroke', 'black');

	boxesGroup.select("rect").datum(function(d) {return d;})
		.attr('fill', function(box) {
			var newColor = "black"; // black = bug
			if (box.display === "flag") {
				newColor = "orange";
			} else if (box.display === "bomb") {
				newColor = "red";
			} else if (box.display === "unexposed") {
				newColor = "CornflowerBlue";		
			} else if (box.display === "exposed") {
				newColor = "Beige";
			}
			return newColor;
		});
	
	boxesGroupEnter.append("text")
		.attr('id',  function(box) {return box.x + ' ' + box.y;})
		.attr('x', function(box) {return 5;})
		.attr('y', function(box) {return pixelSize - 4})
		.attr('font-family', "monospace")
		.attr('font-size', pixelSize)
		.attr('fill', "black")
		.style('cursor', 'default');
	
	boxesGroup.select("text").datum(function(d) {return d;})
		.text(function(box) {return box.adjacentBombs == 0 ? "" : box.adjacentBombs;})
		.attr('opacity', function(box) {return box.display === "exposed" ? 1 : 0});
	
}

$(function() {
	
field = d3.select('#playfield').append('svg')
	.attr('width', width*pixelSize)
	.attr('height', height*pixelSize)
	.style('border', '1px solid black');
	
});

/**
 * Remove noscript
 */
document.addEventListener("DOMContentLoaded", function() {
	$(".noscript").remove();
}, false);



