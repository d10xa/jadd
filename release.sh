git stash
sbt clean
sbt universal:packageBin
git stash apply
sh bintray-upload-publish.sh
