# jadd

[ ![Maven Central](https://img.shields.io/maven-central/v/ru.d10xa/jadd_2.13?color=green)](https://repo1.maven.org/maven2/ru/d10xa/jadd_2.13/)
[ ![Travis](https://img.shields.io/travis/d10xa/jadd.svg)](https://travis-ci.com/d10xa/jadd)
[ ![codecov](https://codecov.io/gh/d10xa/jadd/branch/master/graph/badge.svg?token=FNzOXeMeWG)](https://codecov.io/gh/d10xa/jadd)
[ ![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)


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

    coursier install jadd --channel https://git.io/JvV0g

## build and run from sources

    sbt publishLocal
      [info]  published ivy to ~/.ivy2/local/ru.d10xa/jadd-cli_2.13/0.1.27-RC3+133-f448e33d+20230726-0002/ivys/ivy.xml
    coursier launch ru.d10xa:jadd_2.13:0.1.27-RC3 -- show

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
