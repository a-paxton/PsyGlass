<?php
	// Get data, grab device id, save to text file
	$data = file_get_contents('php://input');
	//file_put_contents('blah.txt', $data);
	$array = explode("\n", $data);
	$id = array_shift($array);
	file_put_contents('../data/data' . $id . '.txt', implode("\n", $array));
?>