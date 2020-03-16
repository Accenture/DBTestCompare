docker ps -a
echo "mkdir /var/opt/mssql/backup"
docker exec -i "sqlserver-container" mkdir "/var/opt/mssql/backup"
echo "copy ./docker/AdventureWorks2008R2FullDatabaseBackup.bak to /var/opt/mssql/backup" 
docker cp "./docker/AdventureWorks2008R2FullDatabaseBackup.bak" sqlserver-container:/var/opt/mssql/backup