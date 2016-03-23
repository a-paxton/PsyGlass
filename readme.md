#PsyGlass: An Open-Source Framework for Implementing Google Glass in Research Settings

##Getting Started
Before implementing, we recommend reading through the files to familiarize yourself with the code.

Be sure to change the server string (`public static final String ADDRESS` variable in `GameActivity.java` file) to target server address. Please note that server-connection functionalities may be limited when testing from Android Studio. We recommend publishing the signed APK with the target server address and uploading the app to your Glass to fully test server connection.

Transfer the following files and folders to the target server location before running:
+ `data.html`
+ `experiment.html`
+ `index.html`
+ `resources` folder
+ `data` folder (should be given read, write, and execute permissions)

##Additional PsyGlass Experiment Protocols
The basic PsyGlass protocol can be adapted with additional "wrappers" to adapt the experimenter console for other experimental designs. The current wrappers are listed below and can be found in the subfolder listed:
+ Lexical Decision Task (`lexical-decision-task-optional`)
	+ Modifies the experimenter console to include a "Decissify" button that will update connected Glass devices with words drawn from a provided list of words and nonwords. Lists may be modified on the associated `.js` file.
If you have created a wrapper and would like to include it, feel free to submit a pull request to add it.

##More Information
For more, see "PsyGlass: Capitalizing on Google Glass for naturalistic data collection" (Paxton, Rodriguez, & Dale, 2015, *Behavior Research Methods*).

For information on purchasing Glass for your research team, email `glass-edu(at)google(dot)com`.

**Written by**: K. Rodriguez, A. Paxton, & R. Dale
<br>**Created**: 13 October 2014
<br>**Last modified**: 23 March 2016
