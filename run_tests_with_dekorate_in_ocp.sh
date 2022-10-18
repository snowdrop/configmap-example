#!/usr/bin/env bash

oc create -f .openshiftio/resource.configmap.yaml

# Run Tests
./mvnw -s .github/mvn-settings.xml clean verify -Popenshift,openshift-it
