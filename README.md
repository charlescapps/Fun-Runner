<h1> Fun Runner </h1>
<h4> An Android App by Xanthanov </h4>

Copyright &copy; 2011 Charles L. Capps <br/>
Source code released under the MIT License (<a href="http://www.opensource.org/licenses/mit-license.php">MIT License</a>)<br/>

Artwork Copyright &copy; 2011 Alice Bessoni and Charles L. Capps <br/>
Artwork released under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License (<a href="http://creativecommons.org/licenses/by-nc-sa/3.0/">Creative Commons License</a>)

Programming and Project Lead, Charles L. Capps AKA Xanthanov (<a href="https://www.github.com/Xanthanov">Github page</a>)<br/>
	&nbsp;&nbsp;&nbsp;&nbsp;<b>charles.l.capps@gmail.com</b></br>
</br>

Artwork by Alice Bessoni (<a href="http://www.alicebessoni.com/">her website</a>) <br/>
	&nbsp;&nbsp;&nbsp;&nbsp;<b>alicebessoni@gmail.com</b><br/>

<h2>Building the project</h2>
This is a standard Android project targeting Android 1.6 with Google APIs (Api Level 4, or Target ID 4). No other dependencies are necessary. <br/>
<ol>
<li>git clone git@github.com:Xanthanov/Fun-Runner.git</li>
<li>After cloning the git repo, you need to download artwork.tar from github.</li>
<li>unpack artwork.tar into the res/drawable subfolder</li>
<li>To build by command-line, simply run "ant debug" to use the ant build script. "ant install" to install on your phone.</li>
<li>If you don't use the command-line, import into Eclipse or your favorite IDE.</li>  
</ol>

<b>Javadoc can be found [here](http://xanthanov.github.com/funrunner/javadoc/index.html) </b>

<h2>Key Features</h2>

<ul>
<li>Plot your runs through the city</li>
<li>See the path you take in real-time on a map</li>
<li>Google Walking Directions spoken aloud automatically</li>
<li>Keeps track of all your stats: total distance, total time, average speed for each place you run to</li>
<li>View your previous runs in the Stats Gallery, even see the exact routes you ran on a map</li>

</ul>

<h2>What does it do?</h2>

<p><dd>
	This app helps a runner to plot a route through a city, passing by various places of interest. 
<b><i>It makes running into a game to help you get into shape!</b></i> You choose a place-category from a list, 
then choose the place you want to run to. It downloads Google walking directions, speaks them aloud, and draws them on a map. 
Note you can press the "volume up" button to repeat the directions. <b><i>And it also draws the route you actually run, as you run!</b></i>
</dd></p>

<p><dd>
	When you need the next set of directions, they will automatically be spoken aloud. You can also see your total distance, total time, and average speed 
at any step along the way. If you decided you want to run to a new place, you can just press back to change your destination. All your progress will be saved
as long as you ran a significant distance. 
</dd></p>

<p><dd>
	You can also view your previous runs in the stats gallery (<b>Press the Load button from the title screen</b>). 
You can then view the previous run on a map. (Functionality will be added to share your runs on Facebook). 	
</dd></p>

<h2>Credit for code samples</h2>
<dd>The Google Directions API doesn&apos;t provide information on how to decode the &apos;overview polyline&apos; string to draw more accurate directions. 
I found code to do so on Jeffrey Sambell&apos;s <a href="http://jeffreysambells.com/posts/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java/">blog</a>.
Permission to use code granted via email correspondence.</dd>

<h2>A few things to keep in mind</h2>

<p><dd>
	GPS can be incredibly inaccurate. Sometimes it is great, at other times it can only get your position within 100 meters. This app
gets your location directly from the GPS hardware on your phone. If the path it draws is inaccurate, blame your phone&apos;s GPS! 

<b>Please do not rate this app poorly due to inherent inaccuracy in your phone&apos;s GPS unit. Your location in this app will be identical 
to your location in Google Maps!</b>
</dd></p>
