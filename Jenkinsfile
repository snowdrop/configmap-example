node("launchpad-maven") {
  checkout scm
  stage("Test") {
    sh "mvn test"
  }
  stage("Prepare") {
    sh "oc policy add-role-to-user view -z default"
  }
  stage("Install ConfigMap") {
    sh "if ! oc get configmap app-config -o yaml | grep application.properties; then oc create configmap app-config --from-file=src/main/etc/application.properties; fi"
  }
  stage("Deploy") {
    sh "mvn fabric8:deploy -Popenshift -DskipTests"
  }
}
