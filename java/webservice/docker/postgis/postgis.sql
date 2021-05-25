CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;
CREATE EXTENSION fuzzystrmatch;

CREATE USER best_reader WITH PASSWORD 'best_reader';
GRANT CONNECT ON DATABASE best to best_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO best_reader;

CREATE TABLE addresses(
	id VARCHAR(88) NOT NULL,
	city_id VARCHAR(88) NOT NULL,
	part_id VARCHAR(88),
	street_id VARCHAR(88) NOT NULL,
	postal_id VARCHAR(88) NOT NULL,
	houseno VARCHAR(12),
	boxno VARCHAR(40),
	status VARCHAR(10), 
	l72x DOUBLE PRECISION NOT NULL,
	l72y DOUBLE PRECISION NOT NULL,
	geom GEOMETRY NOT NULL);

CREATE TABLE municipalities(
	id VARCHAR(88) NOT NULL,
	niscode VARCHAR(5) NOT NULL,
	name_nl VARCHAR(80),
	name_fr VARCHAR(80),
	name_de VARCHAR(80));

CREATE TABLE municipalityparts(
	id VARCHAR(88) NOT NULL,
	name_nl VARCHAR(80),
	name_fr VARCHAR(80),
	name_de VARCHAR(80));

CREATE TABLE postals(
	id VARCHAR(88) NOT NULL,
	zipcode VARCHAR(4) NOT NULL,
	name_nl VARCHAR(240),
	name_fr VARCHAR(240),
	name_de VARCHAR(240));

CREATE TABLE streets(
	id VARCHAR(88) NOT NULL,
	city_id VARCHAR(88) NOT NULL,
	name_nl VARCHAR(80),
	name_fr VARCHAR(80),
	name_de VARCHAR(80),
	status VARCHAR(10));


\COPY addresses FROM 'addresses.csv' WITH DELIMITER ';' QUOTE '"' csv;
\COPY municipalities FROM 'municipalities.csv' WITH DELIMITER ';' QUOTE '"' csv;
\COPY municipalityparts FROM 'municipalityparts.csv' WITH DELIMITER ';' QUOTE '"' csv;
\COPY postals FROM 'postals.csv' WITH DELIMITER ';' QUOTE '"' csv;
\COPY streets FROM 'streets.csv' WITH DELIMITER ';' QUOTE '"' csv;

CREATE INDEX ON streets(city_id);
CREATE INDEX ON addresses(postal_id);
CREATE INDEX ON addresses(city_id);
CREATE INDEX ON addresses(part_id);

CREATE TABLE postal_municipalities AS (
	SELECT DISTINCT city_id, zipcode
	FROM addresses a, postals p
	WHERE a.postal_id = p.id);

CREATE TABLE postal_streets AS (
	SELECT DISTINCT street_id, zipcode
	FROM addresses a, postals p
	WHERE a.postal_id = p.id);

ALTER TABLE addresses ADD PRIMARY KEY(id);
ALTER TABLE municipalities ADD PRIMARY KEY(id);
ALTER TABLE municipalityparts ADD PRIMARY KEY(id);
ALTER TABLE streets ADD PRIMARY KEY(id);

CREATE INDEX ON addresses USING GIST(geom);
			
VACUUM FULL ANALYZE;
