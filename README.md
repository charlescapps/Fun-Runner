<h1> Fun Runner </h1>
<h4> An Android App by Xanthanov </h4>

Copyright &copy; 2011 Charles L. Capps <br/>
Released under the MIT License (<a href="http://www.opensource.org/licenses/mit-license.php">http://www.opensource.org/licenses/mit-license.php)</br>
</br>

Programming and Project Lead, Charles L. Capps AKA Xanthanov (<a href="https://www.github.com/Xanthanov">Github page</a>)<br/>
	&nbsp;&nbsp;&nbsp;&nbsp;<b>charles.l.capps@gmail.com</b></br>
</br>

Artwork by Alice Bessoni (<a href="http://www.alicebessoni.com/">her website</a>) <br/>
	&nbsp;&nbsp;&nbsp;&nbsp;<b>alicebessoni@gmail.com</b><br/>

<p><dd>
	This app helps a runner to plot a route through a city, passing by various places of interest. 
<b><i>It makes running into a game to help you get into shape!</b></i> You choose a place-category from a list, 
then choose the place you want to run to. It downloads Google walking directions, speaks them aloud, and draws them on a map. 
Note you can press the "volume up" button to repeat the directions. <b><i>And it also draws the route you actually run, as you run!</b></i>
</dd></p>

<p><dd>
	You can also view your previous runs in the stats gallery (<b>Press the Load button from the title screen</b>). 
You can then view the previous run on a map. (Functionality will be added to share your runs on Facebook). 	
</dd></p>

<h2>Credit for code samples</h2>
The Google Directions API doesn&apos;t provide information on how to decode the "overview_polyline" string to draw more accurate directions. 
I found code to do so on Jeffrey Sambell&apos;s blog here: <a>http://jeffreysambells.com/posts/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java/</a>

<h2>A few things to keep in mind</h2>

<p><dd>
	GPS can be incredibly inaccurate. Sometimes it is great, at other times it can only get your position within 100 meters. This app
gets your location directly from the GPS hardware on your phone. If the path it draws is inaccurate, blame your phone&apos;s GPS! 

<b>Please do not rate this app poorly due to inherent inaccuracy in your phone&apos;s GPS unit. Your location in this app will be identical 
to your location in Google Maps!</b>
</dd></p>
