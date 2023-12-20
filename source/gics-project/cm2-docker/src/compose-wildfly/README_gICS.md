${ttp.gics.readme.header}

---
**Hinweis:** Diese README beschäftigt sich nur mit den Ausführen des gICS`s ohne vorher ein eigenes gICS-Image zu bauen. Zum Einsatz kommt dafür nur Docker-Compose mit gemounteten Volumes.


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
    1. keycloak-json (alternative)
    1. KeyCloak-Authentifizierung TTP-FHIR Gateway
1. Externe gICS-Datenbank einbinden
1. Fehlersuche
1. Alle verfügbaren Enviroment-Variablen
1. Additional Information

---
## 1. Übersicht der Verzeichnisstruktur

```
____compose/
  |____addins/
  |____deployments/
  |  |____gics-VERSION.ear
  |  |____gics-web-VERSION.war
  |  |____ths-notification-client-VERSION.ear
  |  |____ths-notification-service-VERSION.war
  |  |____ttp-fhir-gateway-VERSION.war
  |____examples/
  |  |____demo_gics.sql
  |____jboss/
  |  |____configure_wildfly_commons.cli
  |  |____configure_wildfly_fhir.cli
  |  |____configure_wildfly_gics.cli
  |  |____configure_wildfly_gras.cli
  |  |____configure_wildfly_noti.cli
  |  |____gics_gras_jboss-web.xml
  |  |____gics_gras_web.xml
  |  |____gics_oidc_web.xml
  |  |____oidc.json
  |____logs/
  |____sqls/
  |  |____create_database_gics.sql
  |  |____create_database_gras.sql
  |  |____create_database_noti.sql
  |  |____init_database_gras_for_gics.sql
  |____update_sqls/
  |  |____update_database_gics_VERSION.sql
  |  |____...
  |____envs/
  |  |____mysql.env
  |  |____ttp_commons.env
  |  |____ttp_fhir.env
  |  |____ttp_gpas.env
  |  |____ttp_gras.env  
  |  |____ttp_noti.env
  |____ABOUT_gICS.md (oder .pdf)
  |____docker-compose.yml
  |____LICENSE.txt
  |____README_gICS.md (oder .pdf)
  |____ReleaseNotes_gICS.md (oder .pdf)
  
```

### Kurz-Übersicht zum Zweck der einzelnen Konfigurationsdateien

Verzeichnis|Datei|Zweck|Kategorien
---|---|---|---
|/|docker-compose.yml|Docker-Compose Basis-Konfiguration|Images, Volumes, Ports, etc.|
|/jboss/|*.CLI|Skripte zur Konfiguration des Wildfly. <br/>*Empfehlung: Keine Modifikationen*|alle|
|/envs/|mysql.env|Konfiguration MySQL-DB/-Anbindung|Security, Optimizing|
|/envs/|ttp_commons.env|Basiskonfiguration alle Interfaces und Komponenten|Logging, WF-Admin, Security,Web-Security,SOAP-Security,Quality,Optimizing|
|/envs/|ttp_fhir.env|Detailkonfiguration TTP-FHIR Gateway|Security|
|/envs/|ttp_{toolname}.env|Detailkonfiguration {toolname}|Logging,Database, Security|
|/envs/|ttp_gras.env|Detailkonfiguration gRAS (Rechte/Rollen)|Database|  
|/envs/|ttp_noti.env|Detailkonfiguration NotificationService|Logging, Database|

---
## 2. Nutzung
Sowohl in der Nutzung mit Docker-Compose, als auch in der beschriebenen Nutzung mit Docker-Run wird ein WildFly-Image aus dem Docker-Hub von [mosaicgreifswald/wildfly](https://hub.docker.com/r/mosaicgreifswald/wildfly) heruntergeladen, welches wir für die gICS-Nutzung vorbereitet haben. Im Gegensatz zu anderen WildFly-Images kann dieses mittels Einbindung von verschiedenen Volumes direkt genutzt werden und muss nicht erst gebaut werden (bauen ist natürlich trotzdem möglich).

Egal wie der gICS gestartet wird, im Anschluss wird die gICS-Web-Oberfläche mit dieser Adresse geöffnet: **[http://localhost:8080/gics-web](http://localhost:8080/gics-web/html/public/index.xhtml)**

---
#### 2.1. Berechtigungen setzen
Bevor der gICS gestartet werden kann, müssen Berechtigungen auf den Ordnern geändert werden. Diese sind notwendig, damit der Container nicht nur die Ordner lesen, sondern auch beschreiben kann.

```sh
# für den MySQL-Container
chown -R 999:999 sqls

