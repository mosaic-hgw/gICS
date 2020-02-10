![context](https://user-images.githubusercontent.com/12081369/49164555-a27e5180-f32f-11e8-8725-7b97e35134b5.png)

Current Version: 2.11.0 (February 2020)

# Web-Auth Administration
The gICS Web-UI provides roles&rights to distinguish between standard users (e.g. trusted third party employees) and admin users (e.g. data trustee). See the [gICS documentation](https://ths-greifswald.de/gics/handbuch) for default user credentials and details to manage users, roles and rights with the help of mysql-procedures and docker-console commands.

# Installing the web-auth-version of gICS with docker-compose #

Tested with Docker 1.13.1 and Docker-Compose 1.8.0

Note: your account needs administrative privileges to use docker
change to super user (su) or run the following commands with sudo

Download files

```git clone https://github.com/mosaic-hgw/gICS```

grant read/write permissission to contained gICS sub-folders

```sudo chmod -R 777 gICS/docker```

change to gICS folder

```sudo cd gICS/docker/web-auth ```

if applicable: stop runnging mysql services on port 3306 

```sudo service mysql stop```

check docker version (required 1.13.1 or above)

```sudo docker -v```

check docker-compose version (required 1.8.0 or above)

```sudo docker-compose -v```

Note: The default publishing port of the application server is 8080. Modify if necessary in jboss/configure_wildfly_gics.cli

run docker-compose to pull and configure gICS

```sudo docker-compose up```

this will start pulling and configuration of mysql and jboss wildfly and automatically deployment of gICS in the current version.

installation process takes up to 7 minutes (depending on your internet connection) and succeeded if the following output is shown

![gics_install_succeeeded](https://user-images.githubusercontent.com/22166209/49724834-8f8e4a00-fc6a-11e8-9cdd-df09ce03445b.PNG)

open browser and try out the gICS from http://YOURIPADDRESS:8080/gics-web

Demo: use the webfrontend to import demo domain config and preconfigured consen template from /demo_import

finish and close gICS application server with CTRL+C

# Additional Information #

The gICS was developed by the University Medicine Greifswald  and published in 2014 as part of the [MOSAIC-Project](https://ths-greifswald.de/mosaic "")  (funded by the DFG HO 1937/2-1). Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).

## Credits ##
Concept and implementation: L. Geidel

Web-Client: A. Blumentritt, M. Bialke

## License ##
License: AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html

Copyright: 2014 - 2020 University Medicine Greifswald

Contact: https://www.ths-greifswald.de/kontakt/