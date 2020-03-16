Remove-Item -path "./target/jdbc_drivers" -Recurse -Include mssql*.jar
Get-ChildItem -path "./target/" -Recurse -Include DBTestCompare-*SNAPSHOT-jar-with-dependencies.jar | compress-archive -DestinationPath ./DBTestCompare$(dBTestCompareVersion).zip
compress-archive -path "./test-definitions" DBTestCompare$(dBTestCompareVersion).zip -update
compress-archive -path "./deploy" DBTestCompare$(dBTestCompareVersion).zip -update
compress-archive -path "./target/jdbc_drivers" DBTestCompare$(dBTestCompareVersion).zip -update
compress-archive -path "./target/jdbc_drivers" DBTestCompare$(dBTestCompareVersion).zip -update
compress-archive "README.md" DBTestCompare$(dBTestCompareVersion).zip -update
compress-archive "LICENSE" DBTestCompare$(dBTestCompareVersion).zip -update
compress-archive "LICENSE-3RD-PARTY" DBTestCompare$(dBTestCompareVersion).zip -update

