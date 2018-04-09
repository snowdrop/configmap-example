
setupEnvironmentPre(env) {
  sh "if ! oc get -n ${env} configmap app-config -o yaml | grep resource.configmap.yaml; then oc create -n ${env} configmap app-config --from-file=.openshiftio/resource.configmap.yaml; fi"
}

setupEnvironmentPost(env) {
}

return this

