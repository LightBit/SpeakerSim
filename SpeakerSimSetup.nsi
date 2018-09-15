; Written in 2017 by Gregor Pintar <grpintar@gmail.com>
;
; To the extent possible under law, the author(s) have dedicated
; all copyright and related and neighboring rights to this software
; to the public domain worldwide.
;
; This software is distributed without any warranty.
;
; You should have received a copy of the CC0 Public Domain Dedication.
; If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

Name "SpeakerSim"
Caption "SpeakerSim Setup"
OutFile "SpeakerSimSetup.exe"

VIAddVersionKey "ProductName" "SpeakerSim"
VIAddVersionKey "FileDescription" "SpeakerSim"
VIAddVersionKey "LegalCopyright" ""
VIAddVersionKey "FileVersion" "1.0.0"
VIProductVersion "1.0.0.0"

!define JAR "SpeakerSim.jar"
!define PRODUCT_NAME "SpeakerSim"
!define INSTALLSIZE 3000

RequestExecutionLevel user
AutoCloseWindow true
InstallDir "$APPDATA\SpeakerSim"
XPStyle on
SetCompressor /SOLID zlib

!include "MUI2.nsh"

;!define MUI_ABORTWARNING

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_INSTFILES

;!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

Section
  SetOutPath $INSTDIR
  
  File "SpeakerSim.jar"
  File "SpeakerSim.exe"

  WriteUninstaller "$INSTDIR\uninstall.exe"

  ; Shortcuts
  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}.lnk" "$INSTDIR\SpeakerSim.exe" "" "$INSTDIR\SpeakerSim.exe"
  CreateShortCut "$DESKTOP\${PRODUCT_NAME}.lnk" "$INSTDIR\SpeakerSim.exe" "" "$INSTDIR\SpeakerSim.exe"

  ; Registry information for add/remove programs
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "DisplayName" "${PRODUCT_NAME}"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
  ;WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "InstallLocation" "$\"$INSTDIR$\""
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "DisplayIcon" "$\"$INSTDIR\SpeakerSim.exe$\""
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "Publisher" "Gregor Pintar"
  ;WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "HelpLink" "$\"${HELPURL}$\""
  ;WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "URLUpdateInfo" "$\"${UPDATEURL}$\""
  ;WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "URLInfoAbout" "$\"${ABOUTURL}$\""
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "DisplayVersion" "1.0.0"
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "VersionMajor" 1
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "VersionMinor" 0
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "NoModify" 1
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "NoRepair" 1
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "EstimatedSize" ${INSTALLSIZE}
  
  ; Register file association
  WriteRegStr HKCU "Software\Classes\.ssim" "" "${PRODUCT_NAME}"
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}" "" "${PRODUCT_NAME}"
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}\shell" "" "open"
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}\DefaultIcon" "" "$INSTDIR\SpeakerSim.exe"
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}\shell\open\command" "" '"$INSTDIR\SpeakerSim.exe" "%1"'
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}\shell\edit" "" "Open in ${PRODUCT_NAME}"
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}\shell\edit\command" "" '"$INSTDIR\SpeakerSim.exe" "%1"'

  Exec "$INSTDIR\SpeakerSim.exe"
SectionEnd

Section "Uninstall"
  ; Remove shortcuts
  Delete "$SMPROGRAMS\${PRODUCT_NAME}.lnk"
  Delete "$DESKTOP\${PRODUCT_NAME}.lnk"
  
  ; Unregister file association
  DeleteRegKey HKCU "Software\Classes\${PRODUCT_NAME}"
  DeleteRegKey HKCU "Software\Classes\.ssim"
  
  ; Delete files
  Delete $INSTDIR\SpeakerSim.jar
  Delete $INSTDIR\SpeakerSim.exe
  Delete $INSTDIR\uninstall.exe
  RmDir $INSTDIR
  
  ; Remove uninstaller information from the registry
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
SectionEnd
