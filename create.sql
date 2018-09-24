CREATE DATABASE data;

CREATE SCHEMA best;

CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;
CREATE EXTENSION fuzzystrmatch;

CREATE ROLE best_writer WITH LOGIN PASSWORD 'best_writer';
GRANT CONNECT ON DATABASE data TO best_writer;
GRANT USAGE ON SCHEMA best TO best_writer;
GRANT SELECT, UPDATE, INSERT, DELETE ON ALL TABLES IN SCHEMA best TO best_writer;

CREATE ROLE best_reader WITH LOGIN PASSWORD 'best_reader';
GRANT CONNECT ON DATABASE data TO best_reader;
GRANT USAGE ON SCHEMA best TO best_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA best TO best_reader;

CREATE UNLOGGED TABLE best.cities (
    id VARCHAR(16) PRIMARY KEY,
    region CHAR(1) NOT NULL,
    name_nl VARCHAR(60),
    name_fr VARCHAR(60)
);

CREATE UNLOGGED TABLE best.streets (
    id VARCHAR(16) PRIMARY KEY,
    region CHAR(1) NOT NULL,
    name_nl VARCHAR(60),
    name_fr VARCHAR(60),
    city_id VARCHAR(16) REFERENCES best.cities(id)
);

CREATE UNLOGGED TABLE best.addresses (
    id VARCHAR(16) PRIMARY KEY,
    region CHAR(1) NOT NULL,
    streetno VARCHAR(16) NOT NULL,
    boxno VARCHAR(24),
    street_id VARCHAR(16) REFERENCES best.streets(id),
    city_id VARCHAR(16) REFERENCES best.cities(id),
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    geom GEOMETRY(Point, 4326) NOT NULL
);
