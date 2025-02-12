${ttp.readme.header}

# gICS 2024.3.0

## New Features
*  Prüfung und automatische Benachrichtigung an den THSNotificationService bei Änderung der Gültigkeit einer Policy durch Zeit
*  Einbettung von gICS Formularen in externe Anwendungen
*  Individuell berechnetes Ablaufdatum für unterzeichnete Policies
*  Speicherung der vom Nutzer ausgewählten Tabellenspalten
*  Auflistung ungültiger Policies in Teilnehmeransicht

## Improvements
*  Optionale Spalten für das Datum der Unterschriften in der Dokumentenliste
*  Beschleunigter Aufruf der Dokumentenliste
*  Beschleunigter Aufruf des Dashboards
*  Allgemeine Verbesserungen in der Weboberfläche
*  Filtern nach Policies in Teilnehmeransicht

## Bug Fixes
*  Fehlende Aktualisierung der Dokumentenliste nach Leerung des Filters
*  Leerer Ausdruck bei wiederholtem Druck des selben Modulbaums


# gICS 2024.2.2

## Improvements
*  Aktualisierung der Dependency FHIR HAPI auf Version 7.4.5

## Bug Fixes
*  Bearbeiten und Speichern von Vorlagen kann zu maskiertem HTML Code führen


# gICS 2024.2.1

## Improvements
*  Aktualisierung der Dependency FHIR HAPI auf Version 7.4.3


# gICS 2024.2.0

## Improvements
*  Im- und Export von Gültigkeitsdatum und Gültigkeitszeitraum
*  Rückgabe der angelegten Einwilligung (Opt-Out) bei Aufruf von addConsentOptOut
*  Möglichkeit einen validen QC Status, auch bei offenen Auffälligkeiten zu setzen  
*  Neue Qualitätsproblem-Typen und Felder
*  Allgemeine Verbesserungen in der Weboberfläche

## Bug Fixes
*  Filterung nach Vorlagentyp liefert bei gleichem Vorlagennamen Dokumente des falschen Typs
*  Nachträgliche Änderungen des Gültigkeitsbeginns einer Vorlage werden nicht gespeichert
*  Falsch zugeordnete Modulauswahl bei via PDF eingelesenen Widerrufen
*  Policy und Modulauswahl filtern nach dem Schlüssel statt der angezeigten Bezeichnung
*  Gelegentlicher Fehler bei der Anzeige von PDFs
*  Interner Fehler bei der Suche nach nachträglich hinzugefügten Teilnehmer-IDs


# gICS 2024.1.1

## Bug Fixes
*  Widerspruch wird nicht berücksichtigt, wenn Einwilligung (Opt-Out) am selben Tag registriert wurde
*  Bearbeiten einer existierenden Vorlage vertauscht Modulreihenfolge


# gICS 2024.1.0

## New Features
*  Unterstützung für Einwilligung (Opt-Out) und Widerspruch
*  Hinzufügen von Einwilligungen (Opt-Out) ohne Modulstatus und Unterschriften
*  Konfiguration in der Weboberfläche anzuzeigender Vorlagentypen
*  Export von Vorlagen als PDF
*  Bereitstellung der Vorlagen und Policynutzung als CSV Download
*  Funktion zur Finalisierung aller Elemente einer Domäne

## Improvements
*  Optionale Finalisierung der Elemente beim Import
*  Anpassung zukünftiger ExpirationProperties in der Weboberfläche auch für finalisierte Domänen ermöglichen
*  Angabe in der Weboberfläche ob ein Dokument durch einen gesetzlicher Vertreter unterzeichnet wurde
*  Erweiterte Darstellungsoptionen im Modulbaum
*  Anzeige des Ortes der Unterschrift in Dokumentendetails
*  Optionale Prüfung auf Vorhandensein des Scans bei der Validierung von Dokumenten
*  Alphabetische Sortierung von Policy-Listen in der Weboberfläche
*  Eingabe und Anzeige langer Schlüssel in der Weboberfläche
*  Allgemeine Verbesserungen in der Weboberfläche

## Bug Fixes
*  Zeitstempel updatedAt im QCProblem wird nicht aktualisiert
*  Dokumente lassen sich ohne Scan anlegen, obwohl verpflichtender Scan in Domäne konfiguriert ist
*  ConsentTemplateKey und ConsentTemplateType werden nicht gegeneinander geprüft
*  Fehlerhafter Wert für qcPassed in ConsentStatusNotification

