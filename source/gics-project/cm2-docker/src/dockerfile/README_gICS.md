${ttp.gics.readme.header}

---
## Inhaltsverzeichnis
1. Übersicht der Verzeichnisstruktur
1. Voraussetzungen
1. Nutzung
    1. gICS-Image Bauen
    1. gICS-Image Starten mit ENV-Variablen
    1. gICS-Image Starten mit ENV-Datei
1. Logging
1. Authentifizierung
    1. gras
    1. keycloak
1. Aktualisierung
1. Fehlersuche
1. Additional Information


---
## 1. Übersicht der Verzeichnisstruktur

```
____docker/
  |____deployments/
  |  |____gics-VERSION.ear
  |  |____gics-web-VERSION.war
  |  |____ths-notification-client-VERSION.ear
  |  |____ths-notification-service-VERSION.war
  |  |____ttp-fhir-gateway-VERSION.war
  |____jboss/
  |  |____configure_wildfly_commons.cli
  |  |____configure_wildfly_fhir.cli
  |  |____configure_wildfly_gics.cli
  |  |____configure_wildfly_gras.cli
  |  |____configure_wildfly_noti.cli
  |  |____gics_gras_jboss-web.xml
  |  |____gics_gras_web.xml
  |  |____gics_oidc_web.xml
  |____ABOUT_gICS.md
  |____Dockerfile
  |____LICENSE.txt
  |____README_gICS.md
  |____ReleaseNotes_gICS.md
```


---
## 2. Voraussetzungen
* Linux-Server für den Einsatz von Docker<br>
  -für WildFly: min. 4 GB RAM<br>
  -für MySQL (abhängig vom Datenumfang): 2 GB RAM, 10 GB HDD<br><br>

* Docker (v1.13.1 oder höher).<br>
  Prüfen mit: `sudo docker -v`<br><br>

* Zum Ausführen von Docker(-Compose) werden die Rechte von Super-User (su) benötigt.<br>
  Entweder mit `sudo su` wechseln, oder vor jedem Befehl `sudo` schreiben.<br><br>


---
## 3. Nutzung

