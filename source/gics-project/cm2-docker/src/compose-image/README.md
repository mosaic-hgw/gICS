![context](https://user-images.githubusercontent.com/12081369/49164555-a27e5180-f32f-11e8-8725-7b97e35134b5.png)

Current Docker-Version of gICS: ${GICS_VERSION} (${build.monthYear})

---
**Hinweis:** Diese README beschäftigt sich nur mit den Ausführen des gICS`s, Mittels vorhandenem gICS-Image (harbor.miracum.org/gics/gics:${GICS_VERSION}). Zum Einsatz kommt nur Docker-Compose.


---
## Inhaltsverzeichnis
1. Übersicht der Verzeichnisstruktur
1. Nutzung
    1. Berechtigungen setzen
    1. Starten mit Docker-Compose
    1. Verwenden von .env-Dateien
    1. Verwenden der Demo-Daten
1. Logging
1. Authentifizierung
    1. gras
    1. keycloak
    1. KeyCloak-Authentifizierung TTP-FHIR Gateway
1. Externe gICS-Datenbank einrichten
1. Fehlersuche
1. Alle verfügbaren Enviroment-Variablen
1. Additional Information

---
## 1. Übersicht der Verzeichnisstruktur

```
____compose/
  |____addins/
  |____examples/
  |  |____demo.sql
  |____logs/
  |____sqls/
  |  |____create_database_gics.sql
  |  |____create_database_gras.sql
  |  |____create_database_noti.sql
  |  |____init_database_gras_for_gics.sql
  |____docker-compose.yml
  |____gics.env
  |____LICENSE.txt
  |____mysql.env
  |____noti.env
  |____README.md (oder .pdf)
  |____ReleaseNotes.md (oder .pdf)
  |____ttp-fhir.env
  |____wildfly.env
```

---
## 2. Nutzung
Sowohl in der Nutzung mit Docker-Compose, als auch in der beschriebenen Nutzung mit Docker-Run wird ein WildFly-Image aus dem Docker-Hub von [mosaicgreifswald/wildfly](https://hub.docker.com/r/mosaicgreifswald/wildfly) heruntergeladen, welches wir für die gICS-Nutzung vorbereitet haben. Im Gegensatz zu anderen WildFly-Images kann dieses mittels Einbindung von verschiedenen Volumes direkt genutzt werden und muss nicht erst gebaut werden (bauen ist natürlich trotzdem möglich).

Egal wie der gICS gestartet wird, im Anschluss wird die gICS-Web-Oberfläche mit dieser Adresse geöffnet: **[http://localhost:8080/gics-web](http://localhost:8080/gics-web/html/public/index.xhtml)**

---
#### 2.1 Berechtigungen setzen
Bevor der gICS gestartet werden kann, muß die Berechtigung des sqls-Ordnern geändert werden.

```sh
# für den MySQL-Container
chown -R 999:999 sqls
```

---
#### 2.2. Starten mit Docker-Compose
Die einfachste und schnellste Variante ist auf einem geeignetem System docker-compose zu starten.<br>
In der Grundeinstellung wird je ein Container für MySQL und für den WildFly erstellt und gestartet.<br>
Dafür muss zunächst in das Verzeichnis gewechselt werden, in dem sich die Datei `docker-compose.yml` befindet.

```sh
# Anlegen und Starten
docker-compose up -d

# Stoppen
docker-compose stop

# wieder Starten
docker-compose start

# Stoppen und Löschen
docker-compose down
```
Der erste Start dauert bis zu 5 Minuten, da die Datenbank und der Wildfly konfiguriert werden.

---
#### 2.3. Verwenden von .env-Dateien
.env-Dateien ermöglichen das Auslagern von Enviroment-Variablen aus der .yml-Datei. In der .yml-Datei werden sie wie folgt hinterlegt. Alternativ/Zusätzlich können Enviroment-Variablen auch in die .yml-Datei geschrieben werden. Die .env-Dateien enthalten schon alle relevanten Variablen, die zum Teil nur einkommentiert, bzw. angepasst werden müssen.

```yml
services:
  mysql:
    env_file:
      - mysql.env
    ...
  wildfly:
    env_file:
      - gics.env
      - noti.env
      - ttp-fhir.env
      - wildfly.env
    ...
```

---
#### 2.4. Verwenden der Demo-Daten
Im examples-Verzeichnis befindet sich eine sql-Datei, welche einen kleinen Demo-Datensatz enthält.<br>
Die einfachste Möglichkeit die Demo-Daten einzuspielen, ist vor dem Hochfahren der Container die Datei `demo.sql` in das sql-Verzeichnis zu kopieren. Beim Hochfahren werden diese automatisch mit verarbeitet.

---
## 3. Logging
Wem die Standard-Log-Einstellungen nicht genügen, kann diese ändern.<br>
Zum einen kann mit der ENV-Variable `CONSOLE_LOG_LEVEL` der Log-Level für den Console-Handler geändert werden (Default ist *info*), zum anderen kann mit `GICS_FILE_LOG` *true* eine separate Log-Datei für den gICS angelegt werden. Die Log-Datei wird im WildFly-Container unter `${docker.wildfly.logs}` abgelegt und kann wie folgt gemountet werden.

```ini
CONSOLE_LOG_LEVEL=debug
GICS_FILE_LOG=true
GICS_LOG_LEVEL=info
```

docker-compose.yml:

```yml
services:
  wildfly:
    volumes:
      - ./logs:${docker.wildfly.logs}
```

---
## 4. Authentifizierung
In der Standard-Ausgabe vom gICS ist keine Authentifizierung notwendig. Möchte man den gICS jedoch nur für bestimmte Nutzergruppen zugänglich machen, oder sogar das Anlegen von neuen Domänen beschränken, können zwei Authentifizierungsverfahren angewendet werden. `gras` und `keycloak`, wobei es für KeyCloak zwei verschiedene Varianten gibt.

---
#### 4.1. gRAS-Authentifizierung
Um diese Variante zu nutzen, muss die ENV-Variable `GICS_AUTH_MODE` den Wert *gras* bekommen:

```ini
GICS_AUTH_MODE=gras
```

**Hinweis:** Befindet sich die gRAS-Datenbank nicht im lokalen Docker-Compose-Netzwerk, müssen die Variablen für die DB-Verbindung ebenfalls angepasst werden.


---
#### 4.2. KeyCloak-Authentifizierung
Statt gRAS kann auch eine KeyCloak-Authentifizierung eingesetzt werden.<br>
Neben der ENV-Variable `GICS_AUTH_MODE` mit den Wert *keycloak*, müssen weitere Variablen für die KeyCloak-Credentials hinzugefügt werden.

```ini
GICS_AUTH_MODE=keycloak
KEYCLOAK_SERVER_URL=<PROTOCOL://HOST_OR_IP:PORT/auth/>
KEYCLOAK_SSL_REQUIRED=<none|external|all>
KEYCLOAK_REALM=<REALM>
KEYCLOAK_RESOURCE=<RESOURCE>
KEYCLOAK_CLIENT_SECRET=<CLIENT_SECRET>
KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS=<true|false>
KEYCLOAK_CONFIDENTIAL_PORT=<CONFIDENTIAL_PORT>
```
**Hinweis:** Konfiguration des Keycloak-Server unter https://www.ths-greifswald.de/ttp-tools/keycloak

**Hinweise dazu aus der offiziellen [Keycloak Dokumentation](https://www.keycloak.org/docs/latest/securing_apps/index.html#_java_adapter_config):**

*use-resource-role-mappings*: If set to true, the adapter will look inside the token for application level role mappings for the user. If false, it will look at the realm level for user role mappings.
This is OPTIONAL. The default value is false.

*confidential-port*: The confidential port used by the Keycloak server for secure connections over SSL/TLS. This is OPTIONAL. The default value is 8443.

*ssl-required*: Ensures that all communication to and from the Keycloak server is over HTTPS. In production this should be set to all. This is OPTIONAL. The default value is external meaning that
HTTPS is required by default for external requests. Valid values are 'all', 'external' and 'none'.

---
#### 4.3. KeyCloak-Authentifizierung TTP-FHIR Gateway 
Ab TTP-FHIR Gateway Version 2.0.0 ist eine Absicherung der TTP-FHIR-Gateway-Schnittstelle je Endpunkt, wie zum Beispiel gICS, vorgesehen und nach Bedarf konfigurierbar.

Alle erforderlichen Informationen werden in der separat bereitgestellten Dokumentation erläutert.

https://www.ths-greifswald.de/ttpfhirgateway/keycloak (pdf)

Diese Dokumentation umfasst:

- Installation und Einrichtung von Keycloak
- Testung der Keycloak-Konfiguration
- Einrichtung des TTP-FHIR-Gateways für Keycloak-Authentifizierung
- Test und Benutzung des TTP-FHIR-Gateways mit Keycloak-Authentifizierung anhand von Beispielen

---
## 5. Externe gICS-Datenbank einrichten
Es ist möglich den gICS mit einer existierenden Datenbank zu verbinden. Wenn sichergestellt ist, dass die DB vom Docker-Host erreichbar ist, müssen folgende ENV-Variablen angepasst werden:

```ini
GICS_DB_HOST=<HOST_OR_IP>
GICS_DB_PORT=<PORT>
GICS_DB_NAME=<DB_NAME>
GICS_DB_USER=<DB_USER>
GICS_DB_PASS=<DB_PASSWORD>
```

Zusätzlich muss die .yml-Datei angepasst werden. Da die externe DB in meisten Fällen bereits läuft, muss der WildFly-Container nicht warten, bis der MySQL-Port *3306* verfügbar ist. Aus diesem Grund können die Werte für `depends_on`, `entrypoint` und `command` entfernt oder auskommentiert werden:

```yml
services:
  wildfly:
#    depends_on:
#      - mysql
#    entrypoint: /bin/bash
#    command: -c "./wait-for-it.sh mysql:3306 -t 120 && ./run.sh"
```

Beim Start der docker-compose, darauf achten, das jetzt nur noch der Service *wildfly* gestartet wird. Alternativ kann der Service für *mysql* auch der Compose-Datei entfernt werden.

```sh
# Nur den WildFly-Service starten
docker-compose up wildfly
```

---
## 6. Fehlersuche
* Validierung Zugriff auf KeyCloak<br>
  `curl <PROTOCOL>://<HOST_OR_IP>:<PORT>/auth/realms/<REALM>/.well-known/openid-configuration`

* `Failed to load URLs from .../.well-known/openid-configuration`<br>
  Die Keycloak-Konfiguration verweist möglicherweise auf einen falschen Realm-Eintrag. Dadurch kann die OpenId-Konfiguration nicht abgerufen werden.<br><br>

* `Unable to find valid certification path to requested target`<br>
  Der Zugriff auf den Keycloak-Server soll per https erfolgen. Dies erfordert ein passendes Zertifikat. Folgen Sie
  den [Tipps zur Generierung](https://magicmonster.com/kb/prg/java/ssl/pkix_path_building_failed/) und legen Sie das generierte Zertifikat im Root des Docker-Compose-Verzeichnisses ab.<br><br>

* `Conversation context is already active, most likely it was not cleaned up properly during previous request processing`<br>
  Der verwendete Keycloak-Nutzer wurde bei der letzten Sitzung nicht korrekt am Keycloak-Server abgemeldet. Manuell abmelden und neu versuchen.<br><br>

* Wenn man [Windows Docker Desktop](https://docs.docker.com/desktop/windows/wsl/) mit [WSL 2](https://docs.microsoft.com/de-de/windows/wsl/compare-versions) Backend verwendet, werden die Deployment-Artefakte in einer Endlosschleife neugeladen.
  Eine ausführliche Analyse des Problems findet man im [Repository des WildFly Docker Image auf github](https://github.com/jboss-dockerfiles/wildfly/issues/144).
  Das Problem tritt nicht auf, wenn man die Deployment-Artefakte in den Linux-Container kopiert, so dass die ensprechenden Markerfiles beim Start nicht mehr direkt in den Windows-Mount geschrieben werden.
  Dies passiert automatisch, wenn man in der `wildfly.env` die Variable `WILDFLY_MARKERFILES` auf *false* setzt.

---
## 7. Alle verfügbaren Enviroment-Variablen
In den env-Dateien stehen weitere Details zu den einzelnen Variablen.

#### gics.env **<-- komplett neu angelegt, Variablen aus wildfly.env**
| Kategorie | Variable       | verfügbare Werte oder Schema           | default       |
|-----------|----------------|----------------------------------------|---------------|
| Logging   | GICS_FILE_LOG  | true, false                            | false         |
| Logging   | GICS_LOG_LEVEL | TRACE, DEBUG, INFO, WARN, ERROR, FATAL | INFO          |
| Database  | GICS_DB_HOST   | \<STRING\>                             | mysql         |
| Database  | GICS_DB_PORT   | 0-65535                                | 3306          |
| Database  | GICS_DB_NAME   | \<STRING\>                             | gics          |
| Database  | GICS_DB_USER   | \<STRING\>                             | gics_user     |
| Database  | GICS_DB_PASS   | \<STRING\>                             | gics_password |
| Security  | GICS_AUTH_MODE | gras, keycloak, keycloak-json          | -             |

#### mysql.env
| Kategorie | Variable            | verfügbare Werte oder Schema | default |
|-----------|---------------------|------------------------------|---------|
| Security  | MYSQL_ROOT_PASSWORD | \<STRING\>                   | root    |

#### noti.env **<-- komplett neu angelegt**
| Kategorie | Variable       | verfügbare Werte oder Schema           | default              |
|-----------|----------------|----------------------------------------|----------------------|
| Database  | NOTI_DB_HOST   | \<STRING\>                             | mysql                |
| Database  | NOTI_DB_PORT   | 0-65535                                | 3306                 |
| Database  | NOTI_DB_NAME   | \<STRING\>                             | notification_service |
| Database  | NOTI_DB_USER   | \<STRING\>                             | noti_user            |
| Database  | NOTI_DB_PASS   | \<STRING\>                             | noti_password        |

#### ttp-fhir.env
| Kategorie   | Variable                                                      | verfügbare Werte oder Schema       | default          |
|-------------|---------------------------------------------------------------|------------------------------------|------------------|
| Security    | TTP_FHIR_KEYCLOAK_REALM                                       | \<STRING\>                         | ttp              |
| Security    | TTP_FHIR_KEYCLOAK_CLIENT_ID                                   | \<STRING\>                         | ttp-fhir         |
| Security    | TTP_FHIR_KEYCLOAK_SERVER_URL                                  | <PROTOCOL://HOST_OR_IP:PORT/auth/> | -                |
| Security    | TTP_FHIR_KEYCLOAK_CLIENT_SECRET                               | \<STRING\>                         | -                |
| Security    | TTP_FHIR_KEYCLOAK_ENABLE                                      | true, false                        | false            |
| Security    | TTP_FHIR_KEYCLOAK_ROLE_GICS_ADMIN                             | \<STRING\>                         | -                |
| Terminology | TTP_FHIR_GICS_TERMINOLOGY_FOLDER **<-- neu**                  | \<STRING\>                         | gics/terminology |
| Terminology | TTP_FHIR_GICS_TERMINOLOGY_FORCE_UPDATE_ON_STARTUP **<-- neu** | true, false                        | false            |

#### wildfly.env **<-- einige Variablen wurden in gics.env ausgegliedert**
| Kategorie  | Variable                                             | verfügbare Werte oder Schema           | default          |
|------------|------------------------------------------------------|----------------------------------------|------------------|
| Logging    | CONSOLE_LOG_LEVEL                                    | TRACE, DEBUG, INFO, WARN, ERROR, FATAL | INFO             |
| WF-Admin   | NO_ADMIN                                             | true, false                            | false            |
| WF-Admin   | ADMIN_USER                                           | \<STRING\>                             | admin            |
| WF-Admin   | WILDFLY_PASS                                         | \<STRING\>                             | wildfly_password |
| Database   | GRAS_DB_HOST                                         | \<STRING\>                             | mysql            |
| Database   | GRAS_DB_PORT                                         | 0-65535                                | 3306             |
| Database   | GRAS_DB_NAME                                         | \<STRING\>                             | gras             |
| Database   | GRAS_DB_USER                                         | \<STRING\>                             | gras_user        |
| Database   | GRAS_DB_PASS                                         | \<STRING\>                             | gras_password    |
| Security   | KEYCLOAK_SERVER_URL                                  | \<PROTOCOL://HOST_OR_IP:PORT/auth/\>   | -                |
| Security   | KEYCLOAK_SSL_REQUIRED                                | none, external, all                    | all              |
| Security   | KEYCLOAK_REALM                                       | \<STRING\>                             | -                |
| Security   | KEYCLOAK_RESOURCE **<-- ehemals KEYCLOAK_CLIENT_ID** | \<STRING\>                             | -                |
| Security   | KEYCLOAK_CLIENT_SECRET                               | \<STRING\>                             | -                |
| Security   | KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS                  | true, false                            | false            |
| Security   | KEYCLOAK_CONFIDENTIAL_PORT                           | 0-65535                                | 8443             |
| Quality    | HEALTHCHECK_URLS                                     | \<SPACE-SEPARATED-URLs\>               | -                |
| Optimizing | TZ                                                   | \<STRING\>                             | Europe/Berlin    |
| Optimizing | JAVA_OPTS                                            | \<STRING\>                             | -                |
| Optimizing | MAX_POST_SIZE                                        | \<BYTES\>                              | 10485760         |
| Optimizing | MAX_CHILD_ELEMENTS                                   | \<INTEGER\>                            | 50000            |
| Optimizing | BLOCKING_TIMEOUT **<-- neu**                         | \<SECONDS\>                            | 300              |
| Optimizing | TRANSACTION_TIMEOUT **<-- neu**                      | \<SECONDS\>                            | 300              |
| Optimizing | WILDFLY_MARKERFILES                                  | true, false, auto                      | auto             |


---
## 8. Additional Information ##

The gICS was developed by the University Medicine Greifswald and published in 2014 as part of the [MOSAIC-Project](https://ths-greifswald.de/mosaic "")  (funded by the DFG HO 1937/2-1). Selected
functionalities of gICS were developed as part of the following research projects:

- MAGIC (funded by the DFG HO 1937/5-1)
- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)

#### Credits ####

Concept and implementation: L. Geidel

Web-Client: A. Blumentritt, M. Bialke, F.M.Moser

Docker: R. Schuldt

TTP-FHIR Gateway für gICS: M. Bialke, P. Penndorf, L. Geidel, S. Lang

#### License ####
**License:** AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html<br>
**Copyright:** 2014 - ${build.year} University Medicine Greifswald<br>
**Contact:** https://www.ths-greifswald.de/kontakt/

#### Publications ####

- https://rdcu.be/b5Yck
- https://rdcu.be/6LJd
- http://dx.doi.org/10.3414/ME14-01-0133
- http://dx.doi.org/10.1186/s12967-015-0545-6

