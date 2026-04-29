#!/bin/sh

# Make icon with ImageMagick
#convert -background transparent resources/SpeakerSim.png -define icon:auto-resize=16,24,32,48,64,72,96,128 SpeakerSim.ico

# Build using Maven
mvn package
#mvn package -Dmaven.test.skip=true

# Move jar
mv ./target/SpeakerSim-jar-with-dependencies.jar ./SpeakerSim.jar

# Zip jar
7za a -tzip -mx=9 SpeakerSim.zip SpeakerSim.jar

# Download JRE for Windows 32-bit
OPENJDK_WINDOWS_URL=https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.17%2B10/OpenJDK17U-jre_x86-32_windows_hotspot_17.0.17_10.zip
OPENJDK_WINDOWS_FILE=OpenJDK17U-jre_x86-32_windows_hotspot_17.0.17_10.zip
if [ ! -d ./jre ]; then
	if [ ! -f $OPENJDK_WINDOWS_FILE ]; then
		wget $OPENJDK_WINDOWS_URL
	fi
	
	unzip $OPENJDK_WINDOWS_FILE
	mv ./jdk-17.0.17+10-jre ./jre
fi

# Windows setup
makensis ./SpeakerSimSetup.nsi

# Recompress jar
rm -rf ./jar
unzip ./SpeakerSim.jar -d ./jar
rm ./SpeakerSim.jar
cd ./jar
7za a -tzip -mx=9 ../SpeakerSim.jar
cd ../
rm -rf ./jar

