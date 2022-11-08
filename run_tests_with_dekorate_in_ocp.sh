#!/usr/bin/env bash

oc create -f .openshiftio/resource.configmap.yaml

# Run Tests
eval "./mvnw clean verify -Popenshift,openshift-it $@"
