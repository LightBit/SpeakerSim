#!/bin/sh

# Make icon with ImageMagick
#convert -background transparent resources/SpeakerSim.png -define icon:auto-resize=16,24,32,48,64,72,96,128 SpeakerSim.ico

# Build using Maven
mvn package

# Move final jar
mv ./target/SpeakerSim-jar-with-dependencies.jar ./SpeakerSim.jar

# Zip jar
7za a -tzip -mx=9 SpeakerSim.zip SpeakerSim.jar

# Recompress jar
#rm -rf ./target/jar
#unzip ./target/SpeakerSim-jar-with-dependencies.jar -d ./target/jar
#cd ./target/jar
#7za a -tzip -mx=9 ../../SpeakerSim.jar
#cd ../../

# Windows setup
makensis ./SpeakerSimSetup.nsi

# Windows portable (.zip)
#OPENJDK_WINDOWS_FILE=OpenJDK8U-jre_x86-32_windows_hotspot_8u202b08.zip
#mkdir -r ./build/SpeakerSim
#cd ./build/SpeakerSim
#
#if [ ! -d ./jre ]; then
#	if [ ! -f $OPENJDK_WINDOWS_FILE ]; then
#		wget https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u202-b08/$OPENJDK_WINDOWS_FILE
#	fi
#	
#	unzip $OPENJDK_WINDOWS_FILE
#	mv ./jdk8u202-b08-jre ./jre
#fi
#
#cp ../../SpeakerSim.jar ./
#cp ../../SpeakerSim.exe ./
#7za a -tzip -mx=9 ../SpeakerSim.zip
#cd ../../
