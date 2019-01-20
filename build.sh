#!/bin/bash

# Compile
echo "javac -sourcepath src/main/java/ -d target/classes/ src/main/java/se/bjornblomqvist/Jas.java"
javac -sourcepath src/main/java/ -d target/classes/ src/main/java/se/bjornblomqvist/Jas.java

# Build jar
echo "jar cvfe target/jas.jar se.bjornblomqvist.Jas -C target/classes/ se"
jar cfe target/jas.jar se.bjornblomqvist.Jas -C target/classes/ se

# Install jar
jarinstaller install target/jas.jar
