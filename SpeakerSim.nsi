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
Caption "SpeakerSim"
Icon "SpeakerSim.ico"
OutFile "SpeakerSim.exe"

VIAddVersionKey "ProductName" "SpeakerSim"
VIAddVersionKey "FileDescription" "SpeakerSim"
VIAddVersionKey "LegalCopyright" ""
VIAddVersionKey "FileVersion" "1.0.0"
VIProductVersion "1.0.0.0"

!define JAR "SpeakerSim.jar"
!define PRODUCT_NAME "SpeakerSim"

; Java
!define JRE_VERSION "7.0"
!define JRE32_URL "http://javadl.oracle.com/webapps/download/AutoDL?BundleId=234472_96a7b8442fe848ef90c96a2fad6ed6d1"
!define JRE64_URL "http://javadl.oracle.com/webapps/download/AutoDL?BundleId=234474_96a7b8442fe848ef90c96a2fad6ed6d1"
!define JAVAEXE "javaw.exe"

RequestExecutionLevel user
AutoCloseWindow true
XPStyle on
SetCompress off

!include "FileFunc.nsh"
!insertmacro GetFileVersion
!insertmacro GetParameters
!include "WordFunc.nsh"
!insertmacro VersionCompare
!include "StdUtils.nsh"

;Var STR_HAYSTACK
;Var STR_NEEDLE
;Var STR_CONTAINS_VAR_1
;Var STR_CONTAINS_VAR_2
;Var STR_CONTAINS_VAR_3
;Var STR_CONTAINS_VAR_4
;Var STR_RETURN_VAR
 
;Function StrContains
  ;Exch $STR_NEEDLE
  ;Exch 1
  ;Exch $STR_HAYSTACK
    ;StrCpy $STR_RETURN_VAR ""
    ;StrCpy $STR_CONTAINS_VAR_1 -1
    ;StrLen $STR_CONTAINS_VAR_2 $STR_NEEDLE
    ;StrLen $STR_CONTAINS_VAR_4 $STR_HAYSTACK
    ;loop:
      ;IntOp $STR_CONTAINS_VAR_1 $STR_CONTAINS_VAR_1 + 1
      ;StrCpy $STR_CONTAINS_VAR_3 $STR_HAYSTACK $STR_CONTAINS_VAR_2 $STR_CONTAINS_VAR_1
      ;StrCmp $STR_CONTAINS_VAR_3 $STR_NEEDLE found
      ;StrCmp $STR_CONTAINS_VAR_1 $STR_CONTAINS_VAR_4 done
      ;Goto loop
    ;found:
      ;StrCpy $STR_RETURN_VAR $STR_NEEDLE
      ;Goto done
    ;done:
   ;Pop $STR_NEEDLE
   ;Exch $STR_RETURN_VAR  
;FunctionEnd
 
;!macro _StrContainsConstructor OUT NEEDLE HAYSTACK
  ;Push '${HAYSTACK}'
  ;Push '${NEEDLE}'
  ;Call StrContains
  ;Pop '${OUT}'
;!macroend
 
;!define StrContains '!insertmacro "_StrContainsConstructor"'
 
Section
  DetailPrint "Loading ..."
  SetOutPath $EXEDIR
  
  ${GetParameters} $1

  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\${JAVAEXE}"
  IfFileExists $R0 0 CheckJavaHome
  Exec '"$R0" -jar "${JAR}" $1'
  IfErrors CheckJavaHome End

  CheckJavaHome:
    ClearErrors
    ReadEnvStr $R0 "JAVA_HOME"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors CheckRegistry64     
    IfFileExists $R0 0 CheckRegistry64
    Call CheckJREVersion
    IfErrors CheckRegistry64
    Exec '"$R0" -jar "${JAR}" $1'
    IfErrors CheckRegistry64 End

  CheckRegistry64:
    ClearErrors
    SetRegView 64
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
	StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfFileExists $R0 0 CheckRegistry32
    Call CheckJREVersion
    IfErrors CheckRegistry32
    Exec '"$R0" -jar "${JAR}" $1'
    IfErrors CheckRegistry32 End
  
  CheckRegistry32:
    ClearErrors
    SetRegView 32
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    IfErrors CheckPath
	StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfFileExists $R0 0 CheckPath
    Call CheckJREVersion
    IfErrors CheckPath
    Exec '"$R0" -jar "${JAR}" $1'
    IfErrors CheckPath End

  CheckPath:
    ;ClearErrors
    ;Exec '"${JAVAEXE}" -jar "${JAR}" $1'
    ;IfErrors CheckJarfile End

  CheckJarfile:
    ;ClearErrors
    ;ReadRegStr $R0 HKCR "jarfile\shell\open\command" ""
    ;IfErrors InstallJRE
    ;${StrContains} $0 "${JAVAEXE}" $R0
    ;StrCmp $0 "" InstallJRE
    ;ExecShell "" '"${JAR}"' $1
    ;IfErrors InstallJRE End
 
  InstallJRE:
    ClearErrors
    StrCpy $2 "$TEMP\JRE.exe"
	
    System::Call "kernel32::GetCurrentProcess()i.s"
    System::Call "kernel32::IsWow64Process(is,*i.r0)"
    StrCmpS $0 0 +3
      StrCpy $0 "${JRE64_URL}"
    Goto +2
      StrCpy $0 "${JRE32_URL}"
	
	DetailPrint "Downloading Java Runtime ..."
    NSISdl::download /TIMEOUT=30000 $0 $2
    Pop $R0 ;Get the return value
    StrCmp $R0 "success" +3
      MessageBox MB_ICONSTOP "Download failed: $R0"
      Abort
    
    DetailPrint "Installing Java Runtime ..."
    Push $1
    ${StdUtils.ExecShellWaitEx} $0 $1 $2 "" "/s"
    StrCmp $0 "error" End
	StrCmp $0 "no_wait" End
    ${StdUtils.WaitForProcEx} $0 $1
    Pop $1
    Delete $2
	IfErrors End CheckJavaHome

  End:
SectionEnd

; Pass the path by $R0
Function CheckJREVersion
  Push $R1

  ${GetFileVersion} $R0 $R1
  ${VersionCompare} ${JRE_VERSION} $R1 $R1

  StrCmp $R1 "1" 0 +2
    SetErrors

  Pop $R1
FunctionEnd
