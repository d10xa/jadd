# jadd

[ ![GitHub release](https://img.shields.io/github/release/d10xa/jadd.svg)](https://github.com/d10xa/jadd/releases)
[ ![Travis](https://img.shields.io/travis/d10xa/jadd.svg)](https://travis-ci.com/d10xa/jadd)
[ ![Coveralls github](https://img.shields.io/coveralls/github/d10xa/jadd.svg)](https://coveralls.io/github/d10xa/jadd)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)


Tool for adding dependencies to gradle/maven/sbt build files

[![jadd.gif](data/jadd.gif)](https://github.com/d10xa/jadd#usage "d10xa/jadd")

## REPL!

Just run `jadd` without arguments and enjoy tab completion!

## usage

    jadd i logback-classic postgresql gson commons-io io.grpc:grpc-protobuf
    jadd i -r jrequirements.txt

## commands

- `install` (shortcut `i`) add dependency to build file

- `search` (shortcut `s`) print dependency to console

- `show` show artifacts from build file

- `help`

## installation


    TODO

## examples

### maven

    mvn archetype:generate -DgroupId=com.example -DartifactId=example-mvn -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
    cd example-mvn
    jadd i logback-classic

### gradle

    mkdir example-gradle
    cd example-gradle
    gradle init --type java-application
    jadd i mysql

### sbt

    sbt new scala/scala-seed.g8
    # name [Scala Seed Project]: example-sbt
    cd example-sbt
    jadd i akka-http
