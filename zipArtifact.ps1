$zipFile="./zip/DBTestCompare$env:DBTESTCOMPAREVERSION.zip"
$zipFileTestResults="./zip/DBTestCompare$env:DBTESTCOMPAREVERSION.TestResults.$env:BUILD_BUILDNUMBER.zip"
New-Item -Path './zip' -ItemType Directory -Force
Write-Output "Artifact file name $zipFile"
Remove-Item -path "./target/jdbc_drivers" -Recurse -Include mssql*.jar
Get-ChildItem -path "./target/" -Recurse -Include DBTestCompare-*SNAPSHOT-jar-with-dependencies.jar `
    | compress-archive -DestinationPath ./$zipFile
compress-archive -path "./test-definitions" $zipFile -update
compress-archive -path "./deploy" $zipFile -update
compress-archive -path "./target/jdbc_drivers" $zipFile -update
compress-archive "README.md" $zipFile -update
compress-archive "LICENSE" $zipFile -update
compress-archive "LICENSE-3RD-PARTY" $zipFile -update
compress-archive -path "./target/test-output" $zipFileTestResults -update