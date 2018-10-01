# Song Stage Monitor for Songbeamer

Display the current songtext for vocalists. Use the whole screen for text.
![Alt text](/screenshot.png?raw=true "Screenshots")
*Lieber auf Deutsch? [Readme Deutsch](README.md)

## How does it work?

You need to run this application on the same machine as you are running Songbeamer. 

This application is basically a webserver which serves a website with the song text. 
A second computer on the same network displays the website (in fullscreen).

Scrolling, highlighting and refreshing is handled automatically and is synced to Songbeamer.

## Setup
* Download latest version [Releases](https://github.com/tim4724/u-song-Stage-Monitor-for-Songbeamer/releases).
* Enable network functionality in Songbeamer
![Alt text](/src/main/resources/assets/img/tutorial.png?raw=true "Enable network function")
Open Extras -> Customize -> Commands -> Beta and drag the icon "Network" to the tile bar, then click the network icon, to make it appear selected.

## Running
* Simply double click the jar file (or set it to autorun with your system)
* Display the website on the second computer. 
You need to enter the ip address or hostname of the server. I.e. "http://myhostname" oder "http://192.168.1.120".
![Alt text](/system-tray-status-icon-example.png?raw=true "System Tray Status Symbol")

## Preview window
Click on "previewFrame window" in the status tray icon context menu or access "http://&lt;hostname&gt;/song?admin=true": 
* You can change the current language of a song
* You can scroll up or down independent of Songbeamer and of what is displayed on the beamer
* You can see the number of active and up-to-date clients connected

## Don't like the style?
You can override my css by providing your own "song.css" file in the same directory as the usong-server.x.y.jar.

## Problems
* The server gets all information via the Songbeamer remote software. But it does not get any information about which 
language is currently used. 
Current Solution: use admin control site.

## Built With
* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management

## Contributing
If you have an idea just write me or submit a pull request :)

## Acknowledgments
* [Zenscroll](https://github.com/zengabor/zenscroll) - Javascript used for scrolling
* [SongBeamer](https://songbeamer.de/) - SongBeamer