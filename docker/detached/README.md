![context](https://user-images.githubusercontent.com/12081369/49164555-a27e5180-f32f-11e8-8725-7b97e35134b5.png)

Current Version: 2.11.0 (February 2020)

# Detached installation of application and database using docker-compose
In some use cases, it is necessary to install application servers and database servers on different physical hosts, e.g. ServerA and ServerB. But how is it done using docker-compose?

For demonstration purposes, follow the steps provided below to install the detached docker-compose version of gICS. 

Note: Tested with Docker 1.13.1 and Docker-Compose 1.8.0

#### Download files

```git clone https://github.com/mosaic-hgw/gICS```

#### Set up application-server

copy files from sub-folder "detached" to your app-server and change into the folder

```cd detached```

modify file-permissions for the contained deployment folder

```sudo chmod +w deployments```

modify ip-address and port within the wildfly-config file `jboss/configure_wildfly_gics.cli` accordingly. Replace `YOUR-DB-SERVER-HOST-IP-ADDRESS:YOUR-DB-SERVER-PORT` with the ip-address and port of the remote db-server.

run docker-compose to start the app-server and to connect to the remote db-server

```sudo docker-compose up```

The default publishing port of the application server is 8080. Modify if necessary in jboss/configure_wildfly_gics.cli.

Now open your browser and try out the gICS from http://YOUR-APP-SERVER-HOST-IP-ADDRESS:8080/gics-web and have fun ...

# Additional Information #

The gICS was developed by the University Medicine Greifswald  and published in 2014 as part of the [MOSAIC-Project](https://ths-greifswald.de/mosaic "")  (funded by the DFG HO 1937/2-1). Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).

## Credits ##
Concept and implementation: L. Geidel

Web-Client: A. Blumentritt, M. Bialke

Docker-Compose: R. Schuldt, M. Bialke

## License ##
License: AGPLv3, https://www.gnu.org/licenses/agpl-3.0.en.html

Copyright: 2014 - 2020 University Medicine Greifswald

Contact: https://www.ths-greifswald.de/kontakt/