# für den WildFly-Container
chown -R 1000:1000 deployments jboss logs
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
.env-Dateien ermöglichen das Auslagern von Enviroment-Variablen aus der .yml-Datei und werden wie folgt verwendet. Zusätzlich können Enviroment-Variablen auch in die .yml-Datei geschrieben werden. Die .env-Dateien enthalten schon alle relevanten Variablen, die zum Teil nur einkommentiert, bzw. angepasst werden müssen.

```yml
services:
  mysql:
    env_file:
      - mysql.env
    ...
  wildfly:
    env_file:
      - ttp_commons.env
      - ttp_fhir.env
      - ttp_gics.env
      - ttp_gras.env
      - ttp_noti.env
    ...
```

---
#### 2.4. Verwenden der Demo-Daten
Im examples-Verzeichnis befindet sich eine sql-Datei, welche einen kleinen Demo-Datensatz enthält.<br>
Die einfachste Möglichkeit die Demo-Daten einzuspielen, ist vor dem Hochfahren der Container die Datei `demo.sql` in das sql-Verzeichnis zu kopieren. Beim Hochfahren werden diese automatisch mit verarbeitet.

---
## 3. Logging
Wem die Standard-Log-Einstellungen nicht genügen, kann diese ändern.<br>
Zum einen kann mit der ENV-Variable `WF_CONSOLE_LOG_LEVEL` der Log-Level für den Console-Handler geändert werden (Default ist *info*), zum anderen kann mit `TTP_GICS_LOG_TO_FILE` *true* eine separate Log-Datei für den gICS angelegt werden. Die Log-Datei wird im WildFly-Container unter `${docker.wildfly.logs}` abgelegt und kann wie folgt gemountet werden.

```ini
WF_CONSOLE_LOG_LEVEL=debug
TTP_GICS_LOG_TO_FILE=true
TTP_GICS_LOG_LEVEL=info
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
Um diese Variante zu nutzen, muss die ENV-Variable `TTP_GICS_WEB_AUTH_MODE` den Wert *gras* bekommen:

```ini
TTP_GICS_WEB_AUTH_MODE=gras
```

**Hinweis:** Befindet sich die gRAS-Datenbank nicht im lokalen Docker-Compose-Netzwerk, müssen die Variablen für die DB-Verbindung ebenfalls angepasst werden.


---
#### 4.2. KeyCloak-Authentifizierung gICS-Web
Statt gRAS kann auch eine KeyCloak-Authentifizierung eingesetzt werden.<br>
Neben der ENV-Variable `TTP_GICS_WEB_AUTH_MODE` mit den Wert *keycloak*, müssen weitere Variablen für die KeyCloak-Credentials hinzugefügt werden.

```ini
TTP_GICS_WEB_AUTH_MODE=keycloak
TTP_KEYCLOAK_SERVER_URL=<PROTOCOL://HOST_OR_IP:PORT/auth/>
TTP_KEYCLOAK_SSL_REQUIRED=<none|external|all>
TTP_KEYCLOAK_REALM=<REALM>
# TTP_KEYCLOAK_CLIENT_ID is the new alias from KEYCLOAK_RESOURCE
TTP_KEYCLOAK_CLIENT_ID=<CLIENT_ID>
TTP_KEYCLOAK_CLIENT_SECRET=<CLIENT_SECRET>
TTP_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS=<true|false>
TTP_KEYCLOAK_CONFIDENTIAL_PORT=<CONFIDENTIAL_PORT>
```
**Hinweis:** Konfiguration des Keycloak-Server unter https://www.ths-greifswald.de/ttp-tools/keycloak

**Hinweise dazu aus der offiziellen [Keycloak Dokumentation](https://www.keycloak.org/docs/latest/securing_apps/index.html#_java_adapter_config):**

*use-resource-role-mappings*: If set to true, the adapter will look inside the token for application level role mappings for the user. If false, it will look at the realm level for user role mappings.
This is OPTIONAL. The default value is false.

*confidential-port*: The confidential port used by the Keycloak server for secure connections over SSL/TLS. This is OPTIONAL. The default value is 8443.

*ssl-required*: Ensures that all communication to and from the Keycloak server is over HTTPS. In production this should be set to all. This is OPTIONAL. The default value is external meaning that
HTTPS is required by default for external requests. Valid values are 'all', 'external' and 'none'.

---

#### 4.3. KeyCloak-Authentifizierung gICS-Web (die JSON-Alternative)

Für diese Variante muss eine JSON-Datei `oidc.json` im jboss-Verzeichnis angepasst werden, dessen Werte aus der lokalen KeyCloak-Instanz entnommen werden können.

```json
{
  "client-id": "<RESOURCE>",
  "provider-url": "<PROTOCOL>://<HOST_OR_IP>:<PORT>/auth/realms/<REALM>",
  "ssl-required": "<none|external|all>",
  "verify-token-audience": true,
  "credentials": {
    "secret": "<CLIENT_SECRET>"
  },
  "use-resource-role-mappings": false,
  "confidential-port": 8443
}

