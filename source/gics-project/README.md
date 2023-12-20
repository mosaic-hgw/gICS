${ttp.gics.readme.header}

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
The [JavaDoc specs for the Consent Services](https://www.ths-greifswald.de/gics/doc "")
are available online (see package `org.emau.icmvc.ganimed.ttp.cm2`).

Use SOAP-UI to create sample requests based on the WSDL files.

### Standard-Service-Interface

The WSDL URL is [http://&lt;YOUR IPADDRESS&gt;:8080/gics/gicsService?wsdl](https://demo.ths-greifswald.de/gics/gicsService?wsdl)

### Standard-Service-Interface with Notifications

The WSDL URL is [http://&lt;YOUR IPADDRESS&gt;:8080/gics/gicsServiceWithNotification?wsdl](https://demo.ths-greifswald.de/gics/gicsServiceWithNotification?wsdl)

### Management-Service-Interface

The WSDL URL is [http://&lt;YOUR IPADDRESS&gt;:8080/gics/gicsManagementService?wsdl](https://demo.ths-greifswald.de/gics/gicsManagementService?wsdl)

## FHIR

More details from https://www.ths-greifswald.de/gics/fhir

# IT-Security Recommendations #
Access to relevant application and database servers of the Trusted Third Party tools should only be possible for authorised personnel and via authorised end devices. We therefore recommend additionally implementing the following IT security measures:

* Operation of the relevant servers in separate network zones (separate from the research and supply network).
* Use of firewalls and IP filters
* Access restriction at URL level with Basic Authentication (e.g. with NGINX or Apache)
* use of Keycloak to restrict access to Web-Frontends and technical interfaces

${ttp.gics.readme.footer}