node("launchpad-maven") {
  checkout scm
  stage("Test") {
    sh "mvn test"
  }
  stage("Prepare") {
    sh "oc policy add-role-to-user view -z default"
  }
  stage("Install ConfigMap") {
    sh "if ! oc get configmap app-config -o yaml | grep application.yml; then oc create configmap app-config --from-file=application.yml; fi"
  }
  stage("Deploy") {
    sh "mvn fabric8:deploy -Popenshift -DskipTests"
  }
}
