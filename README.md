# Song Stage Monitor for Songbeamer

Display the current songtext for vocalists. Use the whole screen for text.
![Alt text](/screenshot.png?raw=true "Screenshots")
*Lieber auf Deutsch? [Readme Deutsch](README.de.md)

## How does it work?

You need to run this application on the same machine as you are running songbeamer. 

This application is basically a webserver which serves a website with the song text. 
A second computer (i.e. a raspberry pi ~40€) is connected to the stage monitor and is connected to the same network 
as the server and displays the website in fullscreen.

Scrolling, Highlighting and Refreshing is handled automatically and is synced to songbeamer.

## Setup
* Build the jar with: ```mvn package``` or download the latest version: [usong-server-1.0.jar](https://github.com/timbirdy/u-song-Stage-Monitor-for-Songbeamer/raw/master/build/usong-server-1.0.jar).
* Download and install the Songbeamer-Remote software and copy the "SBRemoteSender.exe" in the same directory 
as the usong-server.x.y.jar. Configure "localhost" as host and activate network in Songbeamer. 
All described here (http://wiki.songbeamer.de/index.php?title=Fernsteuerung). (Do not start Songbemaer Remote Server!)
* Place a usong.yml config file in the same directory as the usong-server.x.y.jar similar to this one: [usong.yml](usong.yml). 
Change parameter "songDir" to the path to your songs folder locally. 

Important for usong.yml: Lineending must be indicated by "LF" and not "CRLF". Use an Editor like Notepad++.

![Alt text](/build/setup-example.PNG?raw=true "Setup example Screenshot")
![Alt text](/build/usong-yml-example.PNG "usong.yml example Screenshot")

## Running
* Simply double click the jar file (or set it to autorun with your system)
* Connect your stage monitor to a computer and display the website hosted by the server in the browser. 
You need to enter the ip address or hostname of the server. I.e. "http://myhostname" oder "http://192.168.1.120"  
* Select a song in songbeamer

## Admin control site
If you access "http://&lt;hostname&gt;/song?admin=true": 
* You can change the current language of a song
* You can scroll up or down independent of songbeamer and of what is displayed on the beamer
* You can see the number of active and up-to-date clients connected

## Don't like the style?
You can override my css by providing your own "song.css" file in the same directory as the usong-server.x.y.jar.

## Problems
* The server gets all information via the songbeamer remote software. But it does not get any information about which 
language is currently used. 
Current Solution: use admin control site.

## Built With
* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management

## Contributing
If you have an idea just write me or submit a pull request :)

## Acknowledgments
* [Zenscroll](https://github.com/zengabor/zenscroll) - Javascript used for scrolling