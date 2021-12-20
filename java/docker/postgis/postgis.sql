CREATE DATABASE best;

\c best;

CREATE EXTENSION postgis;
CREATE EXTENSION fuzzystrmatch;
CREATE EXTENSION pg_trgm;
CREATE EXTENSION unaccent;

/* wrapper function for unaccent to make it usable in index */
CREATE OR REPLACE FUNCTION im_unaccent(varchar)
  RETURNS text AS $$
    SELECT unaccent($1)
  $$ LANGUAGE sql IMMUTABLE;


CREATE USER best_reader WITH PASSWORD 'best_reader';
GRANT CONNECT ON DATABASE best to best_reader;

/* custom types */
CREATE TYPE enumStatus 
    AS ENUM('current', 'proposed', 'retired', 'reserved');
CREATE TYPE enumStreetnameType
    AS ENUM('hamlet', 'streetname');
CREATE TYPE enumPositionGeometryMethodValueType
    AS ENUM('assignedByAdministrator', 'derivedFromObject');
CREATE TYPE enumPositionSpecificationValueType
    AS ENUM('building','buildingUnit', 'entrance', 'mooringPlace', 'municipality', 
        'parcel', 'plot', 'stand', 'street');

/* Create tables, don't write to WAL since it will be a read-only source anyway */
CREATE UNLOGGED TABLE Address(
    identifier VARCHAR(100) NOT NULL,
    minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1,
    mIdentifier VARCHAR(100) NOT NULL,
    mMinorVersionIdentifier SMALLINT,
    mpIdentifier VARCHAR(100),
    mpMinorVersionIdentifier SMALLINT,
    sIdentifier VARCHAR(100) NOT NULL,
    sMinorVersionIdentifier SMALLINT,
    pIdentifier VARCHAR(100) NOT NULL,
    pMinorVersionIdentifier SMALLINT,
    housenumber VARCHAR(15),
    boxnumber VARCHAR(35),
    officiallyAssigned BOOLEAN,
    status enumStatus NOT NULL,
    validFrom TIMESTAMPTZ,
    validTo TIMESTAMPTZ,
    beginLifeSpanVersion TIMESTAMPTZ,
    endLifeSpanVersion TIMESTAMPTZ,
    point GEOMETRY(POINT, 31370),
    positionGeometryMethod enumPositionGeometryMethodValueType,
    positionSpecification enumPositionSpecificationValueType,
    firstAddress BOOLEAN);

CREATE UNLOGGED TABLE Municipality(
    identifier VARCHAR(100) NOT NULL,
    minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1,
    refnisCode VARCHAR(5) NOT NULL,
    nameNL VARCHAR(100),
    nameFR VARCHAR(100),
    nameDE VARCHAR(100));

CREATE UNLOGGED TABLE PartOfMunicipality(
    identifier VARCHAR(100) NOT NULL, 
    minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1,
    nameNL VARCHAR(100),
    nameFR VARCHAR(100),
    nameDE VARCHAR(100));

CREATE UNLOGGED TABLE PostalInfo(
    identifier VARCHAR(100) NOT NULL,
    minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1,
    postalCode VARCHAR(4) NOT NULL,
    nameNL VARCHAR(240),
    nameFR VARCHAR(240),
    nameDE VARCHAR(240));
	
CREATE UNLOGGED TABLE Street(
    identifier VARCHAR(100) NOT NULL,
    minorVersionIdentifier SMALLINT NOT NULL DEFAULT 1,
    mIdentifier VARCHAR(100) NOT NULL,
    mMinorVersionIdentifier SMALLINT,
    nameNL VARCHAR(100),
    nameFR VARCHAR(100),
    nameDE VARCHAR(100),
    homonymAddition VARCHAR(25),
    streetnameType enumStreetnameType,
    status enumStatus NOT NULL,
    validFrom TIMESTAMPTZ,
    validTo TIMESTAMPTZ,
    beginLifeSpanVersion TIMESTAMPTZ,
    endLifeSpanVersion TIMESTAMPTZ);

/* Load data into tables */
\COPY Address FROM 'addresses.csv' WITH DELIMITER ';' NULL as '' QUOTE '"' csv;
\COPY Municipality FROM 'municipalities.csv' WITH DELIMITER ';' NULL as '' QUOTE '"' csv;
\COPY PartOfMunicipality FROM 'municipalityparts.csv' WITH DELIMITER ';' NULL as '' QUOTE '"' csv;
\COPY Postalinfo FROM 'postals.csv' WITH DELIMITER ';' NULL as '' QUOTE '"' csv;
\COPY Street FROM 'streets.csv' WITH DELIMITER ';' NULL as '' QUOTE '"' csv;

/* Remove addresses that would violate constraints, shouldn't happen but ... it sometimes does */
DELETE FROM Address a 
WHERE NOT EXISTS 
	(SELECT 1 FROM Municipality m 
	WHERE a.midentifier = m.identifier);

DELETE FROM Address a 
WHERE NOT EXISTS 
	(SELECT 1 FROM Postalinfo p 
	WHERE a.pidentifier = p.identifier);
 
DELETE FROM Address a 
WHERE NOT EXISTS 
	(SELECT 1 FROM Street s 
	WHERE a.sidentifier = s.identifier);
 
