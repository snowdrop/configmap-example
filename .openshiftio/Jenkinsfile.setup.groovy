
def setupEnvironmentPre(env) {
  //sh "oc policy -n ${env} add-role-to-user view -z default"
  sh "if ! oc get -n ${env} configmap app-config -o yaml | grep application.yml; then oc create -n ${env} configmap app-config --from-file=application.yml; fi"
}

def setupEnvironmentPost(env) {
}

return this

