cd %~dp0
java.exe -classpath bin\classes\;lib\jason.jar;lib\jogl-all.jar;lib\gluegen-rt.jar;lib\gluegen-rt-natives-windows-amd64.jar;lib\jogl-all-natives-windows-amd64.jar Player %1
pause