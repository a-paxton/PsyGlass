/* For more information on jQuery: http://learn.jquery.com/about-jquery/how-jquery-works/ */

/* PsyGlass: An Open-Source Framework for Implementing Google Glass in Research Settings */
/* This script should be used for the optional lexical decision task. Consult readme for additional information. */
/* Written by K. Rodriguez, A. Paxton, & R. Dale */
/* Created 2 September 2015 */

/* CALLBACK STUFF GOES HERE */
 
// Callback for when the document is ready to be manipulated
// Useful for jQuery operations
// This provides an example of presenting *sometimes different* instructions to the participants
// to explore whether putting them at unannounced cross purposes they do notice and how quickly

var topicsDev0 = ['popular music', 'political issues'];
var topicsDev1 = ['popular music','social issues']
var eventCat = ['sameTopic','diffTopic'];
var curTrial = -1;
var thisInt;
var statesBySeconds = "";

function setTopic( i ) {
	if (curTrial>=words.length) {
		clearTopic();
	} else {
		w = eval('topicsDev'+i+'[curTrial];');
		$( "#device" + i + "_text" ).val(w);	
		$( "#device" + i + "_text" ).css('color','black');
	}
}

function purposifyNow() {
	thisInt = setInterval(function (){	
		setTopic(0);
		$("#device0_button").click();
		setTopic(1);	
		curTrial++;
		console.log(curTrial);
		$("#device1_button").click();	
		statesBySeconds = statesBySeconds + $( "#device0_text" ).val() + "\t" + $( "#device1_text" ).val() + "\n";
	}, 2500)
	$("#purposify_button").unbind('click').bind('click', clearTopic);
	$("#purposify_button").text('Cross Purposify!');
	$("#purposify_button").css('background-color','#440000');	
}

function clearTopic() {
	clearInterval(thisInt);
	$("#purposify_button").bind('click', purposifyNow);	
	$("#purposify_button").css('background-color','#004400');	
	$("#purposify_button").text('Start Purposify!');	
	$.post( "resources/save_distract.php", { statesBySeconds:statesBySeconds} )
	$( "#device0_text" ).val('[done]');	
	$( "#device1_text" ).val('[done]');	
	
}

$("#purposify_button").bind('click', purposifyNow);


