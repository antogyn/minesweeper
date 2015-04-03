var ws = null;

var width = 16;
var height = 12;
var pixelSize = 22;
var size = "MEDIUM";
var canvas;
var canvasBoxes = Array2D(width, height);
var canvasText = Array2D(width, height);
var timestamps = Array2D(width, height);

function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
	$("#sendText").prop("disabled", !connected);
}

function connect() {

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
	var target = "ws://localhost:8080/websocket/" + nicknameVal;
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
		addToChat("WebSocket connection opened.");
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
		setConnected(false);
		addToChat('WebSocket connection closed, Code: ' + event.code
				+ (event.reason == "" ? "" : ", Reason: " + event.reason));
	};
}

function disconnect() {
	if (ws != null) {
		ws.close();
		ws = null;
	}
	$("#userlist").empty();
	setConnected(false);
}

/**
 * Send the text message to the server
 */
function sendText() {
	if (ws != null) {
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
		alert("WebSocket connection not established, please connect.");
	}
}

/**
 * Updates ws target
 * 
 * @param target
 */
function updateTarget(target) {
	if ($(location).attr("protocol") === "http:") {
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
	$("#chat").append($("<p></p>").text(message));
	while ($("#chat").children().length > 25) {
		$("#chat").children().first().remove();
	}
	$("#chat").scrollTop = $("#chat").scrollHeight;
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

/**
 * Initialize the canvas
 */
function initCanvas() {
	$("#canvas").prop("width", width*pixelSize + 2);
	$("#canvas").prop("height", height*pixelSize + 2);
	
	$("#canvas").bind('contextmenu', function (e) {
	    return false;
	});
	
	$("#canvas").bind('mousedown', function (e) {
		
		var coords = getMouseCoordinates(this, e);
		canvasX = Math.floor(coords.x / pixelSize);
		canvasY = Math.floor(coords.y / pixelSize);
		var click;	
		
		switch (e.which) {
	        case 1:
	        	// left
	        	click = "left";
	            break;
	        case 2:
	            // middle
	            click = "left";
	            break;
	        case 3:
	            // right
	            click = "right";
	            break;
	        default:
	            // ??
	        	click = "left";
		}
		
		var data = {
				"click":click,
				"x":Math.min(canvasX,width-1),
				"y":Math.min(canvasY,height-1)
		};
		var jsonText =
			{
				type:"minefield",
				data:data
			};
		ws.send(JSON.stringify(jsonText));
		
	    return false;
	});
	
}

function startGame() {
	if (ws != null) {
		
		for (var i = 0; i < width; i++) {
			for (var j = 0; j < height; j++) {
				timestamps[i][j] = 0;
			}
		}
		
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


/**
 * redraws the minefield based on the json
 * @param boxes
 */
function drawMinefield(data) {

	var deb = new Date().getTime();
	drawBoxes(data.boxes);
	$("#flags").text(data.flags);
	$("#lives").text(data.lives + (data.lives == 0 ? " :(" : "") + (data.win ? " :D" : ""));
	
	//TODO : flags/lives left
	console.log("time to draw : " + (new Date().getTime() - deb));
	
}

function drawBoxes(boxes) {
	for (var i = 0; i < boxes.length; i++) {
		if (timestamps[boxes[i].x][boxes[i].y] <= boxes[i].timestamp) {
			drawABox(boxes[i]);
			timestamps[boxes[i].x][boxes[i].y] = boxes[i].timestamp;
		}
	}
	canvas.renderAll();	
}

function drawABox(box) {
	
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
	
	if (timestamps[box.x][box.y] == 0) {
		canvasBoxes[box.x][box.y] =
			new fabric.Rect({
			  left: box.x * pixelSize,
			  top: box.y * pixelSize,
			  fill: newColor,
			  strokeWidth: 1,
			  stroke: "black",
			  width: pixelSize,
			  height: pixelSize
			});
		canvas.add(canvasBoxes[box.x][box.y]);	
	} else {
		canvasBoxes[box.x][box.y].set(
			{
				fill: newColor
			});
	}

	// text with the number of adjacent mines
	if (box.adjacentBombs != 0) {
		if (timestamps[box.x][box.y] == 0) {
			canvasText[box.x][box.y] = new fabric.Text(box.adjacentBombs.toString(), {
				left: box.x*pixelSize + 6,
				top: box.y*pixelSize - 4,
				fontSize: pixelSize,
				opacity: box.display === "exposed" ? 1 : 0
			});
			canvas.add(canvasText[box.x][box.y]);
		} else {
			canvasText[box.x][box.y].set(
				{
					opacity: box.display === "exposed" ? 1 : 0
				});
		}
	}
	
}


/** coordinates relative to an element
 */
function getMouseCoordinates(element, event){
    var totalOffsetX = 0;
    var totalOffsetY = 0;
    var canvasX = 0;
    var canvasY = 0;
    var currentElement = element;

    do {
        totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
        totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
    }
    while(currentElement = currentElement.offsetParent);

    canvasX = event.pageX - totalOffsetX;
    canvasY = event.pageY - totalOffsetY;

    return {x:canvasX, y:canvasY};
}


$(function() {
	
	initCanvas();
	canvas = new fabric.StaticCanvas('canvas');
	canvas.renderOnAddRemove = false;
	
});

function Array2D(x, y)
{
    var array2D = new Array(x);

    for(var i = 0; i < array2D.length; i++)
    {
        array2D[i] = new Array(y);
    }

    return array2D;
}


/**
 * Remove noscript
 */
document.addEventListener("DOMContentLoaded", function() {
	$(".noscript").remove();
}, false);



