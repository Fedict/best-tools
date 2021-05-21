CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE addresses(
	id VARCHAR(88) NOT NULL,
	city_id VARCHAR(88) NOT NULL,
	part_id VARCHAR(88) NOT NULL,
	street_id VARCHAR(88) NOT NULL,
	postal_id VARCHAR(88) NOT NULL,
	houseno VARCHAR(12),
	boxno VARCHAR(40),
	status VARCHAR(10), 
	l72x DOUBLE NOT NULL,
	l72y DOUBLE NOT NULL,
	geom GEOMETRY NOT NULL);

CREATE TABLE postals(
	id VARCHAR(88) NOT NULL,
	zipcode VARCHAR(4) NOT NULL,
	name_nl VARCHAR(240),
	name_fr VARCHAR(240),
	name_de VARCHAR(240));

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

CREATE TABLE streets(
	id VARCHAR(88) NOT NULL,
	city_id VARCHAR(88) NOT NULL,
	name_nl VARCHAR(80),
	name_fr VARCHAR(80),
	name_de VARCHAR(80),
	status VARCHAR(10));
