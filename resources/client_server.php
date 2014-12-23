<?php
	// Is this a GET or POST request?
	$method = $_SERVER['REQUEST_METHOD'];
	if ($method == 'GET') {
		// Grab session from text file
		$session_raw = file_get_contents('session.txt');
		$session = json_decode($session_raw, true);
		if (isset($session['devices'])) {
			// Send device list
			echo $session_raw;
		} else {
			// Send empty device list
			echo '{"devices":[]}';
		}
	} else { // POST
		// Grab POST parameters and session from text file
		$j_data = json_decode(file_get_contents('php://input'), true);
		$session = json_decode(file_get_contents('session.txt'), true);
		
		if (isset($j_data['state'])) {
			// Change session state and save to text file
			if ($j_data['state'] == "starting") {
				// Reset session
				$session = [];
			} else if ($j_data['state'] == "running") {
				// Add timestamp to session
				$session['timestamp'] = time();
			}
			$session['state'] = $j_data['state'];
			file_put_contents('session.txt', json_encode($session));
		} else if (isset($j_data['index'])) {
			// Change value of target device and save session to text file
			$index = $j_data['index'];
			$devices = $session['devices'];
			$device = $devices[$index];
			if (isset($j_data['color'])) {
				$device['color'] = $j_data['color'];
			}
			if (isset($j_data['text'])) {
				$device['text'] = $j_data['text'];
			}
			$devices[$index] = $device;
			$session['devices'] = $devices;
			file_put_contents('session.txt', json_encode($session));
		}
	}
?>