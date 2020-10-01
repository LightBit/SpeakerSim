#!/bin/sh

mvn clean

rm -rf ./build
rm -rf ./bin
rm -rf ./dist
rm -rf ./test
rm -rf ./jre
rm -rf ./jar

rm -rf ./*.jar
rm -rf ./*.exe
rm -rf ./*.zip

