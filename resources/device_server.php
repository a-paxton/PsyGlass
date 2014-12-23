<?php

	// Creates a device array object
	function newDevice($index) {
		$device = array(
			"id" => $index,
			"step" => 0,
			"color" => "000000",
			"text" => ""
		);
		return $device;
	}

	// Is this a GET or POST request?
	$method = $_SERVER['REQUEST_METHOD'];
	if ($method == 'GET') {
		// Grab session from text file
		$session = json_decode(file_get_contents('session.txt'), true);
		
		if (isset($_GET['connect'])) {
			// Device wants to connect to session
			if (isset($session['state']) && $session['state'] == "starting") {
				// Session is accepting devices
				if (isset($session['devices'])) {
					// Add device to existing list
					$devices = $session['devices'];
					$size = count($devices);
					$devices[] = newDevice($size);
					$session['devices'] = $devices;
					file_put_contents('session.txt', json_encode($session));
					echo '{"id":' . $size . '}';
				} else {
					// Add device to new list
					$devices = array( newDevice(0) );
					$session['devices'] = $devices;
					file_put_contents('session.txt', json_encode($session));
					echo '{"id":0}';
				}
			}
		} else if (isset($_GET['id'])) {
			// Update step and send back what is requested
			$id = $_GET['id'];
			if (isset($session['devices']) && count($session['devices']) > $id) {
				$devices = $session['devices'];
				$device = $devices[$id];
				
				$step = $device['step'];
				$step++;
				$device['step'] = $step;
				$devices[$id] = $device;
				$session['devices'] = $devices;
				file_put_contents('session.txt', json_encode($session));
				
				if (isset($session['timestamp'])) {
					$device['timestamp'] = $session['timestamp'];
				}
				$device['state'] = $session['state'];
				$j_device = json_encode($device);
				echo $j_device;
			}
		}
	}
?>