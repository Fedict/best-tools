#!/usr/bin/perl

use DBI;
use Parse::CSV;
use strict;


my $dsn = "dbi:Pg:dbname=best;host=localhost;port=5432";
my $user = 'best';
my $pass = 'best';

my $csvdir = '.';

my $dbh = DBI->connect($dsn, $user, $pass, { RaiseError => 0, AutoCommit => 1});

sub create_cities {
	my $sql = "DROP TABLE IF EXISTS cities CASCADE";
	my $sth = $dbh->do($sql);
	#$dbh->commit or die $DBI::errstr;

	my $sql = "CREATE UNLOGGED TABLE cities (
		ID VARCHAR(16) PRIMARY KEY,
		NAME_NL VARCHAR(60),
		NAME_FR VARCHAR(60)
	)";
	my $sth = $dbh->do($sql);
	#$dbh->commit or die $DBI::errstr;
}


sub insert_cities {
	my ($prefix, $table) = @_;

	my $sql = "INSERT INTO cities VALUES(?, ?, ?) ON CONFLICT DO NOTHING";
	my $sth = $dbh->prepare($sql);

	my $csv = Parse::CSV->new(
		file => $csvdir . '/' . $table . '.csv',
		names => 1);

	while (my $row = $csv->fetch) {
		$sth->execute(
			$prefix . $row->{id},
		       	$row->{name_nl},
		       	$row->{name_fr}
		) or print "error";
	}
}


sub create_streets {
	my $sql = "DROP TABLE IF EXISTS streets CASCADE";
	my $sth = $dbh->do($sql);
	 #$dbh->commit or die $DBI::errstr;

	my $sql = "CREATE UNLOGGED TABLE streets (
		ID VARCHAR(16) PRIMARY KEY,
		NAME_NL VARCHAR(60),
		NAME_FR VARCHAR(60),
		CITY_ID VARCHAR(16) REFERENCES cities(id)
	)";
	my $sth = $dbh->do($sql);
	#$dbh->commit or die $DBI::errstr;
}

sub insert_streets {
	my ($prefix, $table) = @_;

	my $sql = "INSERT INTO streets VALUES(?, ?, ?, ?) ON CONFLICT DO NOTHING";
	my $sth = $dbh->prepare($sql);

	my $nr = 0;
	my $csv = Parse::CSV->new(
		file => $csvdir . '/' . $table . '.csv',
		names => 1);
	while (my $row = $csv->fetch) {
		$sth->execute(
			$prefix . $row->{id},
		       	$row->{name_nl},
		       	$row->{name_fr},
			$row->{city_id} ? "$prefix" . $row->{city_id} : undef
		);
		#		if (++$nr % 10000 == 0) {
		#	$dbh->commit;
		#}
	}
	#$dbh->commit;
}

sub create_addresses {
	my $sql = "DROP TABLE IF EXISTS addresses CASCADE";
	my $sth = $dbh->do($sql);
	#	$dbh->commit or die $DBI::errstr;

	my $sql = "CREATE UNLOGGED TABLE addresses (
		ID VARCHAR(16) PRIMARY KEY,
		STREETNO VARCHAR(16),
		BOXNO VARCHAR(24),
		STREET_ID VARCHAR(16) REFERENCES streets(id),
		CITY_ID VARCHAR(16) REFERENCES cities(id),
		X DOUBLE PRECISION,
		Y DOUBLE PRECISION,
		GEOM GEOMETRY(Point, 4326)
	)";
	my $sth = $dbh->do($sql);
	#	$dbh->commit or die $DBI::errstr;
}

sub insert_addresses {
	my ($prefix, $table) = @_;

	my $sql = "INSERT INTO addresses VALUES(?, ?, ?, ?, ?, ?, ?, 
			ST_Transform(
				ST_SetSRID(ST_MakePoint(?, ?), 31370), 
			4326)) 
		ON CONFLICT DO NOTHING";
	my $sth = $dbh->prepare($sql);

	my $nr = 0;
	my $csv = Parse::CSV->new(
		file => $csvdir . '/' . $table . '.csv',
		names => 1);
	while (my $row = $csv->fetch) {
		next if ($row->{status} ne "current");
		$sth->execute(
			$prefix . $row->{id},
			$row->{number},
			$row->{box},
			$prefix . $row->{street_id},
			$prefix . $row->{city_id},
		       	$row->{x},
		       	$row->{y},
			$row->{x}, $row->{y}
		);
		#		if (++$nr % 10000 == 0) {
			#		$dbh->commit;
			#}
	}
	#$dbh->commit;
}
print "Cities\n";

create_cities();

insert_cities('BXL', 'Brussels_municipalities');
insert_cities('VLA', 'Flanders_municipalities');
insert_cities('WAL', 'Wallonia_municipalities');

print "Streets\n";

create_streets();

insert_streets('BXL', 'Brussels_streetnames');
insert_streets('VLA', 'Flanders_streetnames');
insert_streets('WAL', 'Wallonia_streetnames');

print "Addresses\n";

create_addresses();

insert_addresses('BXL', 'Brussels_addresses');
insert_addresses('VLA', 'Flanders_addresses');
insert_addresses('WAL', 'Wallonia_addresses');