#### 3.1. gICS-Image Bauen
Um das gICS-Image bauen zu können, müssen Sie zunächst in das Verzeichnis wechseln, wo sich auch die Datei `Dockerfile` befindet.<br>
Da das Image auf ein von uns vorbereitetes WildFly-Image ([mosaicgreifswald/wildfly](https://hub.docker.com/r/mosaicgreifswald/wildfly)) basiert, wird dies zunächst von Docker-Hub heruntergeladen. Im nächsten Schritt werden aus den Verzeichnissen `deployments` und `jboss` die notwendigen Dateien in das gICS-Image kopiert.

```sh
docker build --tag=harbor.miracum.org/gics/gics:${GICS_VERSION} .
```


#### 3.2. gICS-Image Starten mit ENV-Variablen
Im Anschluss kann das Image mit den Datenbank-Parametern wie folgt gestartet werden:

```sh
docker run --detach \
           --env TTP_GICS_DB_HOST=host_or_ip \
           --env TTP_GICS_DB_USER=gics_user \
           --env TTP_GICS_DB_PASS=gics_password \
           --publish 8080:8080 \
           --name gics-wildfly \
           harbor.miracum.org/gics/gics:${GICS_VERSION}
```

Ist der WildFly mit dem gICS fertig hochgefahren, kann die gICS-Web-Oberfläche mit dieser Adresse geöffnet werden:
**[http://localhost:8080/gics-web](http://localhost:8080/gics-web/html/public/index.xhtml)**


#### 3.3. gICS-Image Starten mit ENV-Datei
Die ENV-Variablen können, wie in diesem Beispiel zu sehen, in eine ENV-Datei ausgelagert werden.

```sh
docker run --detach \
           --env-file ttp_commons.env \
           --env-file ttp_fhir.env \
           --env-file ttp_gics.env \
           --env-file ttp_gras.env \
           --env-file ttp_noti.env \
           --publish 8080:8080 \
           --name gics-wildfly \
           harbor.miracum.org/gics/gics:${GICS_VERSION}
```


---
## 4. Logging
Wem die Standard-Log-Einstellungen nicht genügen, kann diese mit kleinen Anpassungen ändern.<br>
Zum einen kann mit der ENV-Variable `WF_CONSOLE_LOG_LEVEL` der Log-Level für den Console-Handler geändert werden (Default ist *info*):

```sh
docker run --detach \
           --env WF_CONSOLE_LOG_LEVEL=debug \
           --publish 8080:8080 \
           --name gics-wildfly \
           harbor.miracum.org/gics/gics:${GICS_VERSION}
```
Zum anderen kann mit `TTP_GICS_LOG_TO_FILE` *true* eine separate Log-Datei für den gICS angelegt werden. Diese wird im WildFly-Container unter `${docker.wildfly.logs}` abgelegt und kann wie folgt gemountet werden.

```sh
docker run --detach \
           --volume logs:${docker.wildfly.logs} \
           --env TTP_GICS_LOG_TO_FILE=true \
           --publish 8080:8080 \
           --name gics-wildfly \
           harbor.miracum.org/gics/gics:${GICS_VERSION}
```


---
## 5. Authentifizierung
In der Standard-Ausgabe vom gICS ist keine Authentifizierung notwendig, um alle Bereiche zu nutzen. Möchte man den gICS jedoch nur für bestimmte Nutzergruppen zugänglich machen, oder sogar das Anlegen von neuen Domänen beschränken, können zwei Authentifizierungsverfahren angewendet werden: `gRAS` und `KeyCloak`


#### 5.1. gRAS-Authentifizierung
Um diese Variante zu nutzen, muss die ENV-Variable `TTP_GICS_WEB_AUTH_MODE` den Wert *gras* bekommen.<br>
Außerdem müssen zusätzlich zur gICS-DB-Verbindung, noch ENV-Variablen für die gRAS-Datenbank angegeben werden:

```sh
docker run --detach \
           --env ... \
           --env TTP_GICS_WEB_AUTH_MODE=gras \
           --env TTP_GRAS_DB_HOST=host_or_ip \
           --env TTP_GRAS_DB_USER=gras_user \
           --env TTP_GRAS_DB_PASS=gras_password \
           --publish 8080:8080 \
           --name gics-wildfly \
           harbor.miracum.org/gics/gics:${GICS_VERSION}
```
**Hinweis:** Hier noch einmal der Verweis auf die Verwendung eine ENV-Datei (siehe 3.3).


#### 5.2. KeyCloak-Authentifizierung
Statt gRAS kann auch eine KeyCloak-Authentifizierung eingesetzt werden.<br>
Neben der ENV-Variable `TTP_GICS_WEB_AUTH_MODE` mit den Wert *keycloak*, müssen weitere Variablen für die KeyCloak-Credentials hinzugefügt werden:

```sh
docker run --detach \
           --env ... \
           --env TTP_GICS_WEB_AUTH_MODE=keycloak \
           --env TTP_KEYCLOAK_SERVER_URL=<PROTOCOL://HOST_OR_IP:PORT/auth/> \
           --env TTP_KEYCLOAK_SSL_REQUIRED=<none|external|all> \
           --env TTP_KEYCLOAK_REALM=<REALM> \
           --env TTP_KEYCLOAK_CLIENT_ID=<RESOURCE> \
           --env TTP_KEYCLOAK_CLIENT_SECRET=<CLIENT_SECRET> \
           --env TTP_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS=<true|false> \
           --env TTP_KEYCLOAK_CONFIDENTIAL_PORT=<CONFIDENTIAL_PORT> \
           --publish 8080:8080 \
           --name gics-wildfly \
           harbor.miracum.org/gics/gics:${GICS_VERSION}
```
**Hinweis 1:** Konfiguration des Keycloak-Server unter https://www.ths-greifswald.de/ttp-tools/keycloak.
**Hinweis 2:** Die KeyCloak-Authentifizierung mittels `keycloak-json` ist in dieser Variante nicht möglich.


---
## 6. Aktualisierung
1. Stoppen und Löschen Sie das *alte* gICS-Image mit: `docker rm -f gics-wildfly` (Evtl. müssen Sie bei sich den Container-Namen ändern.)
1. Prüfen Sie, ob es auch neue Update-SQLs im Verzeichnis `update_sqls` gibt, falls ja ...
    1. Legen Sie ein Backup Ihrer gICS-Datenbank an
    1. Spielen Sie die notwendigen Update-Skripte ein.<br>Achten Sie auf die Versions-Nummern und führen Sie nur die Skripte aus, die Sie wirklich benötigen.<br>Benötigen Sie mehr als ein Skript, ist die richtige Reihenfolge (von klein nach groß) relevant.
1. Bauen Sie das *neue* gICS-Image, wie unter 3.1 beschrieben
1. Starten Sie das *neue* gICS-Image, wie oben mehrfach beschrieben.
1. Wenn Sie sicher sind, dass die volle Funktionalität wieder hergestellt ist, können Sie jetzt ggf. das angelegte gICS-Datenbackup wieder löschen.


---
## 7. Fehlersuche
* Validierung Zugriff auf KeyCloak<br>
  `curl <PROTOCOL>://<HOST_OR_IP>:<PORT>/auth/realms/<REALM>/.well-known/openid-configuration`<br><br>

* `Failed to load URLs from .../.well-known/openid-configuration`<br>
  Die Keycloak-Konfiguration verweist möglicherweise auf einen falschen Realm-Eintrag. Dadurch kann die OpenId-Konfiguration nicht abgerufen werden.<br><br>

* `Unable to find valid certification path to requested target`<br>
  Der Zugriff auf den Keycloak-Server soll per https erfolgen. Dies erfordert ein passendes Zertifikat. Folgen Sie
  den [Tipps zur Generierung](https://magicmonster.com/kb/prg/java/ssl/pkix_path_building_failed/) und legen Sie das generierte Zertifikat im Root des Docker-Compose-Verzeichnisses ab.<br><br>

* `Conversation context is already active, most likely it was not cleaned up properly during previous request processing`<br>
  Der verwendete Keycloak-Nutzer wurde bei der letzten Sitzung nicht korrekt am Keycloak-Server abgemeldet. Manuell abmelden und neu versuchen.<br><br>

* Wenn man [Windows Docker Desktop](https://docs.docker.com/desktop/windows/wsl/) mit [WSL 2](https://docs.microsoft.com/de-de/windows/wsl/compare-versions) Backend verwendet, werden die Deployment-Artefakte in einer Endlosschleife neugeladen.
  Eine ausführliche Analyse des Problems findet man im [Repository des WildFly Docker Image auf github](https://github.com/jboss-dockerfiles/wildfly/issues/144).
  Das Problem tritt nicht auf, wenn man die Deployment-Artefakte in den Linux-Container kopiert, sodaß die entsprechenden Markerfiles beim Start nicht mehr direkt in den Windows-Mount geschrieben werden.
  Dies passiert automatisch, wenn man in der `ttp_commons.env` die Variable `WF_MARKERFILES` auf *false* setzt.
  **Intern**: Siehe auch [Endlose Redeploy Loops von compose wildfly in Windows Docker Desktop mit WSL2 Backend](https://git.icm.med.uni-greifswald.de/ths/docker/-/wikis/problems/Endlose-Redeploy-Loops-von-compose-wildfly-in-Windows-Docker-Desktop-mit-WSL2-Backend) im Docker-Wiki.


---
#${ttp.gics.readme.footer}
