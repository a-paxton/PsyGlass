/* For more information on jQuery: http://learn.jquery.com/about-jquery/how-jquery-works/ */

// Constant variables to represent session state
var NUM_STATES = 4;
var DISCONNECTED = 0;
var STARTING = 1;
var RUNNING = 2;
var FINISHING = 3;

var POLL_DELAY = 250; // milliseconds

var currentState = DISCONNECTED; // Keeps track of server state
var devices = new Array(); // Local copy of devices
var timeoutId = -1; // Id to cancel server polling

// CALLS SCRIPT.JS
// Run code as soon as the document is ready to be manipulated
$( document ).ready(function() {
	onReady();
});



// CALLED FROM SCRIPT.JS
// Change the state of the server to the next state
// Returns the new state to caller
function changeState() {
	currentState++;
	if (currentState == NUM_STATES) {
		currentState = 0;
	}
	
	if (currentState == DISCONNECTED) {
		clearTimeout(timeoutId);
	}
	sc_changeServerState();
	
	return currentState;
}

// CALLED FROM SCRIPT.JS
// Changes the color and text display of the device and returns true
// Does nothing and returns false if nothing has changed
// int deviceId - id of the device
// string color - hex color value without hash symbol to update
// string text - text to update
function updateDevice( deviceId, color, text ) {
	//console.log("color: " + color + ", text: " + text);
	if (color != devices[deviceId].color) {
		if (text != devices[deviceId].text) {
			sc_changeDeviceDisplay({"index":deviceId,"color":color,"text":text})
		} else {
			sc_changeDeviceDisplay({"index":deviceId,"color":color});
		}
		return true;
	} else if (text != devices[deviceId].text) {
		sc_changeDeviceDisplay({"index":deviceId,"text":text});
		return true;
	}
	return false;
}



// Sends a POST request to the server
// Changes the session state held on the server
function sc_changeServerState() {
	$.post( "resources/client_server.php", JSON.stringify( {"state":sc_getStateString()} ) )
	.done(function() {
		// Do stuff after server update
		sc_postServerState();
	})
	.fail(function() {
		// Something went wrong, retry
		sc_changeServerState();
	});
}

// Continuously sends GET requests to the server
// Grabs the data from the server
function sc_pollServer() {
	if (currentState == DISCONNECTED) {
		return;
	}
	$.get( "resources/client_server.php", sc_handleServerResponse );
	pollingId = setTimeout(sc_pollServer, POLL_DELAY);
}

// Sends a POST request to the server
// Changes the display of a physical device
function sc_changeDeviceDisplay( message ) {
	$.post( "resources/client_server.php", JSON.stringify( message ))
	.done(function() {
		// Nothing to do here
	})
	.fail(function() {
		// Something went wrong, retry
		sc_changeDeviceDisplay( message );
	});
}


// CALLS SCRIPT.JS
// Sets up the page according to the session state
// Called after the server state has changed
function sc_postServerState() {
	if (currentState == STARTING) {
		sc_pollServer();
	} else if (currentState == DISCONNECTED) {
		devices = new Array();
	}
	onStateChanged( currentState );
}

// CALLS SCRIPT.JS
// Called each time the polling GET request returns
function sc_handleServerResponse( data ) {
	var j_data = JSON.parse(data);
	var j_devices = j_data.devices;
	
	if (currentState == DISCONNECTED) {
		return;
	} else if (currentState == STARTING) {
		// Add new devices to local list
		var epoch = new Date().getTime();
		for (var i = devices.length; i < j_devices.length; i++) {
			devices[i] = { step: 0, timestamp: epoch, color: "000000", text: "" };
			onDeviceConnect(i);
		}
	} else if (currentState == RUNNING) {
		for (var i = 0; i < devices.length; i++) {
			// Update devices if they completed a change in display
			if (j_devices[i].step > devices[i].step) {
				var updated = false;
				if (j_devices[i].color != devices[i].color) {
					updated = true;
					devices[i].color = j_devices[i].color;
				}
				if (j_devices[i].text != devices[i].text) {
					updated = true;
					devices[i].text = j_devices[i].text;
				}
				if (updated) {
					onDeviceUpdate(i);
				}
			}
		}
	}
	
	// Update PING status of devices
	var epoch = new Date().getTime();
	for (var i = 0; i < devices.length; i++) {
		if (devices[i].timestamp != 0) {
			var lapse = epoch - devices[i].timestamp;
			onDevicePing(i, lapse);
		}
		if (j_devices[i].step > devices[i].step) {
			devices[i].step = j_devices[i].step;
			devices[i].timestamp = epoch;
		}
	}
}

// Takes the current int state and returns its string form
function sc_getStateString() {
	switch(currentState) {
		case STARTING:
			return "starting";
			break;
		case RUNNING:
			return "running";
			break;
		case FINISHING:
			return "finishing";
			break;
		case DISCONNECTED:
			return "disconnected";
			break;
		default:
			// Should never happen
	}
}