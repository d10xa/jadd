#!/usr/bin/env bash

if [ -z "$JADD_HOME" ]; then
    JADD_HOME="$HOME/.jadd"
fi

if [ -z $(which curl) ]; then
    echo " Please install curl on your system"
    exit 0
fi

if [ -z $(which unzip) ]; then
    echo " Please install unzip on your system"
    exit 0
fi

if [ -z "$JADD_VERSION" ]; then
    JADD_VERSION=$(curl -s https://raw.githubusercontent.com/d10xa/jadd/master/VERSION)
fi

jadd_archive_name="jadd-${JADD_VERSION}.zip"

mkdir -p "${JADD_HOME}/zip"

if [ -z "$JADD_ZIP_FILE" ]; then
    JADD_ZIP_FILE="${JADD_HOME}/zip/${jadd_archive_name}"

    if [ -z "$JADD_ZIP_URL" ]; then
        JADD_ZIP_URL="https://bintray.com/d10xa/bin/download_file?file_path="
    fi

    if ! [ -s "${JADD_ZIP_FILE}" ]; then
        JADD_ZIP_REMOTE="${JADD_ZIP_URL}${jadd_archive_name}"
        echo "Download jadd from ${JADD_ZIP_REMOTE}"
        echo $JADD_ZIP_FILE
        curl -L --silent -o "${JADD_ZIP_FILE}" "${JADD_ZIP_REMOTE}"
    fi
fi

mkdir -p "${JADD_HOME}/_tmp"
unzip -q "${JADD_ZIP_FILE}" -d "${JADD_HOME}/_tmp"

if ! [ -s "${JADD_HOME}/_tmp/jadd-${JADD_VERSION}/bin/jadd" ]; then
    echo " installation error"
    exit 0;
fi

if [ -d "${JADD_HOME}/bin/" ]; then rm -r "${JADD_HOME}/bin/"; fi
if [ -d "${JADD_HOME}/lib/" ]; then rm -r "${JADD_HOME}/lib/"; fi

mv "${JADD_HOME}/_tmp/jadd-${JADD_VERSION}/bin" "${JADD_HOME}/bin"
mv "${JADD_HOME}/_tmp/jadd-${JADD_VERSION}/lib" "${JADD_HOME}/lib"
rm -r "${JADD_HOME}/_tmp"

${JADD_HOME}/bin/jadd help

echo '------------------------------------------------------'
echo ' Please add to ~/.profile or ~/.bashrc or ~/.zshrc    '
echo '         export PATH=$PATH:$HOME/.jadd/bin            '
echo '------------------------------------------------------'
