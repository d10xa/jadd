#!/usr/bin/env bash

export JADD_ZIP_FILE=$(find . -name "*jadd-*.zip")
export JADD_VERSION=$(cat VERSION)
sbt clean universal:packageBin
sh install.sh
