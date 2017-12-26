# Song Stage Monitor für Songbeamer

Zeigt den aktuellen Liedtext für Sänger und Musiker. 
Der ganze Bildschirm wird benutzt um möglichst viel Text zu zeigen.
![Alt text](/screenshot.png?raw=true "Screenshots")

*Prefer Readme in English? [Readme English](README.md)


## Wie funktioniert es?

Die Anwendung wird auf dem selben Rechner auf dem auf Songbeamer läuft gestartet.

Im Prinzip handelt es sich um einen Webserver, welcher eine Website mit dem Text anzeigt.
Man kann also auf einen zweiten Computer oder Tablet die Website im Browser aufrufen. 
Beide Computer müssen soch im selben Netzwerk (WLAN oder LAN) befinden.
Es eignet sich zum Beispiel ein Raspberry Pi (~40€)

Die Seite ist mit Songbeamer synchronisiert und es wird automatisch die aktuelle Folie hervorgehoben.

## Einrichten
* Baue die Ausführbare jar mit: ```mvn package``` oder downloade die neueste Version herunter [usong-server-1.0.jar](/build/usong-server-1.0.jar).
* Lade Songbeamer Remote herunterund kopiere die Datei "SBRemoteClient.exe" in den selben Ordner in dem auch "usong-server.x.y.jar" liegt.
Konfiguriere "localhost" als host und aktivier die Netzwerkfunktion in Songbeamer. Siehe: http://wiki.songbeamer.de/index.php?title=Fernsteuerung
* Erstelle im selben Ordmer eine "usong.yml" Datei. Inhalt sollte folgender sein: [usong.yml](usong.yml). 
Wichtig: Als <b>songDir</b> sollte der Pfad zum Song Ordner gesetzt sein. 
![Alt text](/build/setup-example.PNG?raw=true "Setup example Screenshot")
![Alt text](/build/usong-yml-example.PNG?raw=true "usong.yml example Screenshot")

## Ausführen
* Doppelklick auf die "usong-server-x.y.jar" führt zum start des Servers. (man sieht kein Fenster, läuft im Hintergrund)
* Auf dem zweiten Computer im Browser den hostnamen oder die Ip-Adresse des "Servers" eintragen.
Z.B.: "http://derHostnme" oder "http://192.168.1.120".
* In Songbeamer Song wählen

Tip: 
* Nicht mehrere Instanzen von Server und Songbeamer starten, das führt nur zu Problemen.
* Zum Testen ob es geht, kann auch auf dem Rechner wo Songbeamer und der Server laufen 
"http://localhost" im Browser eingegeben werden.

## Admin Kontroll Oberfläche
Wenn man auf "http://&lt;hostname&gt;/song?admin=true" zugreift, kann man: 
* Aktuelle Sprache ändern
* Unabhängig von Songbeamer scrollen
* Anzahl aktiver und aktueller Clients anzeigen.

## Style ändern?
Man kann das css überschreiben. Einfache eine Datei "song2.css" im gleichen Ordner hinterlegen.

## Probleme
* Die Sprache wird nicht erkannt, da ich diese Information nicht über den Songbeamer Remote Client erfahre :(
Aktuelle Lösung: Sprache auf der Admin Oberfläche wählen.

## Gebaut mit
* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/)
* [Maven](https://maven.apache.org/)

## Acknowledgments
* [Zenscroll](https://github.com/zengabor/zenscroll) - Javascript used for scrolling