```
**Hinweis:** Wenn `use-resource-role-mappings` gleich *true* ist, müssen die Rollen am Client definiert sein.

Zusätzlich braucht nur der Wert der ENV-Variable `TTP_GICS_WEB_AUTH_MODE` auf *keycloak-json* gesetzt werden und der WildFly bezieht die KeyCloak-Credentials aus der JSON-Datei.

```ini
TTP_GICS_WEB_AUTH_MODE: keycloak-json
```
---
#### 4.4. KeyCloak-Authentifizierung TTP-FHIR Gateway
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
TTP_GICS_DB_HOST=<HOST_OR_IP>
TTP_GICS_DB_PORT=<PORT>
TTP_GICS_DB_NAME=<DB_NAME>
TTP_GICS_DB_USER=<DB_USER>
TTP_GICS_DB_PASS=<DB_PASSWORD>
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
  Das Problem tritt nicht auf, wenn man die Deployment-Artefakte in den Linux-Container kopiert, sodaß die entsprechenden Markerfiles beim Start nicht mehr direkt in den Windows-Mount geschrieben werden.
  Dies passiert automatisch, wenn man in der `ttp_commons.env` die Variable `WF_MARKERFILES` auf *false* setzt.

---
## 7. Alle verfügbaren Enviroment-Variablen
In den env-Dateien stehen weitere Details zu den einzelnen Variablen.

#### ./envs/ttp_gics.env **<-- ehemals gics.env**
| Kategorie | Variable                                                | verfügbare Werte oder Schema           | default                                              |
|-----------|---------------------------------------------------------|----------------------------------------|------------------------------------------------------|
| Logging   | TTP_GICS_LOG_TO_FILE **<-- Alias von GICS_FILE_LOG**    | true, false                            | false                                                |
| Logging   | TTP_GICS_LOG_LEVEL **<-- Alias von GICS_LOG_LEVEL**     | TRACE, DEBUG, INFO, WARN, ERROR, FATAL | INFO                                                 |
| Database  | TTP_GICS_DB_HOST **<-- Alias von GICS_DB_HOST**         | \<STRING\>                             | mysql                                                |
| Database  | TTP_GICS_DB_PORT **<-- Alias von GICS_DB_PORT**         | 0-65535                                | 3306                                                 |
| Database  | TTP_GICS_DB_NAME **<-- Alias von GICS_DB_NAME**         | \<STRING\>                             | gics                                                 |
| Database  | TTP_GICS_DB_USER **<-- Alias von GICS_DB_USER**         | \<STRING\>                             | gics_user                                            |
| Database  | TTP_GICS_DB_PASS **<-- Alias von GICS_DB_PASS**         | \<STRING\>                             | gics_password                                        |
| Security  | TTP_GICS_WEB_AUTH_MODE **<-- Alias von GICS_AUTH_MODE** | gras, keycloak, keycloak-json          | -                                                    |
| Security  | TTP_GICS_SOAP_KEYCLOAK_ENABLE **<-- neu**               | true, false                            | -                                                    |
| Security  | TTP_GICS_SOAP_ROLE_USER_NAME **<-- neu**                | \<STRING\>                             | role.gics.user                                       |
| Security  | TTP_GICS_SOAP_ROLE_USER_SERVICES **<-- neu**            | \<STRING\>                             | /gicas/gicsService,/gics/gicsServiceWithNotification |
| Security  | TTP_GICS_SOAP_ROLE_ADMIN_NAME **<-- neu**               | \<STRING\>                             | role.gics.admin                                      |
| Security  | TTP_GICS_SOAP_ROLE_ADMIN_SERVICES **<-- neu**           | \<STRING\>                             | /gics/gicsManagementService,/gics/gicsFhirService    |
| Security  | TTP_GICS_AUTH_DOMAIN_ROLES **<-- neu**                  | DISABLED, FORCED, IMPLIED              | IMPLIED                                              |

#### ./envs/ttp_noti.env **<-- ehemals noti.env**
| Kategorie | Variable                                      | verfügbare Werte oder Schema | default              |
|-----------|-----------------------------------------------|------------------------------|----------------------|
| Database  | TTP_NOTI_DB_HOST **<-- Alias von NOTI_DB_HOST | \<STRING\>                   | mysql                |
| Database  | TTP_NOTI_DB_PORT **<-- Alias von NOTI_DB_PORT | 0-65535                      | 3306                 |
| Database  | TTP_NOTI_DB_NAME **<-- Alias von NOTI_DB_NAME | \<STRING\>                   | notification_service |
| Database  | TTP_NOTI_DB_USER **<-- Alias von NOTI_DB_USER | \<STRING\>                   | noti_user            |
| Database  | TTP_NOTI_DB_PASS **<-- Alias von NOTI_DB_PASS | \<STRING\>                   | noti_password        |

#### ./envs/ttp_fhir.env **<-- ehemals fhir.env**
| Kategorie   | Variable                                          | verfügbare Werte oder Schema         | default          |
|-------------|---------------------------------------------------|--------------------------------------|------------------|
| Security    | TTP_FHIR_KEYCLOAK_ENABLE                          | true, false                          | false            |
| Security    | TTP_FHIR_KEYCLOAK_REALM                           | \<STRING\>                           | ttp              |
| Security    | TTP_FHIR_KEYCLOAK_CLIENT_ID                       | \<STRING\>                           | fhir             |
| Security    | TTP_FHIR_KEYCLOAK_SSL_REQUIRED                    | none, external, all                  | all              |
| Security    | TTP_FHIR_KEYCLOAK_SERVER_URL                      | \<PROTOCOL://HOST_OR_IP:PORT/auth/\> | -                |
| Security    | TTP_FHIR_KEYCLOAK_CLIENT_SECRET                   | \<STRING\>                           | -                |
| Security    | TTP_FHIR_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS      | true, false                          | false            |
| Security    | TTP_FHIR_KEYCLOAK_CONFIDENTIAL_PORT               | 0-65535                              | 8443             |
| Security    | TTP_FHIR_KEYCLOAK_ROLE_GICS_USER                  | \<STRING\>                           | role.gics.user   |
| Security    | TTP_FHIR_KEYCLOAK_ROLE_GICS_ADMIN                 | \<STRING\>                           | role.gics.admin  |
| Terminology | TTP_FHIR_GICS_TERMINOLOGY_FOLDER                  | \<STRING\>                           | gics/terminology |
| Terminology | TTP_FHIR_GICS_TERMINOLOGY_FORCE_UPDATE_ON_STARTUP | true, false                          | false            |

**Hinweis:** Details zur Bedeutung des Terminologie-Imports sind im [gICS-Handbuch](https://www.ths-greifswald.de/gics/handbuch) im Abschnitt 'ADD-INS: Aktualisierung von Terminologien per
Import' beschrieben.

#### ./envs/ttp_gras.env **<-- neu, Werte aus ttp_commons.env ausgelagert**
| Kategorie | Variable                                      | verfügbare Werte oder Schema | default       |
|-----------|-----------------------------------------------|------------------------------|---------------|
| Database  | TTP_GRAS_DB_HOST **<-- Alias von GRAS_DB_HOST | \<STRING\>                   | mysql         |
| Database  | TTP_GRAS_DB_PORT **<-- Alias von GRAS_DB_PORT | 0-65535                      | 3306          |
| Database  | TTP_GRAS_DB_NAME **<-- Alias von GRAS_DB_NAME | \<STRING\>                   | gras          |
| Database  | TTP_GRAS_DB_USER **<-- Alias von GRAS_DB_USER | \<STRING\>                   | gras_user     |
| Database  | TTP_GRAS_DB_PASS **<-- Alias von GRAS_DB_PASS | \<STRING\>                   | gras_password |

#### ./envs/ttp_commons.env **<-- ehemals wildfly.env**
| Kategorie     | Variable                                                                                      | verfügbare Werte oder Schema           | default          |
|---------------|-----------------------------------------------------------------------------------------------|----------------------------------------|------------------|
| Logging       | WF_CONSOLE_LOG_LEVEL **<-- ehemals CONSOLE_LOG_LEVEL**                                        | TRACE, DEBUG, INFO, WARN, ERROR, FATAL | INFO             |
| Logging       | TTP_AUTH_LOG_TO_FILE **<-- neu**                                                              | true, false                            | false            |
| Logging       | TTP_AUTH_LOG_LEVEL **<-- neu**                                                                | TRACE, DEBUG, INFO, WARN, ERROR, FATAL | INFO             |
| Logging       | TTP_WEB_LOG_TO_FILE **<-- neu**                                                               | true, false                            | false            |
| Logging       | TTP_WEB_LOG_LEVEL **<-- neu**                                                                 | TRACE, DEBUG, INFO, WARN, ERROR, FATAL | INFO             |
| WF-Admin      | WF_NO_ADMIN **<-- ehemals NO_ADMIN**                                                          | true, false                            | false            |
| WF-Admin      | WF_ADMIN_USER **<-- ehemals ADMIN_USER**                                                      | \<STRING\>                             | admin            |
| WF-Admin      | WF_ADMIN_PASS **<-- ehemals WILDFLY_PASS**                                                    | \<STRING\>                             | wildfly_password |
| Security      | TTP_KEYCLOAK_SERVER_URL **<-- Alias von KEYCLOAK_SERVER_URL**                                 | \<PROTOCOL://HOST_OR_IP:PORT/auth/\>   | -                |
| Security      | TTP_KEYCLOAK_SSL_REQUIRED **<-- Alias von KEYCLOAK_SSL_REQUIRED**                             | none, external, all                    | all              |
| Security      | TTP_KEYCLOAK_REALM **<-- Alias von KEYCLOAK_REALM**                                           | \<STRING\>                             | -                |
| Security      | TTP_KEYCLOAK_CLIENT_ID **<-- Alias von KEYCLOAK_RESOURCE**                                    | \<STRING\>                             | -                |
| Security      | TTP_KEYCLOAK_CLIENT_SECRET **<-- Alias von KEYCLOAK_CLIENT_SECRET**                           | \<STRING\>                             | -                |
| Security      | TTP_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS **<-- Alias von KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS** | true, false                            | false            |
| Security      | TTP_KEYCLOAK_CONFIDENTIAL_PORT **<-- Alias von KEYCLOAK_CONFIDENTIAL_PORT**                   | 0-65535                                | 8443             |
| Web-Security  | TTP_WEB_KEYCLOAK_REALM **<-- neu**                                                            | \<STRING\>                             | ttp              |
| Web-Security  | TTP_WEB_KEYCLOAK_CLIENT_ID **<-- neu**                                                        | \<STRING\>                             | ths              |
| Web-Security  | TTP_WEB_KEYCLOAK_SERVER_URL **<-- neu**                                                       | \<PROTOCOL://HOST_OR_IP:PORT/auth/\>   | -                |
| Web-Security  | TTP_WEB_KEYCLOAK_SSL_REQUIRED **<-- neu**                                                     | none, external, all                    | all              |
| Web-Security  | TTP_WEB_KEYCLOAK_CLIENT_SECRET **<-- neu**                                                    | \<STRING\>                             | -                |
| Web-Security  | TTP_WEB_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS **<-- neu**                                       | true, false                            | false            |
| Web-Security  | TTP_WEB_KEYCLOAK_CONFIDENTIAL_PORT **<-- neu**                                                | 0-65535                                | 8443             |
| SOAP-Security | TTP_SOAP_KEYCLOAK_REALM **<-- neu**                                                           | \<STRING\>                             | ttp              |
| SOAP-Security | TTP_SOAP_KEYCLOAK_CLIENT_ID **<-- neu**                                                       | \<STRING\>                             | ths              |
| SOAP-Security | TTP_SOAP_KEYCLOAK_SERVER_URL **<-- neu**                                                      | \<PROTOCOL://HOST_OR_IP:PORT/auth/\>   | -                |
| SOAP-Security | TTP_SOAP_KEYCLOAK_SSL_REQUIRED **<-- neu**                                                    | none, external, all                    | all              |
| SOAP-Security | TTP_SOAP_KEYCLOAK_CLIENT_SECRET **<-- neu**                                                   | \<STRING\>                             | -                |
| SOAP-Security | TTP_SOAP_KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS **<-- neu**                                      | true, false                            | false            |
| SOAP-Security | TTP_SOAP_KEYCLOAK_CONFIDENTIAL_PORT **<-- neu**                                               | 0-65535                                | 8443             |
| Quality       | WF_HEALTHCHECK_URLS **<-- ehemals HEALTHCHECK_URLS**                                          | \<SPACE-SEPARATED-URLs\>               | -                |
| Optimizing    | WF_ADD_CLI_FILTER **<-- neu**                                                                 | \<SPACE-SEPARATED-STRING\>             | -                |
| Optimizing    | WF_MAX_POST_SIZE **<-- Alias von MAX_POST_SIZE**                                              | \<BYTES\>                              | 10485760         |
| Optimizing    | WF_MAX_CHILD_ELEMENTS **<-- Alias von MAX_CHILD_ELEMENTS**                                    | \<INTEGER\>                            | 50000            |
| Optimizing    | WF_BLOCKING_TIMEOUT **<-- neu**                                                               | \<SECONDS\>                            | 300              |
| Optimizing    | WF_TRANSACTION_TIMEOUT **<-- neu**                                                            | \<SECONDS\>                            | 300              |
| Optimizing    | WF_DISABLE_HTTP2 **<-- neu**                                                                  | true, false                            | false            |
| Optimizing    | WF_MARKERFILES **<-- ehemals WILDFLY_MARKERFILES**                                            | true, false, auto                      | auto             |
| Optimizing    | TZ                                                                                            | \<STRING\>                             | Europe/Berlin    |
| Optimizing    | JAVA_OPTS                                                                                     | \<STRING\>                             | -                |

#### ./envs/mysql.env
| Kategorie  | Variable            | verfügbare Werte oder Schema | default       |
|------------|---------------------|------------------------------|---------------|
| Security   | MYSQL_ROOT_PASSWORD | \<STRING\>                   | root          |
| Optimizing | TZ **<-- neu**      | \<STRING\>                   | Europe/Berlin |

---
#${ttp.gics.readme.footer}
