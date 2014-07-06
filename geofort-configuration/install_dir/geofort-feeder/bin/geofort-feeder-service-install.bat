geofort-feeder-service //IS//GeofortFeeder --DisplayName="Geofort Feeder" --Startup auto --Install=%cd%\geofort-feeder-service.exe --Jvm=auto --Classpath geofort-feeder.jar --StartMode=jvm --StartClass=cz.hatoff.geofort.feeder.FeederApplication --StartMethod start --StopMode=jvm --StopClass=cz.hatoff.geofort.feeder.FeederApplication --StopMethod stop
echo OK!
@pause