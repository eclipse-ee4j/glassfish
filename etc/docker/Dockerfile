FROM centos:latest

ADD ./entrypoint.sh /etc/
ADD ./jdk1.8.0_181 /usr/lib/jvm/jdk8

RUN chmod +x /etc/entrypoint.sh && \
    #
    # install utilities
    #
    yum install -y which unzip tar wget zip sendmail && \
    update-ca-trust && \
    #
    # setup jdk
    #
    cp -Lf /etc/pki/java/cacerts /usr/lib/jvm/jdk8/jre/lib/security/cacerts && \
    ln -s /usr/lib/jvm/jdk8/bin/* /bin/ && \
    #
    # install maven
    #
    curl -O https://archive.apache.org/dist/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.zip && \
    unzip apache-maven-*-bin.zip -d /usr/share && \
    rm apache-maven-*-bin.zip && \
    mv /usr/share/apache-maven-* /usr/share/maven && \
    ln -s /usr/share/maven/bin/mvn /bin/ && \
    #
    # install takari extensions
    #
    curl -O https://repo1.maven.org/maven2/io/takari/aether/takari-local-repository/0.11.2/takari-local-repository-0.11.2.jar && \
    mv takari-local-repository-*.jar /usr/share/maven/lib/ext/ && \
    curl -O https://repo1.maven.org/maven2/io/takari/takari-filemanager/0.8.3/takari-filemanager-0.8.3.jar && \
    mv takari-filemanager-*.jar /usr/share/maven/lib/ext/ && \
    #
    # install ant
    #
    curl -O https://archive.apache.org/dist/ant/binaries/binaries/apache-ant-1.9.4-bin.zip && \
    unzip apache-ant-*-bin.zip -d /usr/share && \
    rm apache-ant-*-bin.zip && \
    mv /usr/share/apache-ant-* /usr/share/ant && \
    ln -s /usr/share/ant/bin/ant /bin/ && \
    #
    # custom user
    #
    useradd -l -r -d /home/jenkins -u 1000100000 -g root -s /bin/bash jenkins && \
    mkdir -p /home/jenkins/.m2/repository/org/glassfish/main && \
    chmod 777 -R /home/jenkins/.m2/repository/org/glassfish/main && \
    chown -R jenkins:root /home/jenkins

ENV JAVA_HOME /usr/lib/jvm/jdk8
ENV MAVEN_HOME /usr/share/maven
ENV M2_HOME /usr/share/maven
ENV ANT_HOME /usr/share/ant
ENV JAVA_TOOL_OPTIONS "-Xmx2G"

ENV HOME /home/jenkins
WORKDIR /home/jenkins
USER jenkins

ENTRYPOINT [ "/etc/entrypoint.sh" ]