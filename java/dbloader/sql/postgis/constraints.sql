CREATE INDEX ON streets(city_id);
CREATE INDEX ON addresses(postal_id);
CREATE INDEX ON addresses(city_id);
CREATE INDEX ON addresses(part_id);
			
ALTER TABLE municipalities ADD PRIMARY KEY(id);
ALTER TABLE municipalityparts ADD PRIMARY KEY(id);
ALTER TABLE streets ADD PRIMARY KEY(id);
ALTER TABLE addresses ADD PRIMARY KEY(id);

CREATE INDEX ON addresses USING GIST(geom);
			
VACUUM FULL ANALYZE;