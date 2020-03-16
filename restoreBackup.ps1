docker ps -a
docker exec -i sqlserver-container mkdir /var/opt/mssql/backup
docker cp ./docker/"AdventureWorks2008R2FullDatabaseBackup.bak" sqlserver-container:/var/opt/mssql/backup
docker exec -i sqlserver-container /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P "yourStrong(!)Password" -Q "RESTORE DATABASE AdventureWorks2008R2 FROM DISK = '/var/opt/mssql/backup/AdventureWorks2008R2FullDatabaseBackup.bak' WITH MOVE 'AdventureWorks2008R2_Data' TO '/var/opt/mssql/data/AdventureWorks2008R2.mdf', MOVE 'AdventureWorks2008R2_Log' TO '/var/opt/mssql/data/AdventureWorks2008R2_1.LDF'"
