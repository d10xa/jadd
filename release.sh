sbt clean
sbt universal:packageBin
sh bintray-upload-publish.sh
