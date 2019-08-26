#!/usr/bin/env sh

WORKDIR="$(pwd)/.antlr-tmp"
ANTLR_JAR="antlr-4.7.1-complete.jar"
ANTLR_JAR_PATH="${WORKDIR}/jar/${ANTLR_JAR}"
ANTLR_MODULE="$(pwd)"
ANTLR_OUTPUT="$ANTLR_MODULE/src/main/scala/ru/d10xa/jadd/generated/antlr"

if ! [ -s  "$ANTLR_JAR_PATH" ]; then
    mkdir -p "$WORKDIR/jar"
    curl -o "$ANTLR_JAR_PATH" "https://www.antlr.org/download/$ANTLR_JAR"
fi

cd "$ANTLR_MODULE/src/main/antlr"
java -jar "${ANTLR_JAR_PATH}" -visitor -o "${ANTLR_OUTPUT}" -package ru.d10xa.jadd.generated.antlr SbtDependencies.g4
