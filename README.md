# ConfigMap Spring Boot Example

https://appdev.openshift.io/docs/spring-boot-runtime.html#mission-configmap-spring-boot

## Table of Contents

* [ConfigMap Spring Boot Example](#configmap-spring-boot-example)
    * [Prerequisites](#prerequisites)
    * [Deploying application on OpenShift using Dekorate](#deploying-application-on-openshift-using-dekorate)
    * [Deploying application on OpenShift using Helm](#deploying-application-on-openshift-using-helm)
    * [Deploying application on Kubernetes using Helm](#deploying-application-on-kubernetes-using-helm)
    * [Running Tests on OpenShift using Dekorate](#running-tests-on-openshift-using-dekorate)
    * [Running Tests on OpenShift using S2i from Source](#running-tests-on-openshift-using-s2i-from-source)
    * [Running Tests on OpenShift using Helm](#running-tests-on-openshift-using-helm)
    * [Running Tests on Kubernetes with Helm](#running-tests-on-kubernetes-using-helm)

## Prerequisites

- JDK 11+ installed with JAVA_HOME configured appropriately

## Deploying application on OpenShift using Dekorate

Before deploying the application using Dekorate, make sure you have deployed the configmap first:

```
oc create -f .openshiftio/resource.configmap.yaml
```

Now, we can deploy the application using Dekorate:

```
mvn clean verify -Popenshift -Ddekorate.deploy=true
```

## Deploying application on OpenShift using Helm

First, make sure you have installed [the Helm command line](https://helm.sh/docs/intro/install/) and connected/logged to a kubernetes cluster.

Then, you need to install the example by doing:

```
helm install configmap ./helm --set app.route.expose=true --set app.s2i.source.repo=https://github.com/snowdrop/configmap-example --set app.s2i.source.ref=<branch-to-use>
```

**note**: Replace `<branch-to-use>` with one branch from `https://github.com/snowdrop/configmap-example/branches/all`.

And to uninstall the chart, execute:

```
helm uninstall configmap
```

## Deploying application on Kubernetes using Helm

Requirements:
- Have installed [the Helm command line](https://helm.sh/docs/intro/install/)
- Have connected/logged to a kubernetes cluster

You need to install the example by doing:

```
helm install configmap ./helm --set app.ingress.host=<your k8s domain>
```

And to uninstall the chart, execute:

```
helm uninstall configmap
```

## Running Tests on OpenShift using Dekorate

```
./run_tests_with_dekorate_in_ocp.sh
```

## Running Tests on OpenShift using S2i from Source

```
./run_tests_with_s2i.sh
```

This script can take 2 parameters referring to the repository and the branch to use to source the images from.

```bash
./run_tests_with_s2i.sh "https://github.com/snowdrop/configmap-example" branch-to-test
```

## Running Tests on OpenShift using Helm

```
./run_tests_with_helm_in_ocp.sh
```

This script can take 2 parameters referring to the repository and the branch to use to source the images from.

```bash
./run_tests_with_helm_in_ocp.sh "https://github.com/snowdrop/configmap-example" branch-to-test
```

## Running Tests on Kubernetes using Helm

First, you need to create the k8s namespace:

```
kubectl create namespace <the k8s namespace>
```

Then, run the tests by specifying the container registry and the kubernetes namespace:
```
./run_tests_with_helm_in_k8s.sh <your container registry: for example "quay.io/user"> <the k8s namespace>
```

For example:

```
./run_tests_with_helm_in_k8s.sh "quay.io/user" "myNamespace"
```
