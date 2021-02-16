@echo off
set dockerUser=popescuemanuel24
set project=mycloud-api
set version=%1
set imageName=%dockerUser%/%project%:%version%

if "%~1"=="" goto INPUT_VALIDATION_FAILURE

REM mvn clean install -DskipTests

cls

cd..
echo.
echo # Building the docker image
docker build -t mycloud-api .

echo.
echo # Tag the image name for docker repository
docker tag mycloud-api %imageName%

echo.
echo # Push image to docker repository
docker push %imageName%

echo.
echo # Restart mycloud-api pods
kubectl rollout restart deployment %project%

goto FINISH

:INPUT_VALIDATION_FAILURE
echo.
echo ERROR: No input parameters provided
echo eg. install-and-deploy 1.0.0
echo.

:FINISH
