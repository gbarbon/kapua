###############################################################################
# Copyright (c) 2018 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech - initial API and implementation
#
###############################################################################

FROM @docker.account@/java-base

COPY maven /

ENV DATASTORE_ADDR es
ENV DATASTORE_PORT 9200
ENV DATASTORE_CLIENT org.eclipse.kapua.service.datastore.client.rest.RestDatastoreClient

ENV SQL_DB_ADDR db
ENV SQL_DB_PORT 3306

ENV SERVICE_BROKER_ADDR failover:(amqp://events-broker:5672)?jms.sendTimeout=1000

USER 0

RUN useradd -u 1002 -g 0 -d '/var/opt/activemq' -s '/sbin/nologin' activemq && \
    chown -R activemq:root /var/opt/activemq /opt/activemq && \
    chmod -R g=u /var/opt/activemq /opt/activemq

RUN yum -y install unzip

RUN curl -L https://www.yourkit.com/download/docker/YourKit-JavaProfiler-2019.8-docker.zip -o /tmp/YourKit-JavaProfiler-2019.8-docker.zip && \
  unzip /tmp/YourKit-JavaProfiler-2019.8-docker.zip -d /usr/local && \
  rm /tmp/YourKit-JavaProfiler-2019.8-docker.zip

ENV ACTIVEMQ_OPTS "-Dcommons.db.schema.update=true \
                   -Dcommons.db.connection.host=\${SQL_DB_ADDR} \
                   -Dcommons.db.connection.port=\${SQL_DB_PORT} \
                   -Ddatastore.elasticsearch.nodes=\${DATASTORE_ADDR} \
                   -Ddatastore.elasticsearch.port=\${DATASTORE_PORT} \
                   -Ddatastore.client.class=\${DATASTORE_CLIENT} \
                   -Dcommons.eventbus.url=\${SERVICE_BROKER_ADDR} \
                   -Dcertificate.jwt.private.key=file:///var/opt/activemq/key.pk8 \
                   -Dcertificate.jwt.certificate=file:///var/opt/activemq/cert.pem \
                   -Dbroker.ip=broker"

EXPOSE 1883 8883 61614 61615 8161 10001

VOLUME /opt/activemq/data

USER 1002

ENTRYPOINT /var/opt/activemq/run-broker
