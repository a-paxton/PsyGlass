/* For more information on jQuery: http://learn.jquery.com/about-jquery/how-jquery-works/ */

/* CALLBACK STUFF GOES HERE */
 
// Callback for when the document is ready to be manipulated
// Useful for jQuery operations
function onReady() {
	$( "#experiment_button" ).click( experimentButtonClick )
}
 
// Callback for when the server has confirmed a state change
// int newState - constant representing the new state
// Possible states: DISCONNECTED, STARTING, RUNNING, FINISHING
function onStateChanged( newState ) {
	switch(newState) {
		case STARTING:
			$( "#experiment_button" ).html("Start Session");
			$( "#experiment_log" ).html("Waiting for devices...");
			break;
		case RUNNING:
			$( "#experiment_button" ).prop("disabled", false);
			$( "#experiment_button" ).html("End Session");
			$( "#experiment_log" ).html("Session is in progress");
			enableDevices();
			break;
		case FINISHING:
			$( "#experiment_button" ).prop("disabled", false);
			$( "#experiment_button" ).html("Disconnect from Server");
			$( "#experiment_log" ).html("Session has ended");
			cleanDevices();
			break;
		case DISCONNECTED:
			$( "#experiment_button" ).prop("disabled", false);
			$( "#experiment_button" ).html("Connect to Server");
			$( "#experiment_log" ).html("Disconnected from server");
			resetDevices();
			break;
		default:
			// Should never happen
	}
}

// Callback for when a device connects
// int deviceId - id of the device
function onDeviceConnect( deviceId ) {
	console.log("new device: " + deviceId);
	numDevices++;
	$( "#device" + deviceId + "_status" ).html("Connected");
	
	// Check if we have enough devices
	if (numDevices >= MIN_DEVICES) {
		$( "#experiment_button" ).prop("disabled", false);
		$( "#experiment_log" ).html("Ready");
	}
}

// Callback for when a device responds
// You can assume a response means updates have been received
// int deviceId - id of the device
function onDeviceUpdate( deviceId ) {
	$( "#device" + deviceId + "_status" ).html("Active");
	enableDevice(deviceId);
}

// Callback for the ping of a device
// int deviceId - id of the device
// ing lapse - time since device last responded in milliseconds
function onDevicePing( deviceId, lapse ) {
	var seconds = Math.floor(lapse / 1000);
	var milliseconds = lapse % 1000;
	var string = seconds > 0 ? seconds + "s" + milliseconds + "ms" : milliseconds + "ms";
	$( "#device" + deviceId + "_ping" ).html(string);
}





/* ADDIOTIONAL FUNCTIONS GO HERE */

var MIN_DEVICES = 1; // Minimum number of devices required to start session

var numDevices = 0;

// Called when the experiment button is clicked
// Tells server to change to next state
function experimentButtonClick( event ) {
	event.preventDefault();
	$( "#experiment_button" ).prop("disabled", true);
	$( "#experiment_log" ).html("Waiting for server...");
	var expectedState = changeState();
	if (expectedState == FINISHING) {
		disableDevices( "Connected" );
	}
}

// Called when the update button for a device is clicked
// Tells the server what update needs to be made
function deviceButtonClick( event ) {
	event.preventDefault();
	var index = event.data.index;
	var color = $( "#device" + index + "_color" ).val();
	if (!isValidColor("#" + color)) {
		return;
	}
	var text = $( "#device" + index + "_text" ).val();
	var changed = updateDevice(index, color, text);
	if (changed) {
		disableDevice(index, "Sending update...");
		$( "#device" + index + "_display" ).css("background-color", "#" + color);
	}
}

// Checks whether string is valid hex color
function isValidColor(str) {
    return str.match(/^#[a-f0-9]{6}$/i) !== null;
}

// Disables functionality of devices
// Changes the displayed status of devices
function disableDevices( status ) {
	for (var i = 0; i < numDevices; i++) {
		disableDevice(i, status);
	}
}

// Disables functionality of device
// Changes the displayed status of device
function disableDevice( i, status ) {
	$( "#device" + i + "_text" ).prop("disabled", true);
	$( "#device" + i + "_color" ).prop("disabled", true);
	$( "#device" + i + "_button" ).prop("disabled", true);
	$( "#device" + i + "_status" ).html(status);
}

// Enables functionality of devices
// Changes the displayed status of devices
function enableDevices() {
	for (var i= 0; i < numDevices; i++) {
		enableDevice(i);
	}
}

// Enables functionality of device
// Changes the displayed status of device
function enableDevice( i ) {
	$( "#device" + i + "_text" ).prop("disabled", false);
	$( "#device" + i + "_color" ).prop("disabled", false);
	$( "#device" + i + "_button" ).prop("disabled", false);
	$( "#device" + i + "_button" ).unbind('click').bind('click', {index: i} , deviceButtonClick );
	$( "#device" + i + "_status" ).html("Active");
}

// Resets the devices to default visual settings
function cleanDevices() {
	for (var i = 0; i < numDevices; i++) {
		$( "#device" + i + "_color" ).val("000000");
		$( "#device" + i + "_text" ).val("");
		$( "#device" + i + "_display" ).css("background-color", "#000000");
	}
}

// Resets the devices completely
function resetDevices() {
	for (var i = 0; i < numDevices; i++) {
		$( "#device" + i + "_status" ).html("Disconnected");
		$( "#device" + i + "_ping" ).html("Ping");
	}
}