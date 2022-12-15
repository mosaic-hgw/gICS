![context](https://user-images.githubusercontent.com/12081369/49164555-a27e5180-f32f-11e8-8725-7b97e35134b5.png)

Current Docker-Compose-Version of gICS: 2.14.1 (April 2022)

Current Docker-Version of TTP-FHIR-Gateway: 2.1.1 (March 2022), Details from [ReleaseNotes](https://www.ths-greifswald.de/fhirgw/releasenotes/2-1-1)

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

### Funktionsumfang
Der Funktionsumfang in der Version 2.0.0 umfasst:
* https://simplifier.net/guide/ttp-fhir-gateway-ig/allConsentsForDomain
* https://simplifier.net/guide/ttp-fhir-gateway-ig/currentConsentForPersonAndTemplate
* https://simplifier.net/guide/ttp-fhir-gateway-ig/isConsented
* https://simplifier.net/guide/ttp-fhir-gateway-ig/policyStatesForPerson
* https://simplifier.net/guide/ttp-fhir-gateway-ig/getAllConsentedIdsFor


### Beispiele
Sämtliche Beispiel-Requests, Beispiel-Responses und weiterführende Informationen werden im zugehörigen Simplifier-Projekt und Implementation Guide beschrieben.

https://simplifier.net/guide/ttp-fhir-gateway-ig/Einwilligungsmanagement

https://simplifier.net/ths-greifswald

### Weitere Details

Weitere Details sind dem Anwwenderhandbuch zu entnehmen: https://www.ths-greifswald.de/gics/handbuch/

# Additional Information #

The gICS was developed by the University Medicine Greifswald and published in 2014 as part of the [MOSAIC-Project](https://ths-greifswald.de/mosaic "")  (funded by the DFG HO 1937/2-1). Selected
functionalities of gICS were developed as part of the following research projects:

- MAGIC (funded by the DFG HO 1937/5-1)
- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)

## Credits ##

Concept and implementation: L. Geidel

Web-Client: A. Blumentritt, M. Bialke, F.M.Moser

TTP-FHIR Gateway für gICS: M. Bialke, P. Penndorf, L. Geidel, S. Lang

Docker: R. Schuldt

## License ##

License: AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html
Copyright: 2014 - 2022 University Medicine Greifswald Contact: https://www.ths-greifswald.de/kontakt/

## Publications ##
https://rdcu.be/b5Yck

https://rdcu.be/6LJd 

http://dx.doi.org/10.3414/ME14-01-0133

http://dx.doi.org/10.1186/s12967-015-0545-6
