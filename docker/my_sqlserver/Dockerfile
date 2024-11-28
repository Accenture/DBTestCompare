FROM mcr.microsoft.com/mssql/server:2019-latest

USER root

RUN apt-get update \
    && apt-get install -y gnupg gnupg2 gnupg1 \
    && apt-get install -y curl apt-transport-https \
    && curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - \
    && curl https://packages.microsoft.com/config/ubuntu/18.04/prod.list > /etc/apt/sources.list.d/mssql-release.list \
    && apt-get update \
    && ACCEPT_EULA=Y apt-get install -y msodbcsql17 mssql-tools \
    && echo 'export PATH="$PATH:/opt/mssql-tools/bin"' >> ~/.bashrc \
    && . ~/.bashrc

HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=10 \
  CMD /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P "yourStrong22Password" -Q"SELECT 1" || exit 1