## Docker
*  Geänderte Logging-Variable: TTP_GICS_LOG_TO_FILE zu TTP_GICS_LOG_TO


# gICS 2023.2.1

## Bug Fixes
*  Merge Bugfixes von 2023.1.4


# gICS 2023.2.0

## New Features
*  Export einer Vorlage aus Vorlagenliste heraus
*  Umfangreiche Qualitätsprüfung in der Weboberfläche
*  Konfiguration der Qualitätsstatusoptionen in der Weboberfläche
*  Summe der Signed Policies in Statistik

## API-Changes
*  Verschiebung der ConsentNotificationMessage von gics-ejb nach gics-commons

## Improvements
*  Überarbeitete Domänenkonfiguration und Bearbeitung in der Weboberfläche
*  Ausfüllen einer Einwilligung aus Vorlagenliste heraus
*  Speichern einer Einwilligung, ohne dass eine der angebotenen Moduloptionen gewählt wurde, sofern eine versteckte Vorauswahl definiert wurde
*  Ignorieren invalidierter Dokumente beim Aufruf der Funktion getMappedTemplatesForSignerId
*  Ausblenden der Gesamtstatistik, sofern kein Recht für alle Domänen besteht
*  Neue IllegalCompositionException bei mehrfacher Verwendung von Modul/Policy in einer Vorlage oder einem Modul
*  Verwendung von JakartaEE statt JavaEE
*  Anpassbarer Context Root der Weboberfläche und SOAP-Schnittstelle
*  Berücksichtigung des flexiblen Context-Roots beim Link vom gICS zum gPAS
*  Allgemeine Verbesserung im Frontend
*  Dashboard Statistiken für Summe aller Domänen
*  Wählbarer Start und Endzeitpunkt in Statistik-Diagrammen
*  Lokalisierter Kalender in Datumsauswahl

## Bug Fixes
*  Widerrufszuordnung verhindert ggf. Ausfüllen von neuen Einwilligungen
*  Fehlerhafte Ausrichtung der Unterschriftenfelder im Ausdruck von Vorlagen und Einwilligungen
*  Unterschiedliche Größe von QR Codes beim Druck einer Vorlagen
*  Trimmen von IDs beim Anlegen von Objekten

## Docker
*  Anpassbarer Context Root der Weboberfläche und SOAP-Schnittstelle


# gICS 2023.1.4

## Bug Fixes
*  Änderungen an Modulen und Policies werden erst nach einem Neustart in AssignedModules und AssignedPolicies übernommen
*  Drucken-Dialog wird nicht angezeigt, nachdem eine Vorlage bearbeitet wurde
*  Export stellt keinen Download bereit


# gICS 2023.1.3

## Bug Fixes
*  Allgemeine Fehlerbehebungen im Frontend


# gICS 2023.1.2

## Bug Fixes
*  Mögliche NullPointerException bei Benutzung des SOAP-Interfaces ohne Authentifizierung


# gICS 2023.1.1

## Bug Fixes
*  Aktualisierung ca.uhn.hapi.fhir aufgrund von Vulnerabilities
*  Aktualisierung von slf4j-log4j12 in ths-notification-client aufgrund von Vulnerabilities in log4j 1.2.16
*  Rechtsklick "Teilnehmer ID kopieren" defekt
*  Sprung-URL zum gPAS fehlerhaft
*  Modulstatus in Consentdetails zeigt in seltenen Fällen zeitweise falsche Werte

## Docker
*  Aktualisierung Dependencies aufgrund von Vulnerabilities


# gICS 2023.1.0

## New Features
*  Funktion getAliasesForSignerIds
*  Domänenspezifische Vergabe von Berechtigungen
*  Auswahl der Spalten in der Einwilligungsliste
*  Verknüpfung zum gPAS von Teilnehmerseite aus
*  Funktion getConsentLightDto

## API-Changes
* Funktion getConsentLightDto
* Anpassung aller ServiceMethoden mit KeyParametern (z.B. ConsentKey, ConsentDate, TemplateKey) im Zuge der Verbesserung Eingabeparameter
* Anpassungen im Zuge domänenspezifische Vergabe von Berechtigungen
* Anpassungen im Zuge Einführung gICS-Cache

