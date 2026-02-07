@echo off
setlocal

set DIR=%~dp0
if exist "%DIR%gradle\wrapper\gradle-wrapper.jar" (
  set WRAPPER_JAR=%DIR%gradle\wrapper\gradle-wrapper.jar
) else (
  echo Missing gradle-wrapper.jar in %DIR%gradle\wrapper
  exit /b 1
)

java -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*

endlocal

