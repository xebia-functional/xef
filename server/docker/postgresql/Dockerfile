# Use ankane/pgvector:v0.4.4 as the base image
FROM ankane/pgvector:v0.4.4

# Set the desired version of Apache AGE
ENV AGE_VERSION=1.5.0

# Set the version of PostgreSQL
ENV PG_VERSION=15

# Install dependencies needed for downloading and installing the AGE extension
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    wget \
    build-essential \
    clang \
    llvm \
    libreadline-dev \
    zlib1g-dev \
    flex  \
    bison \
    postgresql-server-dev-all

# Download and install the specific version of Apache AGE
RUN wget https://github.com/apache/age/releases/download/PG${PG_VERSION}%2Fv${AGE_VERSION}-rc0/apache-age-${AGE_VERSION}-src.tar.gz \
    && tar -xvzf apache-age-${AGE_VERSION}-src.tar.gz

RUN cd apache-age-${AGE_VERSION} \
    && make PG_CONFIG=/usr/lib/postgresql/15/bin/pg_config install \
    && rm -rf apache-age-${AGE_VERSION}-src.tar.gz

# Set the default command to run when starting the container
CMD ["postgres"]
