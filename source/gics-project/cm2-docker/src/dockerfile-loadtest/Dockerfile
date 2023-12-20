FROM mosaicgreifswald/wildfly:wildfly-mysql-jmeter

# ###license-information-start###
# gPAS - a Generic Pseudonym Administration Service
# __
# Copyright (C) 2013 - ${build.year} Independent Trusted Third Party of the University Medicine Greifswald
# 							kontakt-ths@uni-greifswald.de

# 							concept and implementation
# 							l.geidel
# 							web client
# 							a.blumentritt
# 							docker
# 							r.schuldt

# 							please cite our publications
# 							http://dx.doi.org/10.3414/ME14-01-0133
# 							http://dx.doi.org/10.1186/s12967-015-0545-6
# __
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
# ###license-information-end###

# envs
ENV TTP_${project.upperName}_VERSION="${project.version}" \
    TTP_${project.upperName}_WEB_AUTH_MODE="none" \
    TTP_${project.upperName}_DB_HOST="localhost" \
    TTP_${project.upperName}_LOG_TO_FILE="true" \
    TTP_${project.upperName}_LOG_LEVEL="INFO" \
    \
    ${project.upperName}_SOAP_PROTOCOL="http" \
    ${project.upperName}_SOAP_HOST="localhost" \
    ${project.upperName}_SOAP_PORT="8080" \
    \
    JMETER_MODE="local" \
    TEST_THREADS="1" \
    TEST_RAMP_UP="1" \
    TEST_LOOPS="40" \
    TEST_AGGREGATE_REPORT="${ENTRY_JMETER_LOGS}/gics_loadtest_report.csv" \
    \
    WF_MAX_CHILD_ELEMENTS="1000001" \
    WF_MAX_POST_SIZE="104857600" \
    JAVA_OPTS="-server -Xms1G -Xmx3G -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=1G -XX:StringTableSize=1000003 -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Djboss.modules.system.pkgs=org.jboss.byteman"

# annotations
LABEL maintainer                           = "ronny.schuldt@uni-greifswald.de" \
      org.opencontainers.image.authors     = "university-medicine greifswald" \
      org.opencontainers.image.source      = "https://github.com/mosaic-hgw/${project.name}" \
      org.opencontainers.image.vendor      = "uni-greifswald.de" \
      org.opencontainers.image.title       = "${project.name}-test" \
      org.opencontainers.image.version     = "${project.version}-2022-05-12" \
      org.opencontainers.image.description = "gICS - a Generic Informed Consent Service" \
      org.opencontainers.image.license     = "AGPLv3"

# copy files
COPY ["deployments/*ar",                   "${ENTRY_WILDFLY_DEPLOYS}/"]
COPY ["jboss/*",                           "${ENTRY_WILDFLY_CLI}/"]
COPY ["sqls/*",                            "${ENTRY_MYSQL_INITFILES}/"]
COPY ["jmeter/*",                          "${ENTRY_JMETER_TESTS}/"]
COPY ["LICENSE.txt",                       "./"]

# create mysql-config and manipulate run.sh
RUN { \
        echo "[mysqld]"; \
        echo "innodb_buffer_pool_size=1G"; \
        echo "sort_buffer_size=8M"; \
        echo "join_buffer_size=512M"; \
    } > ${MYSQL_HOME}/my.cnf && \
    \
    NEW_LINES="# start mysql and wildfly only if start jmeter in local-mode (default: JMETER_MODE=local)\n" && \
    NEW_LINES="${NEW_LINES}if [ \${JMETER_MODE} = 'local' ]; then" && \
    sed -i "s/# befor mysql/${NEW_LINES}\n\0/" run.sh && \
    \
    NEW_LINES="fi" && \
	sed -i "s/# after wildfly/\0\n${NEW_LINES}/" run.sh

USER root
RUN chown -R ${USER}:${USER} ${ENTRY_WILDFLY_DEPLOYS} ${ENTRY_WILDFLY_CLI} ${ENTRY_MYSQL_INITFILES} ${ENTRY_JMETER_TESTS} LICENSE.txt
USER ${USER}
