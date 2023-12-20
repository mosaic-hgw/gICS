FROM ${docker.baseImage}

# ###license-information-start###
# gICS - a Generic Informed Consent Service
# __
# Copyright (C) 2013 - ${build.year} Independent Trusted Third Party of the University Medicine Greifswald
# 							kontakt-ths@uni-greifswald.de
#
# 							concept and implementation
# 							l.geidel, c.hampf
# 							web client
# 							a.blumentritt, m.bialke
# 							fhir-api
# 							m.bialke
# 							docker
# 							r. schuldt
#
# 							Selected functionalities
# 							of gICS were developed as
# 							part of the MAGIC Project
# 							(funded by the
# 							DFG HO 1937/5-1).
#
# 							please cite our publications
# 							https://doi.org/10.1186/s12967-020-02457-y
# 							http://dx.doi.org/10.3414/ME14-01-0133
# 							http://dx.doi.org/10.1186/s12967-015-0545-6
# 							http://dx.doi.org/10.3205/17gmds146
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

# tool-defaults
ENV ${project.upperName}_VERSION                           ${project.version}
ENV ${project.upperName}_AUTH_MODE                         none

# keycloak-defaults
ENV KEYCLOAK_SSL_REQUIRED                  all
ENV KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS    false
ENV KEYCLOAK_CONFIDENTIAL_PORT             8443

# annotations
LABEL maintainer                           = "ronny.schuldt@uni-greifswald.de" \
      org.opencontainers.image.authors     = "university-medicine greifswald" \
      org.opencontainers.image.source      = "https://github.com/mosaic-hgw/${project.parent.name}" \
      org.opencontainers.image.vendor      = "uni-greifswald.de" \
      org.opencontainers.image.title       = "${project.parent.name}" \
      org.opencontainers.image.version     = "${project.version}" \
      org.opencontainers.image.description = "gICS - a Generic Informed Consent Service" \
      org.opencontainers.image.license     = "AGPLv3"

# copy files
COPY ["deployments/*ar",                   "${ENTRY_WILDFLY_DEPLOYS}/"]
COPY ["jboss/*",                           "${ENTRY_WILDFLY_CLI}/"]
COPY ["LICENSE.txt",                       "./"]

USER root
RUN chown -R ${USER}:${USER} ${ENTRY_WILDFLY_DEPLOYS} ${ENTRY_WILDFLY_CLI} LICENSE.txt
USER ${USER}
