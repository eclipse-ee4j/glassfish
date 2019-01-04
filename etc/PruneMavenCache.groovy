// the label is unique and identifies the pod descriptor and its resulting pods
// without this, the agent could be using a pod created from a different descriptor
def label = "glassfish-ci-${UUID.randomUUID().toString()}"
podTemplate(label: label, yaml: """
apiVersion: v1
kind: Pod
spec:
  securityContext:
    runAsUser: 1000100000
  volumes:
    - name: maven-repo-shared-storage
      persistentVolumeClaim:
       claimName: glassfish-maven-repo-storage
    - name: maven-repo-local-storage
      emptyDir: {}
    - name: maven-settings
      configMap:
        name: maven-settings.xml
  containers:
  - name: jnlp
    image: jenkins/jnlp-slave:alpine
    imagePullPolicy: IfNotPresent
    volumeMounts:
    env:
      - name: JAVA_TOOL_OPTIONS
        value: -Xmx1G
    resources:
      limits:
        memory: "1Gi"
        cpu: "1"
  - name: glassfish-ci
    image: ee4jglassfish/ci:jdk-8.181
    args:
    - cat
    tty: true
    imagePullPolicy: Always
    volumeMounts:
      - mountPath: "/home/jenkins/.m2/settings.xml"
        subPath: maven-settings.xml
        name: maven-settings
      - mountPath: "/home/jenkins/.m2/repository"
        name: maven-repo-shared-storage
      - mountPath: "/home/jenkins/.m2/repository/org/glassfish/main"
        name: maven-repo-local-storage
    resources:
      limits:
        memory: "7Gi"
        cpu: "3"
"""
) {
  node (label) {
    container('glassfish-ci') {
      sh '''
        # wipe-out the local repo
        if [ "${CLEAN_ALL}" = "true" ] ; then
          rm -rf ${HOME}/.m2/repository/*
          exit 0
        fi

        # purge local repo for specific GAVs
        mvn \
          org.apache.maven.plugins:maven-dependency-plugin:3.1.1:purge-local-repository \
          -DmanualInclude="${INCLUDES}" \
          -DreResolve="false"

        # re-resolve
        IFS=,
        if [ "${RE_RESOLVE}" = "true" ] ; then
          for artifact in ${INCLUDES} ; do
            mvn \
              org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get \
              -Dartifact="${artifact}" \
              -DremoteRepositories="sonatype-nexus-staging::default::https://oss.sonatype.org/content/repositories/staging"
          done
        fi
      '''
    }
  }
}
