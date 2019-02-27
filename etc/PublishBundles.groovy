#!/usr/bin/env groovy

// the label is unique and identifies the pod descriptor and its resulting pods
// without this, the agent could be using a pod created from a different descriptor
def label = "glassfish-ci-${UUID.randomUUID().toString()}"
podTemplate(label: label, yaml: """
apiVersion: v1
kind: Pod
spec:
  securityContext:
    runAsUser: 1000100000
  containers:
  - name: glassfish-ci
    image: ee4jglassfish/ci:jdk-8.181
    args:
    - cat
    tty: true
    imagePullPolicy: Always
    resources:
      limits:
        memory: "7Gi"
        cpu: "3"
"""
) {
  node (label) {
    def CREDENTIALS_ID='902aa4fe-7457-47e3-b3c4-de5a4c33b9c5'
    container('glassfish-ci') {
      sshagent([CREDENTIALS_ID]) {
          sh '''
            set -e

            SSH_OPTS="-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"
            SSH_HOST="genie.glassfish@projects-storage.eclipse.org"
            STAGING_PATH="/home/data/httpd/download.eclipse.org/glassfish/"

            # Print the number of colons in a string
            # arg1 - string
            numColons(){
              local str="${1}"
              local colons="${str//[^:]}"
              echo "${#colons}"
            }

            # Download a bundle from Maven Central
            # arg1 - gav (groupID:artifactID:version[:classifier]:type)
            downloadBundle(){
              local gav=${1}
              local numC=`numColons "${gav}"`
              if [ "${numC}" != "3" ] && [ "${numC}" != "4" ] ; then
                echo "Bad GAV format: ${gav}"
                return 1
              fi
              local groupId=`echo ${gav} | cut -d ':' -f1`
              local artifactId=`echo ${gav} | cut -d ':' -f2`
              local version=`echo ${gav} | cut -d ':' -f3`
              local fname="${artifactId}-${version}"
              if [ "${numC}" = "3" ] ; then
                fname="${fname}.`echo ${gav} | cut -d ':' -f4`"
              else
                fname="${fname}-`echo ${gav} | cut -d ':' -f4`.`echo ${gav} | cut -d ':' -f5`"
              fi
              curl "https://repo1.maven.org/maven2/${groupId//.//}/${artifactId}/${version}/${fname}" > bundles/${fname}
            }

            # download all given bundles from Maven Central
            rm -rf bundles && mkdir -p bundles
            IFS=,
            for gav in ${BUNDLES_GAV} ; do
              downloadBundle "${gav}"
            done

            # upload them to download.eclipse.org
            eval "ssh ${SSH_OPTS} ${SSH_HOST} mkdir -p ${STAGING_PATH}"
            eval "scp ${SSH_OPTS} -r bundles/* ${SSH_HOST}:${STAGING_PATH}"
          '''
      }
    }
  }
}