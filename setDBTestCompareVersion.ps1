Write-Output "Setting  DBTestCompere version " $env:dBTestCompareVersion
$version="<version>$env:dBTestCompareVersion-SNAPSHOT</version>"
((Get-Content -path ./pom.xml -Raw) -replace '<version>\d\.\d-SNAPSHOT<\/version>',$version) | Set-Content -Path ./pom.xml