## Improvements
*  Detaillierterer Policystatus auf Teilnehmerseite
*  Festlegung zulässiger Einwilligungs-/Widerrufs- und Ablehnungsvorlagen
*  Konfigurierbare Verwendung des historischen Datenstandes bei Abfragen zur Vergangenheit
*  Hinweis bei 2 Dokumenten mit exakt gleichem LegalConsentDate
*  Optionale Angabe des Schlüssels bei der Erstellung von Policy, Modul, Vorlage
*  Anzeige des Gültig bis Datums in der Einwilligungsliste
*  Berücksichtigung der Reihenfolge bei der Auflistung von SignerIds verschiedener Typen
*  Intuitive Angabe des Ablaufs von Modulen und Policies
*  Alphabetische Sortierung von Objekten in der Weboberfläche
*  Verbesserte Validierung von Eingabeparametern
*  Anzeige des lesbaren Benutzernamens bei Login via OIDC (Keycloak)
*  Reduzierung des Loggings auf INFO Level

## Bug Fixes
*  [SQLIntegrityConstraintViolationException bei schnellem mehrfachen Aufruf von addConsent](https://github.com/mosaic-hgw/gICS/issues/1)
*  Import des GICS-Exchange-Format funktioniert nicht nach Wechsel Major-Version
*  Berücksichtigung der Reihenfolge bei der Auflistung von SignerIds verschiedener Typen
*  [Stored XSS Vulnerability in der Weboberfläche](https://github.com/mosaic-hgw/gICS/issues/2)
*  Fehler bei der Anzeige von Domänen-Eigenschaften im Frontend
*  Anzeige des lesbaren Benutzernamens bei Login via OIDC (Keycloak)

# gICS 2.15.2

## Improvements
*  Anpassung fester Ablaufdaten bei finalisierten Vorlagen
*  Beibehaltung der farblichen Unterscheidung von akzeptieren und abgelehnten Modulen nach Ablauf oder Invalidierung
*  Halbtransparente Darstellung von vollständig abgelaufenen ICs

## Bug Fixes
*  Fehlerhafte Sortierung bei mehrstelligen Versionsnummern
*  Fehlende Werte in Dashboad Legende

## Docker
*  Fail-Fast-Strategie für Docker-CLI-Skripte im gICS

# gICS 2.15.1

## Bug Fixes
*  Einstellungen einer Domäne werden beim Import nicht übernommen
*  Interner Fehler bei Unterschriftsdatum vor 1.2.1970
*  Fix CVE-2022-42889

# gICS 2.15.0

## New Features
*  Keycloak-basierte Absicherung der SOAP-Requests
*  Deaktivierung von Aliasen
*  Öffnen einer Teilnehmer-ID aus der Einwilligungsliste heraus
*  Dashboard Statistik für QC Zu- und Abnahme
*  Automatisches Erkennen von Policies aus CSV bei Ermittlung des Einwilligungsstatus

## Improvements
*  Dashboard Statistik für Dokumentenzunahme
*  Anzeige des Auswertungsdatums in Einwilligungsanalyse
*  Verbesserte Darstellung von QC invaliden Dokumenten
*  Anzeige des rechtlich gültigen Einwilligungsdatums
*  Auftrennung des SOAP-Interfaces in allgemeine und administrative Aufgaben
*  Upgrade auf Java 17
*  Anzeige der Uhrzeit bei digitalen Unterschriften
*  Anzeige vorhandener Haupt-IDs auch beim Alias-Verlierer
*  Klickbare Verlinkung von verknüpften Teilnehmer-IDs

## Bug Fixes
*  Fehlerhafte Bezeichnung des Feldes sinerIdTypes in SOAP Response
*  Frontend akzeptiert ungültige Datumsangaben und rechnet sie automatisch um
*  Fehler bei Policyauswahl in Einwilligungsanalyse
*  Filterung von Dokumenten anhand des Vorlagen-Name statt Vorlagen-Label

## Docker
*  Docker Upgrade auf Wildfly 26
*  Erhöhung von MAX_ALLOWED_PACKETSIZE für MySQL8 in Docker auf 10MB
*  Vereinfachung Zusammenführung der separaten Docker-Compose-Pakete der einzelnen Tools
*  OIDC-Compliance: Unterstützung KeyCloak 19 für ALLE Schnittstellen
*  Vereinheitlichung der Konfiguration der Keycloak-basierten Authentifizierung für alle Schnittstellen
*  Unterstützung Client-basierter Rollen in KeyCloak

# gICS 2.14.1

## Improvements
*  Möglichkeit zur Deaktivierung der Berechnung aufwändiger Statistiken
*  Beschleunigung der Statistikberechnung
*  Beschleunigung des Aufrufes der Einwilligungsliste

## Bug Fixes
*  Fehlerhafte Kombination von SignerIdType und SignerId führt zu internem Fehler in Teilnehmersuche
*  Ungenaue Bestimmung des Einwilligungsstatus bei gleichem Tag der Unterschrift
*  Lange Zugriffszeit auf ConsentLightDTO durch Zugriff auf Scan-Tabelle

# gICS 2.14.0

## New Features
*  Benachrichtigung anderer Systeme bei Änderung des Policystatus eines Teilnehmers
*  Angabe von Ablaufeigenschaften der gesamten Domäne im Frontend
*  Anzeige von Kommentaren im Modulbaum
*  Angabe von Datum und Ort beim Druck vorausgefüllter Vorlagen
*  Abfragezeitpunkt bei Analyse des Einwilligungsstatus im Frontend

## Improvements
*  Automatische Generierung eines passenden Dateinamens beim Export
*  Berücksichtigung des Erstelldatums von Vorlage/Modul/Policy/Domain beim Import/Export
*  Verbesserungen im Darkmode
*  Bessere Fehlerbehandlung beim FHIR-Import
*  Bessere Unterscheidbarkeit der QS Status im Dashboard
*  Weitere Kennzahlen im Dashboard Download
*  Optimierter Ausdruck des Modulbaums
*  Anzeige der externen Eigenschaften von Assigned Policies im Vorlagenbaum
*  Dokumentation Terminologie-Endpunkt Mechanismus
*  Hinweissymbol wenn ein Modul oder eine Policy externe Eigenschaften oder ein Ablaufdatum besitzt
*  Reduzierung der Datenpunkten in Dashboard Verlaufs-Diagrammen
*  Hinweis auf ungültigen Modulstatus, wenn Qualitätsstatus des IC ungültig ist
*  Eigenes Interface für Servicemethoden mit Versand von Benachrichtigungen
*  Allgemeine Verbesserungen im Frontend

## Bug Fixes
*  Verschieben von Freitextfeldern erzeugt doppelte Positionseinträge in der Datenbank
*  Möglicherweise fehlerhafte Berechnung Ablaufdatum Consent
*  Eingefügtes Unterschriftsdatum wird automatisch geleert
*  Modulablauf lässt sich aus Vorlage nicht wieder entfernen
*  SignatureDate wird bei aktuellem Consentstatus ignoriert
*  Falsche Anzeige des Policystatus nach wiederholtem Ändern des Qualitätsstatus
*  Speichern der Vorlage schlägt fehl, wenn die Domäne sehr viele Module besitzt
*  Policystatus ist true, wenn der Abfragezeitpunkt vor der ersten Einwilligung des Teilnehmers liegt

## Docker

* Anpassung und Umstrukturierung der ENV-Files. Details und Änderungsübersicht in beiliegender ReadMe.MD
* Add-In Terminology-Update Mechanismus. Details dazu im [Handbuch](https://www.ths-greifswald.de/gics/handbuch/2-14-0)
* Konfigurierbares Notification-Modul für gICS. Details zur Konfiguration [online](https://www.ths-greifswald.de/ttp-tools/notifications)

# gICS 2.13.4

## New Features

* Funktion getCurrentPolicyStatesForSignerIds
* Hinweis auf Kommentare in Listen anzeigen

## Improvements

* Verbesserte Docker-Compose Konfiguration
*  Update auf FHIR HAPI 5.6.1


## Bug Fixes
*  Fehler bei SQL-Datenübernahme gICS 2.12.x -> 2.13
*  Reihenfolge von Freitextfeldern wird nicht exportiert
*  Unvollständige Konvertierung von ConsentLightDTO zu ConsentDTO : ExpirationProperties werden ignoriert
*  Upload von Scans nicht möglich
*  Anlegen von Domains per FHIR-Import erzeugt JAXBException

# gICS 2.13.3

## Improvements
*  Alphabetische Sortierung der Policies im Frontend
*  Expiration Properties der Domäne beim Export/Import verarbeiten
*  Scans in Consent Details nach Hochladedatum sortieren
*  Fokus auf 1. Eingabefeld nur beim Erstellen, nicht beim Bearbeiten eines Objektes
*  Verbessertes Filtern von Einwilligungen im Frontend
*  Bezug zum Consent im ScanDTO ergänzen
*  Komprimiertere Darstellung der Versionen im Modulbaum

## Bug Fixes
*  Fehler bei wiederholtem Aufrufen von addSignerIdToConsent
*  Extern Properties von AssignedPolicy werden beim Bearbeiten nicht geladen
*  Bei einer Domäne mit mehreren SignerIds sind alle für die IC Erfassung erforderlich

# gICS 2.13.2

## Improvements
* Erweiterung des FHIR-Import/Export um Policy Labels
* VersionStrings nicht mehr parsen, wenn ignoreVersionNumber gewählt ist

## Bug Fixes
* In Template-Druck nur Template-Versionlabel nutzen
* FHIR-Import von Templates soll Label und Name auch kleingeschrieben tolerieren
* Fehler bei updateModule/updateConsentTemplate wenn bei nicht finalisierten Objekten abhängige Objekte (Policies/Module) hinzugefügt werden


# gICS 2.13.1

## Improvements
* Import von Änderungen an externen Eigenschaften einer Assigned Policy, wenn diese bereits in Verwendung ist
* Erhöhte Geschwindigkeit des Update Skriptes

## Bug Fixes
* GetConsentStatusType berücksichtigt Aliase nicht, wenn IdMatchingType() = AT_LEAST_ONE verwendet wird
* Fehler in Consentprüfung bei Verwendung IgnoreVersionNumber
* Fehler in Consentprüfung bei Verwendung des IdMatchingType AT_LEAST_ALL
* Sortierung von Freitextfeldern wird nicht korrekt übernommen
* Fehlende Expiration Properties beim Import einer Domain führen zu Fehler
* Korrektur englischer Übesetzungen
* Digitale Einwilligungen aus Dispatcher werden nicht korrekt in Statistik erfasst


# gICS 2.13.0

## New Features
* Erfassung mehrerer Scans je Einwilligung
* HTML Editor im Texteditor
* Optionale Bezeichnung der Vorlagenversion
* Zusammenführen von Teilnehmern durch Alias
* Erfassung des Ortes der Unterschrift
* Dashboard
* Formatierung bereinigen Funktion im Texteditor

## Improvements
* Festlegung der Reihenfolge von Teilnehmer-ID Typen
* Anmeldung auf der Startseite
* Allgemeine Verbesserungen im Frontend
* Eindeutige UUIDs/IDs für alle im gICS verwalteten Elemente
* QR Codes im Ausdruck optional
* Hinweis auf THS Standard Policies bei Policyverwaltung
* Automatisches Hinzufügen neu angelegter Module und Policies bei Vorlagenerstellung
* Anzeige von Dateiname und Hochladedatum eines Scans
* Auslagerung von Docker in separates Modul

## Bug Fixes
* Import von bereits finalisierter Vorlage kann zu Fehlermeldung führen
* Konsentierungsabfrage wirft bei unbekannter Version einen Fehler trotz ignoreVersion=true

## TTP-FHIR Gateway für gICS
Das TTP-FHIR Gateway (Version 2.0.0) für gICS umfasst die Anbindung ausgewählter gICS-Funktionalitäten zur FHIR-konformen Bereitstellung der gICS-Inhalte als FHIR-Ressourcen, so wie in der AG Einwilligungsmanagement abgestimmt.

Dabei fungiert das TTP-FHIR Gateway ausschließlich als Übersetzer zwischen FHIR-Aufrufen des anfragenden Systems und den zugeordneten gICS-Funktionalitäten. Dafür nötige Zugriffe werden innerhalb des Wildfly-Anwendungsservers per JNDI realisiert.

Das TTP-FHIR Gateway realisiert keine Anwendungslogik. Die Auswertung der übermittelten FHIR-Inhalte obliegt entsprechend dem anfragenden System.

![](https://www.ths-greifswald.de/wp-content/uploads/2021/06/fhirgateway-gics.png)

### Profilierung

Die Profilierung der erforderlichen Ressourcen und Definition der nötigen FHIR-Operations erfolgte von März 2021-Juni 2021 in Zusammenarbeit mit gefyra.

### Beispiele
Sämtliche Beispiel-Requests, Beispiel-Responses und weiterführende Informationen werden im zugehörigen Simplifier-Projekt und Implementation Guide beschrieben.

https://www.ths-greifswald.de/gics/fhir

### Weitere Details

Weitere Details sind dem Anwenderhandbuch zu entnehmen: https://www.ths-greifswald.de/gics/handbuch/

${ttp.readme.footer}