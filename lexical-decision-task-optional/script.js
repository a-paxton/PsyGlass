/* For more information on jQuery: http://learn.jquery.com/about-jquery/how-jquery-works/ */

/* PsyGlass: An Open-Source Framework for Implementing Google Glass in Research Settings */
/* This script should be used for the optional lexical decision task. Consult readme for additional information. */
/* Written by K. Rodriguez, A. Paxton, & R. Dale */
/* Created on 8 April 2015 */
/* Last modified on 8 April 2015 */

/* CALLBACK STUFF GOES HERE */
 
// Callback for when the document is ready to be manipulated
// Useful for jQuery operations

var words = ['decision', '.'];
var wordTypes = ['w','nw'];
var curTrial = -1;
var thisInt;
var statesBySeconds = "";

function setWord( i ) {
	if (curTrial>=words.length) {
		clearDecision();
	} else {
		w = words[curTrial];
		$( "#device" + i + "_text" ).val(w);	
		$( "#device" + i + "_text" ).css('color','black');
	}
}

function decissifyNow() {
	thisInt = setInterval(function (){	
		setWord(0);
		$("#device0_button").click();
		setWord(1);	
		curTrial++;
		console.log(curTrial);
		$("#device1_button").click();	
		statesBySeconds = statesBySeconds + $( "#device0_text" ).val() + "\t" + $( "#device1_text" ).val() + "\n";
	}, 2500)
	$("#decissify_button").unbind('click').bind('click', clearDecision);
	$("#decissify_button").text('Stop Decissify!');
	$("#decissify_button").css('background-color','#440000');	
}

function clearDecision() {
	clearInterval(thisInt);
	$("#decissify_button").bind('click', decissifyNow);	
	$("#decissify_button").css('background-color','#004400');	
	$("#decissify_button").text('Lexical Decissify!');	
	$.post( "resources/save_distract.php", { statesBySeconds:statesBySeconds} )
	$( "#device0_text" ).val('[done]');	
	$( "#device1_text" ).val('[done]');	
	
}

$("#decissify_button").bind('click', decissifyNow);


