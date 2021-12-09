# ConfigMap Spring Boot Example

https://appdev.openshift.io/docs/spring-boot-runtime.html#mission-configmap-spring-boot

## Table of Contents

* [ConfigMap Spring Boot Example](#configmap-spring-boot-example)
    * [Deploying application on OpenShift using Dekorate](#deploying-application-on-openshift-using-dekorate)
    * [Running Tests on OpenShift using Dekorate](#running-tests-on-openshift-using-dekorate)
    * [Running Tests on OpenShift using S2i from Source](#running-tests-on-openshift-using-s2i-from-source)
    * [Running Tests on Kubernetes with External Registry](#running-tests-on-kubernetes-with-external-registry)

## Deploying application on OpenShift using Dekorate

```
mvn clean verify -Popenshift -Ddekorate.push=true
```

## Running Tests on OpenShift using Dekorate

```
./run_tests_with_dekorate.sh
```

## Running Tests on OpenShift using S2i from Source

```
./run_tests_with_s2i.sh
```

This script can take 2 parameters referring to the repository and the branch to use to source the images from.

```bash
./run_tests_with_s2i.sh "https://github.com/snowdrop/configmap-example" branch-to-test
```

## Running Tests on Kubernetes with External Registry

```
kubectl create -f .github/configmap.yaml
mvn clean verify -Pkubernetes,kubernetes-it -Ddekorate.docker.registry=<url to your registry, example: quay.io> -Ddekorate.push=true
```
