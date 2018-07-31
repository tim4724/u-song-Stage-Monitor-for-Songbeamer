# Song Stage Monitor für Songbeamer

Zeigt den aktuellen Liedtext für Sänger und Musiker. 
Der ganze Bildschirm wird benutzt um möglichst viel Text zu zeigen.
![Alt text](/screenshot.png?raw=true "Screenshots")

*Prefer Readme in English? [Readme English](README.en.md)


## Wie funktioniert es?

Die Anwendung wird auf dem selben Rechner auf dem auf Songbeamer läuft gestartet.

Im Prinzip handelt es sich um einen Webserver, welcher eine Website mit dem Liedtext anzeigt.
Man kann also auf einen zweiten Computer oder Tablet (im selben Netzwerk) die Website im Browser aufrufen. 
Es eignet sich zum Beispiel auch ein Raspberry Pi (~40€)

Die Seite ist mit Songbeamer synchronisiert und es wird automatisch die aktuelle Folie hervorgehoben.

## Einrichten
* Lade die neueste Version herunter [usong-server-2.0.0.jar](https://github.com/timbirdy/u-song-Stage-Monitor-for-Songbeamer/raw/master/build/usong-server-2.0.0.jar).
* Netzwerkfunktion in Songbeamer aktivieren. 
> Dazu müssen Sie unter Extras -> Anpassen -> Befehle -> Beta das Symbol "Netzwerk" auf die SongBeamer-Symbolleiste ziehen, und dann anklicken, bis dieses gedrückt erscheint.``` 
> https://download.songbeamer.de/SongBeamer%20Remote.pdf (NUR SCHRITTE 6 - 9)

## Ausführen
* Doppelklick auf die "usong-server-x.y.jar" führt zum start des Servers. (Man sieht ein kurzen Splash Screen)
* Auf dem zweiten Computer im Browser den hostnamen oder die Ip-Adresse des "Servers" eintragen.
Z.B.: "http://derHostnme" oder "http://192.168.1.120". 
Um die Ip-Addresse oder den Hostnamen herauszufinden, am besten das Status Fenster über das System-Tray Symbol aufrufen.
![Alt text](/system-tray-status-icon-example.png?raw=true "System Tray Status Symbol")

Tip: 
* Zum Testen ob es geht, kann auch auf dem Rechner wo Songbeamer und der Server laufen [http://localhost](http://localhost) im Browser eingegeben werden.

## Vorschau Fenster / Steuerungsfenster
Man kann das Vorschau Fenster über das System Tray aufrufen (oder "http://&lt;hostname&gt;/song?admin=true" aufrufen)
* Aktuelle Sprache ändern
* Unabhängig von SongBeamer scrollen
* Anzahl aktiver und aktueller Clients anzeigen

## Style ändern?
Man kann das css überschreiben. Einfache eine Datei "song.css" im gleichen Ordner hinterlegen.

## Probleme
* Die Sprache wird nicht erkannt, da ich diese Information nicht über den Songbeamer Remote Client erfahre :(
Aktuelle Lösung: Sprache auf der Admin Oberfläche wählen.

## Ideen / Verbesserung
* Lied Sprache automatisch erkennen
* Automatisch mit Windows starten als Option
* Zoom Buttons / Zoom Funktionalität auf der Website für Tablets
* ...

## Gebaut mit
* [Dropwizard](http://www.dropwizard.io/)
* [Maven](https://maven.apache.org/)

## Acknowledgments
* [Zenscroll](https://github.com/zengabor/zenscroll) - Javascript used for scrolling
* [SongBeamer](https://songbeamer.de/) - SongBeamer