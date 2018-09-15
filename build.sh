#!/bin/sh

ant

rm -rf ./SpeakerSim.jar
rm -rf ./build/*

mkdir ./build
cd ./build

jar xf ../dist/lib/minimal-json-0.9.5.jar
jar xf ../dist/lib/jfreechart-1.5.0.jar

jar xf ../dist/SpeakerSim.jar

echo "Manifest-Version: 1.0" > ./META-INF/MANIFEST.MF
echo "Main-Class: SpeakerSim.GUI.Main" >> ./META-INF/MANIFEST.MF

jar cfm0 ../SpeakerSim.jar ./META-INF/MANIFEST.MF ./*
#pack200 -rG ../SpeakerSim.jar

cd ../

#convert -density 384 -background transparent graphics/SpeakerSim.svg -define icon:auto-resize -colors 256 SpeakerSim.ico
makensis ./SpeakerSim.nsi
makensis ./SpeakerSimSetup.nsi

# Windows portable (.zip)
cd ./build
rm -rf ./*
mkdir ./SpeakerSim
cp ../SpeakerSim.jar ./SpeakerSim/
cp ../SpeakerSim.exe ./SpeakerSim/
cp -r ../jre ./SpeakerSim/
7za a -tzip -mx=9 ../SpeakerSim.zip

# Compress JAR
rm -rf ./*
jar xf ../SpeakerSim.jar
rm ../SpeakerSim.jar
7za a -tzip -mx=9 ../SpeakerSim.jar

cd ../
rm -rf ./build
