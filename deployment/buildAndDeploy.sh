#!/bin/bash

./gradlew --daemon clean build -x :keanu-python:build uploadArchives -PnexusUser=$NEXUS_USER -PnexusPassword=$NEXUS_PASSWORD -Psigning.keyId=$SIGNING_KEY_ID -Psigning.password=$SIGNING_PASSWORD -Psigning.secretKeyRingFile=../deployment/secret-keys-keanu.gpg --info --stacktrace
