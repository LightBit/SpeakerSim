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
OPENJDK_WINDOWS_PATH=https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u292-b10
OPENJDK_WINDOWS_FILE=OpenJDK8U-jre_x86-32_windows_hotspot_8u292b10.zip
if [ ! -d ./jre ]; then
	if [ ! -f $OPENJDK_WINDOWS_FILE ]; then
		wget $OPENJDK_WINDOWS_PATH/$OPENJDK_WINDOWS_FILE
	fi
	
	unzip $OPENJDK_WINDOWS_FILE
	mv ./jdk8u292-b10-jre ./jre
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

