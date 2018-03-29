# jadd [![Build Status](https://travis-ci.org/d10xa/jadd.svg?branch=master)](https://travis-ci.org/d10xa/jadd)

Tool for adding dependencies to gradle/maven/sbt build files

## install snapshot

    git clone https://github.com/d10xa/jadd.git ~/.jadd_snapshot
    cd ~/.jadd_snapshot
    sbt assembly

add alias to ~/.profile or ~/.bashrc or ~/.zshrc

    alias jadd="java -jar ~/.jadd_snapshot/target/scala-2.12/jadd.jar"

## update snapshot

    cd ~/.jadd_snapshot
    git pull
    sbt assembly
    
## usage
    
    jadd logback-classic akka-http io.grpc:grpc-protobuf
