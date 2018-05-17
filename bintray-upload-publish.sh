VERSION_NAME=$(cat VERSION)
FILE_EXT=target/universal/jadd-${VERSION_NAME}.zip
FILE_TARGET_PATH=jadd-${VERSION_NAME}.zip
curl -T ${FILE_EXT} -ud10xa:${BINTRAY_API_KEY} https://api.bintray.com/content/d10xa/bin/jadd/${VERSION_NAME}/${FILE_TARGET_PATH}
curl -X POST -ud10xa:${BINTRAY_API_KEY} https://api.bintray.com/content/d10xa/bin/jadd/${VERSION_NAME}/publish
gsha256sum target/universal/jadd-${VERSION_NAME}.zip