/* Add primary keys */
ALTER TABLE Address 
    ADD CONSTRAINT pkAddress PRIMARY KEY(identifier, minorVersionIdentifier);
ALTER TABLE Municipality
    ADD CONSTRAINT pkMunicipality PRIMARY KEY(identifier, minorVersionIdentifier);
ALTER TABLE PartOfMunicipality
    ADD CONSTRAINT pkPartOfMunicipality PRIMARY KEY(identifier, minorVersionIdentifier);
ALTER TABLE PostalInfo
    ADD CONSTRAINT pkPostalInfo PRIMARY KEY(identifier, minorVersionIdentifier);
ALTER TABLE Street
    ADD CONSTRAINT pkStreet PRIMARY KEY(identifier, minorVersionIdentifier);

ALTER TABLE Address ADD CONSTRAINT fkAddressMunicipality
    FOREIGN KEY (mIdentifier, mMinorVersionIdentifier)
    REFERENCES Municipality(identifier, minorVersionIdentifier);
ALTER TABLE Address ADD CONSTRAINT fkAddressPartOfMunicipality
    FOREIGN KEY (mpIdentifier, mpMinorVersionIdentifier)
    REFERENCES PartOfMunicipality(identifier, minorVersionIdentifier);
ALTER TABLE Address ADD CONSTRAINT fkAddressPostalInfo
    FOREIGN KEY (pIdentifier, pMinorVersionIdentifier)
    REFERENCES PostalInfo(identifier, minorVersionIdentifier);
ALTER TABLE Address ADD CONSTRAINT fkAddressStreet
    FOREIGN KEY (sIdentifier, sMinorVersionIdentifier)
    REFERENCES Street(identifier, minorVersionIdentifier);

/* Set FK-indexes */
CREATE INDEX idxAddressMunicipality ON Address(mIdentifier);
CREATE INDEX idxAddressPostal ON Address(pIdentifier);
CREATE INDEX idxAddressStreet ON Address(sIdentifier);
CREATE INDEX idxStreetMunicipality ON Street(mIdentifier);

/* Set 'first' address in apartment building: used to reduce number of 'locate-near-GPS-coordinates' results */ 
UPDATE Address a1 SET firstaddress = true
FROM 
	(SELECT midentifier, sidentifier, housenumber, MIN(COALESCE(boxnumber,'0')) AS minboxnumber 
	FROM Address
	GROUP BY midentifier, sidentifier, housenumber) 
AS a2
	WHERE a1.midentifier = a2.midentifier AND a1.sidentifier = a2.sidentifier 
		AND a1.housenumber = a2.housenumber AND COALESCE(a1.boxnumber,'0') = minboxnumber;

/* Create some auxiliary tables to speed up queries */
CREATE UNLOGGED TABLE PostalMunicipalities AS (
	SELECT DISTINCT a.mIdentifier, p.postalCode
	FROM Address a, PostalInfo p
	WHERE a.pIdentifier = p.identifier);

CREATE UNLOGGED TABLE PostalStreets AS (
	SELECT DISTINCT a.sIdentifier, p.postalCode
	FROM Address a, PostalInfo p
	WHERE a.pIdentifier = p.identifier);

/* Geo index */
CREATE INDEX idxAddressPoint ON Address
	USING GIST(point);

/* Full text indexes on names */
CREATE INDEX idxGinMunicipalityNL ON Municipality 
	USING GIN(LOWER(IM_UNACCENT(nameNL)) gin_trgm_ops);
CREATE INDEX idxGinMunicipalityFR ON Municipality
	USING GIN(LOWER(IM_UNACCENT(nameFR)) gin_trgm_ops);
CREATE INDEX idxGinMunicipalityDE ON Municipality
	USING GIN(LOWER(IM_UNACCENT(nameDE)) gin_trgm_ops);

CREATE INDEX idxGinPostalinfoNL ON Postalinfo
	USING GIN(LOWER(IM_UNACCENT(nameNL)) gin_trgm_ops);
CREATE INDEX idxGinPostalinfoFR ON Postalinfo
	USING GIN(LOWER(IM_UNACCENT(nameFR)) gin_trgm_ops);
CREATE INDEX idxGinPostalinfoDE ON Postalinfo
	USING GIN(LOWER(IM_UNACCENT(nameDE)) gin_trgm_ops);

CREATE INDEX idxGinStreetNL ON Street
	USING GIN(LOWER(IM_UNACCENT(nameNL)) gin_trgm_ops);
CREATE INDEX idxGinStreetFR ON Street
	USING GIN(LOWER(IM_UNACCENT(nameFR)) gin_trgm_ops);
CREATE INDEX idxGinStreetDE ON Street
	USING GIN(LOWER(IM_UNACCENT(nameDE)) gin_trgm_ops);


/* Values for support / debugging */
CREATE TABLE version(identifier VARCHAR(20) NOT NULL,
					valuestr VARCHAR(20) NOT NULL);
INSERT INTO version(identifier, valuestr) 
	VALUES ('api verson', :'apiversion');
INSERT INTO version(identifier, valuestr) 
	VALUES ('build date', TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD'));

/* Clean up and update statistics */
VACUUM FULL ANALYZE;

GRANT SELECT ON ALL TABLES IN SCHEMA public TO best_reader;
