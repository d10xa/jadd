#!/usr/bin/env sh

PROJECT_DIR="$(pwd)"
WORKDIR="${PROJECT_DIR}/.antlr-tmp"
ANTLR_JAR="antlr-4.7.2-complete.jar"
ANTLR_JAR_PATH="${WORKDIR}/jar/${ANTLR_JAR}"
ANTLR_OUTPUT="${PROJECT_DIR}/src/main/scala/ru/d10xa/jadd/generated/antlr"

if ! [ -s  "$ANTLR_JAR_PATH" ]; then
    mkdir -p "$WORKDIR/jar"
    curl -o "$ANTLR_JAR_PATH" "https://www.antlr.org/download/$ANTLR_JAR"
fi

cd "${PROJECT_DIR}/src/main/antlr"
java -jar "${ANTLR_JAR_PATH}" -visitor -o "${ANTLR_OUTPUT}" -package ru.d10xa.jadd.generated.antlr SbtDependencies.g4
"amm" "${PROJECT_DIR}/removeJavadoc.sc" --dir "${ANTLR_OUTPUT}"
