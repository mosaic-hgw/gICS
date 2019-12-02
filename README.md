![gICS Logo](https://user-images.githubusercontent.com/12081369/49164555-a27e5180-f32f-11e8-8725-7b97e35134b5.png)

Current Source Code Version: 2.8.5

# About #
The Consent Management solution gICS supports the management of digital and paper-based informed consent documents. It facilitates checking for various policies and modules of a consent in real time.

![context](https://user-images.githubusercontent.com/22166209/42631209-c1a9e236-85d9-11e8-94e8-74b5022a2f43.PNG)

# Docker and source code
This repository does not provide the latest version of gpas. Please find the latest versions of gICS here:
* docker-compose: https://github.com/mosaic-hgw/Dockerbank/tree/master/gICS
* source code: https://www.ths-greifswald.de/kontakt/ 

# License
This Software was developed by the Institute for Community Medicine of the University Medicine Greifswald. It it licensed under AGPLv3 and initially provided by the DFG-funded MOSAIC-Project (grant number HO 1937/2-1).

# Build
To build gICS with maven use the goals "clean install".

# Web-based Interface
All functionalities of the gICS are provided for external use via a SOAP-Interface.

[gICSService Interface-Description (JavaDoc)](https://www.ths-greifswald.de/spezifikationen/soap/gics "gICS-Service Interface Description")

Use SOAP-UI to create sample requests. The WSDL URL is ``http://<YOUR IPADDRESS>:8080/gics/gicsService?wsdl`` (Please modify IP Address and Port accordingly).

# More Information
Concept and implementation: l.geidel, web client: a.blumentritt, m.bialke

Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).

Please cite our publications: 
http://dx.doi.org/10.3414/ME14-01-0133, 
http://dx.doi.org/10.1186/s12967-015-0545-6, 
http://dx.doi.org/10.3205/17gmds146

For more info visit https://www.ths-greifswald.de/forscher/gics/



![detail](https://user-images.githubusercontent.com/22166209/42631227-d0d2c688-85d9-11e8-9612-4f7994d4e49c.PNG)

![tree](https://user-images.githubusercontent.com/22166209/42631235-da0df7b8-85d9-11e8-9069-a3d4ad62cd53.PNG)
