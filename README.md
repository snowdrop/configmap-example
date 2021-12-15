# ConfigMap Spring Boot Example

https://appdev.openshift.io/docs/spring-boot-runtime.html#mission-configmap-spring-boot

## Table of Contents

* [ConfigMap Spring Boot Example](#configmap-spring-boot-example)
    * [Deploying application on OpenShift using Dekorate](#deploying-application-on-openshift-using-dekorate)
    * [Running Tests on OpenShift using Dekorate](#running-tests-on-openshift-using-dekorate)
    * [Running Tests on OpenShift using S2i from Source](#running-tests-on-openshift-using-s2i-from-source)

## Deploying application on OpenShift using Dekorate

Before deploying the application using Dekorate, make sure you have deployed the configmap first:

```
oc create -f .openshiftio/resource.configmap.yaml
```

Now, we can deploy the application using Dekorate:

```
mvn clean verify -Popenshift -Ddekorate.deploy=true
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
