# ConfigMap Spring Boot Example

https://appdev.openshift.io/docs/spring-boot-runtime.html#mission-configmap-spring-boot

## Deploying application on OpenShift using Dekorate:

```
mvn clean verify -Popenshift -Ddekorate.push=true
```

## Running Tests on OpenShift using Dekorate:

```
oc create -f .openshiftio/resource.configmap.yaml
sh run_tests_with_dekorate.sh
```

## Running Tests on OpenShift using S2i from Source:

```
oc create -f .openshiftio/resource.configmap.yaml
oc create -f .openshiftio/application.yaml
oc new-app --template=rest-http -p SOURCE_REPOSITORY_URL="https://github.com/snowdrop/configmap-example" -p SOURCE_REPOSITORY_REF=sb-2.4.x

sleep 30 # needed in order to bypass the 'Pending' state
# wait for the app to stand up
timeout 300s bash -c 'while [[ $(oc get pod -o json | jq  ".items[] | select(.metadata.name | contains(\"build\"))  | .status  " | jq -rs "sort_by(.startTme) | last | .phase") == "Running" ]]; do sleep 20; done; echo ""'

# launch the tests without deploying the application
sh run_tests_with_s2i.sh
```

## Running Tests on Kubernetes with External Registry:

```
kubectl create -f .github/configmap.yaml
mvn clean verify -Pkubernetes,kubernetes-it -Ddekorate.docker.registry=<url to your registry, example: quay.io> -Ddekorate.push=true
```
