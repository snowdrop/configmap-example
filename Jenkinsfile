node("launchpad-maven") {
  checkout scm
  stage("Test") {
    sh "mvn test"
  }
  stage("Prepare") {
    sh "oc policy add-role-to-user view -z default"
  }
  stage("Deploy") {
    sh "mvn fabric8:deploy -Popenshift -DskipTests"
  }
}
