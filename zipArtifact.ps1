Remove-Item -path "./target/jdbc_drivers" -Recurse -Include mssql*.jar
Get-ChildItem -path "./target/" -Recurse -Include DBTestCompare-*SNAPSHOT-jar-with-dependencies.jar | compress-archive -DestinationPath ./DBTestCompare.zip
compress-archive -path "./test-definitions" DBTestCompare.zip -update
compress-archive -path "./deploy" DBTestCompare.zip -update
compress-archive -path "./target/jdbc_drivers" DBTestCompare.zip -update
compress-archive "README.md" DBTestCompare.zip -update
compress-archive "LICENSE" DBTestCompare.zip -update
compress-archive "LICENSE-3RD-PARTY" DBTestCompare.zip -update

