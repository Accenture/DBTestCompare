Write-Output "Setting  DBTestCompare version " $env:DBTESTCOMPAREVERSION
$version="<version>$env:DBTESTCOMPAREVERSION-SNAPSHOT</version>"
((Get-Content -path ./pom.xml -Raw) -replace '<version>\d\.\d.\d-SNAPSHOT<\/version>',$version) | Set-Content -Path ./pom.xml