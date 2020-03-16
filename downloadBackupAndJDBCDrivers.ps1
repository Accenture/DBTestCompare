(New-Object System.Net.WebClient).DownloadFile("https://github.com/raczeja/DBTestCompare_backups/raw/master/AdventureWorks2008R2FullDatabaseBackup.zip", "AdventureWorks2008R2FullDatabaseBackup.zip")
Expand-Archive -LiteralPath 'AdventureWorks2008R2FullDatabaseBackup.zip' -DestinationPath ./docker -Force
        
(New-Object System.Net.WebClient).DownloadFile("https://download.microsoft.com/download/4/0/8/40815588-bef6-4715-bde9-baace8726c2a/sqljdbc_8.2.0.0_enu.zip", "sqljdbc_8.2.0.0_enu.zip")
Expand-Archive -LiteralPath 'sqljdbc_8.2.0.0_enu.zip' -DestinationPath "./sqljdbc" -Force
Copy-Item -Path "./sqljdbc/sqljdbc_8.2/enu/*"  -Destination "./jdbc_drivers" -Include "mssql-jdbc-*.jar"

(New-Object System.Net.WebClient).DownloadFile("https://github.com/raczeja/DBTestCompare_backups/raw/master/my-mysql/sql-scripts/sql-scripts.zip","sql-scripts.zip")
Expand-Archive -LiteralPath 'sql-scripts.zip' -DestinationPath "./docker/my-mysql/sql-scripts" -Force

(New-Object System.Net.WebClient).DownloadFile( "https://github.com/raczeja/DBTestCompare_backups/raw/master/my-postgres/sql-scripts/sql-scripts.zip","sql-scripts.zip")
Expand-Archive -LiteralPath 'sql-scripts.zip' -DestinationPath "./docker/my-postgres/sql-scripts" -Force