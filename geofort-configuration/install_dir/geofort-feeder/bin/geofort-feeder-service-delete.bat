SET GEOFORT_PATH=%~dp0
sc create "Geofort Feeder" binPath= "%GEOFORT_PATH%geofort-feeder.exe"