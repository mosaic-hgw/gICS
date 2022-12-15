### Sinn und Zweck ###
Dieses Verzeichnis ist ausschließlich für Deployments, wie `.ear`- oder `.war`-Dateien. Da der WildFly beim Deployen Dateien mit der Endung `.isdeploying` und/oder `.deployed` anlegt, sollte dieses Verzeichnis einen Lese-Schreib-Zugriff haben.<br>
Seit Einführung der ENV-Variable `WILDFLY_MARKERFILES=auto` ist der Schreib-Zugriff jetzt nur noch optional. Erst wenn der Wert gleich `true` gesetzt wird, wird der Schreib-Zugriff zwingend. Nur in diesem Fall muss der Verzeichnis-Besitzer wie folgt angepasst werden:

```bash
sudo chown 1000:1000 deployments
```
Weitere Details finden Sie hier: https://access.redhat.com/documentation/en-us/jboss_enterprise_application_platform/6/html/administration_and_configuration_guide/Reference_for_Deployment_Scanner_Marker_Files1

### License ###
**License:** AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html<br>
**Copyright:** 2014 - ${build.year} University Medicine Greifswald<br>
**Contact:** https://www.ths-greifswald.de/kontakt/