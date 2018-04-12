# jadd [![Build Status](https://travis-ci.org/d10xa/jadd.svg?branch=master)](https://travis-ci.org/d10xa/jadd)

Tool for adding dependencies to gradle/maven/sbt build files

## usage

    jadd install logback-classic gson commons-io io.grpc:grpc-protobuf
    jadd i mongodb h2 postgresql mysql rest-assured
    
## commands

install
search
help

## command shortcuts 

i - install
s - search


## installation (macOS)

    brew install d10xa/jadd/jadd
    
## update version (macOS)

    brew upgrade jadd

## installation (other OS)

    git clone https://github.com/d10xa/jadd.git ~/.jadd_snapshot
    cd ~/.jadd_snapshot
    sbt assembly

add alias to ~/.profile or ~/.bashrc or ~/.zshrc

    alias jadd="java -jar ~/.jadd_snapshot/target/scala-2.12/jadd.jar"

update snapshot

    cd ~/.jadd_snapshot
    git pull
    sbt assembly
    

## examples

### maven

    mvn archetype:generate -DgroupId=com.example -DartifactId=example-mvn -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
    cd example-mvn
    jadd install logback-classic

### gradle

    mkdir example-gradle
    cd example-gradle
    gradle init --type java-application
    jadd install mysql

### sbt

    sbt new scala/scala-seed.g8
    # name [Scala Seed Project]: example-sbt
    cd example-sbt
    jadd install akka-http
