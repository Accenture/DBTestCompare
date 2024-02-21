Write-Output "Downloading AdventureWorks2008R2FullDatabaseBackup"
New-Item -Path './zip' -ItemType Directory -Force
(New-Object System.Net.WebClient).DownloadFile("https://github.com/raczeja/DBTestCompare_backups/raw/master/AdventureWorks2008R2FullDatabaseBackup.zip", "./zip/AdventureWorks2008R2FullDatabaseBackup.zip")
Write-Output "Unzipping AdventureWorks2008R2FullDatabaseBackup"
Expand-Archive -LiteralPath './zip/AdventureWorks2008R2FullDatabaseBackup.zip' -DestinationPath ./docker -Force
 
Write-Output "Downloading sqljdbc drivers"
(New-Object System.Net.WebClient).DownloadFile("https://download.microsoft.com/download/4/0/8/40815588-bef6-4715-bde9-baace8726c2a/sqljdbc_8.2.0.0_enu.zip", "./zip/sqljdbc_8.2.0.0_enu.zip")
Write-Output "Unzipping sqljdbc drivers"
Expand-Archive -LiteralPath './zip/sqljdbc_8.2.0.0_enu.zip' -DestinationPath "./zip/sqljdbc" -Force
Copy-Item -Path "./zip/sqljdbc/sqljdbc_8.2/enu/*"  -Destination "./jdbc_drivers" -Include "mssql-jdbc-*.jar"

Write-Output "Downloading mysql sctipts"
(New-Object System.Net.WebClient).DownloadFile("https://github.com/raczeja/DBTestCompare_backups/raw/master/my-mysql/sql-scripts/sql-scripts.zip","./zip/sql-scripts-mysql.zip")
Write-Output "Unzipping mysql sctipts"
Expand-Archive -LiteralPath './zip/sql-scripts-mysql.zip' -DestinationPath "./docker/my-mysql/sql-scripts" -Force

Write-Output "Downloading postgres sctipts"
(New-Object System.Net.WebClient).DownloadFile( "https://github.com/raczeja/DBTestCompare_backups/raw/master/my-postgres/sql-scripts/sql-scripts.zip","./zip/sql-scripts-postgres.zip")
Write-Output "Unzipping postgres sctipts"
Expand-Archive -LiteralPath './zip/sql-scripts-postgres.zip' -DestinationPath "./docker/my-postgres/sql-scripts" -Force