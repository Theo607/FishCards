@echo off
REM Run the application using the bundled JRE
set DIR=%~dp0
"%DIR%jre\bin\javaw.exe" -jar "%DIR%Flashcards-project.jar"
pause
