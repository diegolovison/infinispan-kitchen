#!/usr/bin/env bash

oc new-project foo
oc delete all --all
oc create configmap custom-clustered --from-file=custom-clustered.xml
oc create configmap keystore-server --from-file=keystore_server.jks
oc create -f ispn-template.yaml