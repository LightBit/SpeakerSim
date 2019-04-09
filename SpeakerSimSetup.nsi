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
VIAddVersionKey "FileVersion" "0.0.0.0"
VIProductVersion "0.0.0.0"

!define PRODUCT_NAME "SpeakerSim"

RequestExecutionLevel user
AutoCloseWindow true
InstallDir "$APPDATA\SpeakerSim"
XPStyle on
SetCompressor /SOLID zlib

!include "MUI2.nsh"

!insertmacro MUI_PAGE_LICENSE COPYING
!insertmacro MUI_PAGE_INSTFILES

!define MUI_FINISHPAGE_SHOWREADME ""
!define MUI_FINISHPAGE_SHOWREADME_CHECKED
!define MUI_FINISHPAGE_SHOWREADME_TEXT "Create Desktop shortcut"
!define MUI_FINISHPAGE_SHOWREADME_FUNCTION finish_page_action
!define MUI_FINISHPAGE_RUN "$INSTDIR\SpeakerSim.exe"
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

!insertmacro MUI_LANGUAGE "English"

Section
  SetOutPath "$INSTDIR"
  File "SpeakerSim.jar"
  File "SpeakerSim.exe"
  File /nonfatal /r "jre"
  WriteUninstaller "$INSTDIR\uninstall.exe"

  ; Menu shortcut
  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}.lnk" "$INSTDIR\SpeakerSim.exe" "" "$INSTDIR\SpeakerSim.exe"

  ; Registry information for add/remove programs
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "DisplayName" "${PRODUCT_NAME}"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "InstallLocation" "$\"$INSTDIR$\""
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "DisplayIcon" "$\"$INSTDIR\SpeakerSim.exe$\""
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "Publisher" "Gregor Pintar"
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "NoModify" 1
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "NoRepair" 1
  
  ; Register file association
  WriteRegStr HKCU "Software\Classes\.ssim" "" "${PRODUCT_NAME}"
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}" "" "${PRODUCT_NAME}"
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}\shell" "" "open"
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}\DefaultIcon" "" "$INSTDIR\SpeakerSim.exe"
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}\shell\open\command" "" '"$INSTDIR\SpeakerSim.exe" "%1"'
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}\shell\edit" "" "Open in ${PRODUCT_NAME}"
  WriteRegStr HKCU "Software\Classes\${PRODUCT_NAME}\shell\edit\command" "" '"$INSTDIR\SpeakerSim.exe" "%1"'
SectionEnd

Function finish_page_action
  ; Desktop shortcut
  CreateShortCut "$DESKTOP\${PRODUCT_NAME}.lnk" "$INSTDIR\SpeakerSim.exe" "" "$INSTDIR\SpeakerSim.exe"
FunctionEnd

Section "Uninstall"
  SetOutPath "$TEMP"

  ; Remove shortcuts
  Delete /REBOOTOK "$SMPROGRAMS\${PRODUCT_NAME}.lnk"
  Delete /REBOOTOK "$DESKTOP\${PRODUCT_NAME}.lnk"
  
  ; Delete files
  Delete /REBOOTOK "$INSTDIR\SpeakerSim.jar"
  Delete /REBOOTOK "$INSTDIR\SpeakerSim.exe"
  RMDir /r /REBOOTOK "$INSTDIR\jre"
  Delete /REBOOTOK "$INSTDIR\uninstall.exe"
  RMDir /REBOOTOK "$INSTDIR"
  
  ; Unregister file association
  DeleteRegKey HKCU "Software\Classes\${PRODUCT_NAME}"
  DeleteRegKey HKCU "Software\Classes\.ssim"
  
  ; Remove uninstaller information from the registry
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
SectionEnd
