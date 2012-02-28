#!/bin/bash

jarsigner -verbose -keystore ~/.android/xanthanov.store FunRunApp-unsigned.apk android_release
zipalign -v 4 FunRunApp-release.apk FunRunApp-aligned.apk

