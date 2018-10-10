FROM maven:3.5.4-jdk-8

RUN apt-get update && \
    apt-get install -y apt-utils ant unzip tar wget zip sendmail && \
    # install takari extensions for
    # process/thread safe access to the local repository
    curl -O http://repo1.maven.org/maven2/io/takari/aether/takari-local-repository/0.11.2/takari-local-repository-0.11.2.jar && \
    mv takari-local-repository-0.11.2.jar ${MAVEN_HOME}/lib/ext/ && \
    curl -O http://repo1.maven.org/maven2/io/takari/takari-filemanager/0.8.3/takari-filemanager-0.8.3.jar && \
    mv takari-filemanager-0.8.3.jar ${MAVEN_HOME}/lib/ext/ && \
    # custom user
    useradd -l -r -d /home/jenkins -u 1000100000 -g root -s /bin/bash jenkins && \
    mkdir -p /home/jenkins/.m2/repository/org/glassfish/main && \
    chmod 777 -R /home/jenkins/.m2/repository/org/glassfish/main && \
    chown -R jenkins:root /home/jenkins

COPY ./entrypoint.sh /etc/entrypoint.sh
RUN chmod +x /etc/entrypoint.sh

ENV M2_HOME /usr/share/maven
ENV ANT_HOME /usr/share/ant
ENV JAVA_TOOL_OPTIONS "-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xmx2Gm "

ENV HOME /home/jenkins
WORKDIR /home/jenkins
USER jenkins

ENTRYPOINT [ "/etc/entrypoint.sh" ]