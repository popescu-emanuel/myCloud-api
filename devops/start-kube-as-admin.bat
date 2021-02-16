echo off

echo.
echo Starting minikube as system administrator
powershell -command "Start-Process cmd -ArgumentList '/c cd /d %CD% && start-kube.bat' -Verb runas"
echo.
