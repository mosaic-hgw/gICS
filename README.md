![context](https://user-images.githubusercontent.com/12081369/49164555-a27e5180-f32f-11e8-8725-7b97e35134b5.png)

[Current Docker-Compose Version: 2.13.0 (June 2021)](https://www.ths-greifswald.de/gics/#_download "")

# About #
The Consent Management solution gICS (generic Informed Consent Administration Service) supports the management of digital informed consent documents. It facilitates checking  for various policies and modules of a consent in real time. 

![context](https://user-images.githubusercontent.com/22166209/42631209-c1a9e236-85d9-11e8-94e8-74b5022a2f43.PNG)

# Download #

[Latest Docker-compose version of gICS](https://www.ths-greifswald.de/gics/#_download "")

# Source #

https://github.com/mosaic-hgw/gICS/tree/master/source

# Live-Demo and more information #

Try out gICS from https://demo.ths-greifswald.de 

or visit https://ths-greifswald.de/gics for more information.

# API

## SOAP

All functionalities of the gICS are provided for external use via SOAP-interfaces. 

[Consent Administration-Interface (JavaDoc)](https://www.ths-greifswald.de/gics/doc "")

The WSDL URL is <strong>http://<YOUR IPADDRESS>:8080/gics/gicsService?wsdl</strong>

Use SOAP-UI to create sample requests.

## FHIR

More details from https://simplifier.net/guide/ttp-fhir-gateway-ig/Einwilligungsmanagement

# IT-Security Recommendations #

For the operation of gICS at least following IT-security measures are recommended:
* use **integrated authentication and authorization mechanism (gRAS)** or **keycloak-support** to secure access and grant privileges to gics-web (see supplementary documentation for details)
* operation in a separate network-zone
* use of firewalls and IP-filters
* access restriction to the gPAS-Servers with basic authentication (e.g. with nginx or apache)

# Additional Information #

The gICS was developed by the University Medicine Greifswald  and published in 2014 as part of the [MOSAIC-Project](https://ths-greifswald.de/mosaic "")  (funded by the DFG HO 1937/2-1). Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).

## Credits ##
Concept and implementation: L. Geidel

Web-Client: A. Blumentritt, M. Bialke

Docker: R. Schuldt

FHIR-API for gICS: M. Bialke

## License ##
License: AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html
Copyright: 2014 - 2021 University Medicine Greifswald
Contact: https://www.ths-greifswald.de/kontakt/

## Publications ##
https://rdcu.be/b5Yck

https://rdcu.be/6LJd 

http://dx.doi.org/10.3414/ME14-01-0133

http://dx.doi.org/10.1186/s12967-015-0545-6

# Supported languages #
German, English

# Screenshots #

![detail](https://user-images.githubusercontent.com/22166209/42631227-d0d2c688-85d9-11e8-9612-4f7994d4e49c.PNG)

![tree](https://user-images.githubusercontent.com/22166209/42631235-da0df7b8-85d9-11e8-9069-a3d4ad62cd53.PNG)
