geofort-store-service //IS//GeofortStore --DisplayName="Geofort Store" --Startup auto --Install=%cd%\geofort-store-service.exe --Jvm=auto --Classpath geofort-store.jar --StartMode=jvm --StartClass=cz.hatoff.geofort.store.StoreApplication --StartMethod start --StopMode=jvm --StopClass=cz.hatoff.geofort.store.StoreApplication --StopMethod stop
echo OK!
@pause