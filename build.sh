#!/bin/bash

version=`cat VERSION`

# Compile
compileScript="javac -cp \"lib/*\" -sourcepath src/main/java/ -d target/classes/ src/main/java/se/bjornblomqvist/*.java"
echo $compileScript
eval $compileScript

# Copy jar installer classes
unzip -o -d target/classes/ lib/jarinstaller-0.2.0.jar -x META-INF/MANIFEST.MF

# Build jar
jarScript="jar cfe target/jas-$version.jar se.bjornblomqvist.Jas -C target/classes/ se -C target/classes/ jarinstaller -C target/classes/ dependencies"
echo $jarScript
eval $jarScript