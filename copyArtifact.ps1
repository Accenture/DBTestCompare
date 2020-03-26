$release="./release"
New-Item -Path $release -ItemType Directory -Force
Write-Output "Artifact application file name $release"
Remove-Item -path "./target/jdbc_drivers" -Recurse -Include mssql*.jar
Get-ChildItem -path "./target/" -Recurse -Include DBTestCompare-*SNAPSHOT-jar-with-dependencies.jar `
    | Copy-Item -Destination $release
Copy-Item -path "./test-definitions/" -destination $release -Force -Recurse -Container
Copy-Item -path "./deploy/" -destination $release -Force -Recurse -Container
Copy-Item -path "./target/jdbc_drivers/" -destination $release -Force -Recurse -Container
Copy-Item "README.md" -destination $release -Force
Copy-Item "LICENSE" -destination $release -Force
Copy-Item "LICENSE-3RD-PARTY" -destination $release -Force