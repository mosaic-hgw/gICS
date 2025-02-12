${ttp.gics.readme.header}

# Aktualisierung der THS-Tools per Docker

## Hintergrund

**Am folgenden Beispiel wird die Aktualisierung der Docker-Container vom gICS gezeigt. Grundsätzlich findet die Aktualisierung aller THS-Tools (E-PIX, gPAS, gICS, Dispatcher) auf dieselbe Weise statt.**

Im Beispiel wird die bestehende und laufende Instanz vom gICS als `<gics-old>` bezeichnet. Die existierende Version (`<old-version>`) soll gesichert und ein Update auf eine neue Version vom gICS (`<gics-new>`, `<new-version>`) durchgeführt werden, ohne die bereits vorhandenen Daten in der MySQL-Datenbank zu verändern.

Ob die Instanzen vom gICS laufen, kann mit folgenden Befehl geprüft werden:
```
docker ps -a
```

## Handlungsanweisung

### Neue  Tool-Version von der THS-Webseite herunterladen

Die aktuelle Version von [ths-greifswald.de/gics](https://www.ths-greifswald.de/gics) herunterladen und entpacken, sowie auf das Host-System kopieren und sicherstellen, dass entsprechende Berechtigungen zum Ausführen der Dateien gesetzt sind.

```
CHMOD -R 755 /PFAD
```

### Sichern der aktuellen Docker-Konfiguration

Um auf dem Host-System den derzeitigen Stand der gICS-Konfiguration (Wildfly-Skripte, etc.) zu sichern, den entsprechenden Ordner per TAR-Archiv sichern:

```
tar czf backup-gics-2022-03-31.tgz <gics-old>/
```

### Sichern der existierenden Datenbank

Um zusätzlich die Sicherung der existierenden Datenbank durchzuführen, wird ein MySQL-Dump über die Docker-Konsole angestoßen und die resultierende Export-Datei im Dateisystem vom Host abgelegt.

```
sudo docker exec gics-<old-version>-mysql /usr/bin/mysqldump -u gics_user -p gics > backup-gics-<old-version>-2022-03-31.sql
```

Der Name der bestehenden MySQL-Instanz muss entsprechend angepasst werden.

### Aktualisieren der Datenbank

Für alle Versionen sind die Datenbank-Aktualisierungsskripte jeweils im Docker-Verzeichnis unter *`<gics-new>/update_scripts`* zu finden. Die Update-Skripte müssen in den Docker-Container kopiert werden, wobei nur die Skripte erforderlich sind, welche die Version zwischen `<gics-old>` zu `<gics-new>` betreffen.

```
docker cp <gics-new>/update_scripts/ gics-<old-version>-mysql:/update-files/
```

Je nachdem von welcher Version aus gICS aktualisiert werden soll, müssen die relevanten SQL-Skripte *chronologisch durchlaufen* werden.

**Beispiel:** Für ein Update von Version 2.9.1 auf 2.11.0 sind demzufolge die Skripte *update_database_gics_2.9.x-2.10.0.sql* und *update_database_gics_2.10.x-2.11.0.sql* auszuführen.

Dazu per MySQL Client mit der bestehenden Datenbank verbinden und die Update-Skripte nacheinander durchlaufen. Dies kann per *docker* realisiert werden (Nutzernamen und Passwörter ggf. anpassen).

**Beispiel:**

```
docker exec -it gics-2.9.1-mysql /usr/bin/mysql -u gics_user -p -e "USE gics; $(cat gics-new/standard/update_database_gics_2.9.x-2.10.0.sql)"
docker exec -it gics-2.9.1-mysql /usr/bin/mysql -u gics_user -p -e "USE gics; $(cat gics-new/standard/update_database_gics_2.10.x-2.11.0.sql)"
```

### Aktualisieren der Deployments und Wildfly-Konfiguration

Den Datenbank-Container nun herunterfahren

```
docker gics-<old-version>-mysql down
```

#### Aktualisieren der Deployments

Die Deployments im `<gics-old>` Verzeichnis auf dem Host-System löschen und die neuen Deployments hinein kopieren:

```
rm -f <gics-old>/deployments/* 
cp -R <gics-new>/deployments/ <gics-old>/deployments/
```

#### Aktualisieren der Bezeichnung des MySQL Containers

```
sudo docker rename gics-<old-version>-mysql gics-<new-version>-mysql
```

#### Aktualisieren der JBOSS Konfigurationsskripte

Die alten Dateien können gesichert oder gelöscht und die neuen müssen eingespielt werden:

```
mv <gics-old>/jboss <gics-old>/jboss-<old-version>
cp -R <gics-new>/jboss/ <gics-old>/jboss
```

Mit Hilfe dieser Dateien wird JBOSS nach den Vorgaben aus den `*.env`-Dateien konfiguriert.

#### Aktualisieren der Umgebungsvariablen für die JBOSS Konfigurationsskripte

In der aktuellen Version liegen die zugehörigen `*.env`-Dateien im Unterordner `./envs`. In älteren Versionen lagen diese direkt im Wurzelverzeichnis des Dockerpaketes. Falls eine solche Version aktualisiert werden soll, müssen zuvor die `*.env`-Dateien in den Unterordner `./envs` verschoben werden:

```
mkdir <gics-old>/envs
mv <gics-old>/*.env <gics-old>/envs/
```

Dieser Ordner sollte auch gesichert werden:

```
cp -R <gics-old>/envs <gics-old>/envs-<old-version>
```

Nun muss die Liste der neuen und umbenannten `ENV`-Variablen in der `README_gICS.md` im Wurzelverzeichnis des Dockerpaketes studiert werden, um gegebenenfalls die `*.env`-Dateien entsprechend anzupassen. **Achtung**: unter Umständen wurden auch die Namen der `*.env`-Dateien geändert. Dies muss auf auf jeden Fall angepasst werden.

**Alternativ** kann man mit etwas höherem Aufwand die Anpassungen der alten `*.env`-Dateien manuell in die neuen übertragen, um größtmögliche Ähnlichkeit zwischen den angepassten und ausgelieferten `*.env`-Dateien zu bewahren. Dazu müssen die alten angepassten Dateien  gesichert und die neuen eingespielt werden:

```
mv <gics-old>/envs <gics-old>/envs-<old-version>
cp -R <gics-new>/envs/ <gics-old>/envs
```

Anschließend müssen alle manuellen Anpassungen der alten in die neuen `*.env`-Dateien übertragen werden. Dabei ist hohe Aufmerksamkeit erforderlich. Wir empfehlen die Nutzung eines grafischen Diff-Werkzeuges (z.B [devart Code Compare Free](https://www.devart.com/codecompare/featurematrix.html)). Für zukünftige Updates ist es hilfreich, die bestehenden Skeletons, Beispiele und Kommentare nicht zu ändern, sondern die eigenen Zeilen zu ergänzen und durch einen leicht wiederauffindbaren Kommentar zu markieren.

#### Aktualisieren der Docker-Compose-Konfiguration

Die alte angepasste Datei muss gesichert und die neue eingespielt werden:

```
mv <gics-old>/docker-compose.yml <gics-old>/docker-compose-<old-version>.yml
cp <gics-new>/docker-compose.yml <gics-old>/docker-compose.yml
```

Wahrscheinlich müssen auch hier die Anpassungen der alten `docker-compose.yml` (inbesondere für Ports und Volumes) in die neue übertragen werden (am besten wieder mit Hilfe eines grafischen Diff-Werkzeuges).

#### Aktualisieren der Dokumentationsdateien

Schließlich empfiehlt es sich, auch die aktualisierten Dokumentationsdateien zu übertragen:

```
rm -f <gics-old>/*.md 
cp <gics-new>/*.md <gics-old>/
rm -f <gics-old>/docs/* 
cp -R <gics-new>/docs/ <gics-old>/docs/
```

#### Anpassen des Eigentümer-Benutzers

```
chown 999 <gics-new>/sqls
chown 1000 <gics-new>/deployments
chown 1000 <gics-new>/logs
chown 1000 <gics-new>/jboss
```

### Starten des aktualisierten Containers

Den aktualisierten Container mittels folgendem Befehl starten (-d um Container im Hintergrund zu starten):

```
docker-compose up -d
```

Den Erfolg der Aktualisierung prüfen durch Aufruf des Web-Frontends unter [http://IPADDRESS:8080/gics-web](http://ipaddress:8080/gics-web).

## Im Fehlerfall: Wiederherstellung der Datenbank

Im Fehlerfall, kann die bisherige Datenbank wiederhergestellt werden (sofern die Anleitung befolgt wurde). Nutzernamen und Passwort ggf. anpassen.

```
docker exec -it gics-<new-version>-mysql /usr/bin/mysql -u gics_user -p -e "USE gics; $(cat backup-gics-2022-03-31.sql)"
```

${ttp.gics.readme.footer}
