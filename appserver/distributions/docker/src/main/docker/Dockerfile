FROM eclipse-temurin:17-jdk

ADD glassfish /opt

EXPOSE 4848 9009 8080 8181 7676 8686 3700 3820 3920 6666

# You should use own credentials and own files! These are just defaults.
ARG PATH_GF_PASSWORD_FILE_FOR_CHANGE=/password-change.txt
ENV PATH_GF_HOME=/opt/glassfish7
ENV AS_USER=admin \
    AS_ADMIN_PASSWORD=admin \
    AS_PASSWORD_FILE=/password.txt \
    AS_TRACE=false \
    AS_TRACE_LOGGING=false \
    AS_TRACE_BOOTSTRAP=false \
    AS_DEBUG_PORT_ENABLED=false \
    AS_STOP_TIMEOUT=9500 \
    PATH_GF_BIN=${PATH_GF_HOME}/bin \
    PATH_GF_SERVER_LOG="${PATH_GF_HOME}/glassfish/domains/domain1/logs/server.log"
ENV PATH="${PATH_GF_BIN}:${PATH}"

RUN true \
    && set -x \
    && mkdir -p "${PATH_GF_HOME}" \
    && useradd -l -u 1000 -d "${PATH_GF_HOME}" -s /bin/bash glassfish \
    && echo glassfish:glassfish | chpasswd \
    && echo "Generating password file at ${AS_PASSWORD_FILE} ..." \
    && echo "AS_ADMIN_PASSWORD=${AS_ADMIN_PASSWORD}" > "${AS_PASSWORD_FILE}" \
    && echo "AS_ADMIN_PASSWORD=" > "${PATH_GF_PASSWORD_FILE_FOR_CHANGE}" \
    && echo "AS_ADMIN_NEWPASSWORD=${AS_ADMIN_PASSWORD}" >> "${PATH_GF_PASSWORD_FILE_FOR_CHANGE}" \
    && echo "" >> "${PATH_GF_PASSWORD_FILE_FOR_CHANGE}" \
    && env | sort \
    && asadmin start-domain \
    && asadmin --user ${AS_USER} --passwordfile ${PATH_GF_PASSWORD_FILE_FOR_CHANGE} change-admin-password \
    && asadmin stop-domain --kill \
    && asadmin start-domain \
    && asadmin --user ${AS_USER} --passwordfile ${AS_PASSWORD_FILE} set-log-attributes org.glassfish.main.jul.handler.GlassFishLogHandler.enabled=false \
    && asadmin --user ${AS_USER} --passwordfile ${AS_PASSWORD_FILE} set-log-attributes org.glassfish.main.jul.handler.SimpleLogHandler.level=FINEST \
    && asadmin --user ${AS_USER} --passwordfile ${AS_PASSWORD_FILE} enable-secure-admin \
    && asadmin --user ${AS_USER} --passwordfile ${AS_PASSWORD_FILE} stop-domain \
    && rm -f ${PATH_GF_SERVER_LOG} ${PATH_GF_PASSWORD_FILE_FOR_CHANGE} \
    && chown -R glassfish:glassfish "${PATH_GF_HOME}" \
    && ls -la ${PATH_GF_BIN} \
    && echo "Installation was successful."
USER glassfish
WORKDIR ${PATH_GF_HOME}
ENTRYPOINT ["run-glassfish.sh"]
