FROM mcr.microsoft.com/mssql/server:2019-latest

HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=10 \
	CMD /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P "yourStrong22Password" -Q"SELECT 1" || exit 1