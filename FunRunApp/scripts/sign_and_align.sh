#!/bin/bash

jarsigner -verbose -keystore ~/.keystore/charles-release-key.keystore -signedjar bin/FunRunnerApp-signed.apk bin/FunRunnerApp-release-unsigned.apk charles_release
zipalign -v 4 FunRunnerApp-release.apk FunRunnerApp-aligned.apk

