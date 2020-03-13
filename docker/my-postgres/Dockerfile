# Derived from official postgres image (our base image)
FROM library/postgres

# Add a database
ENV POSTGRES_USER dbquality
ENV POSTGRES_PASSWORD dbquality
ENV POSTGRES_DB dbquality


# Add the content of the sql-scripts/ directory to your image
# All scripts in docker-entrypoint-initdb.d/ are automatically
# executed during container startup
COPY ./sql-scripts/ /docker-entrypoint-initdb.d/