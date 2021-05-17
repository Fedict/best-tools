/*
 * Copyright (c) 2021, FPS BOSA DG DT
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package be.bosa.dt.hibernate;

import java.sql.Types;
import org.hibernate.boot.model.TypeContributions;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.spatial.GeolatteGeometryJavaTypeDescriptor;
import org.hibernate.spatial.GeolatteGeometryType;
import org.hibernate.spatial.JTSGeometryJavaTypeDescriptor;
import org.hibernate.spatial.JTSGeometryType;
import org.hibernate.spatial.SpatialDialect;
import org.hibernate.spatial.SpatialFunction;
import org.hibernate.spatial.SpatialRelation;
import org.hibernate.type.StandardBasicTypes;

/**
 * Very minimalistic Spatialite dialect for Hibernate
 * 
 * @author Bart Hanssens
 */
public class SpatialiteDialect extends Dialect implements SpatialDialect {
	@Override
	public boolean supportsLimit() {
        return true;
    }

	@Override
	 public String getLimitString(String query, boolean hasOffset) {
        return hasOffset ? query + " LIMIT ? OFFSET ?" 
						: query + " LIMIT ?";
    }

	@Override
	public String getSpatialAggregateSQL(String columnName, int aggregation) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getSpatialFilterExpression(String columnName) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getSpatialRelateSQL(String columnName, int spatialRelation) {
		if (spatialRelation == SpatialRelation.WITHIN) {
			return " ST_Within(" + columnName + ", ?)";
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getDWithinSQL(String columnName) {
		return "PtDistWithin(" + columnName + ", ?, ?, ?)";
	}

	@Override
	public String getHavingSridSQL(String columnName) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getIsEmptySQL(String columnName, boolean bln) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean supports(SpatialFunction sf) {
		return sf == SpatialFunction.dwithin || sf == SpatialFunction.distance;
	}

	@Override
	public boolean supportsFiltering() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
		typeContributions.contributeType( new GeolatteGeometryType( SpatialiteGeometryTypeDescriptor.INSTANCE ) );
		typeContributions.contributeType( new JTSGeometryType( SpatialiteGeometryTypeDescriptor.INSTANCE ) );

		typeContributions.contributeJavaTypeDescriptor( GeolatteGeometryJavaTypeDescriptor.INSTANCE );
		typeContributions.contributeJavaTypeDescriptor( JTSGeometryJavaTypeDescriptor.INSTANCE );
	}
	
	public SpatialiteDialect() {
		super();
		// Register Geometry column type

		registerColumnType( SpatialiteGeometryTypeDescriptor.INSTANCE.getSqlType(), "GEOMETRY" );
	
		registerColumnType(Types.BLOB, "blob");
		registerColumnType(Types.DOUBLE, "double");
		registerColumnType(Types.DATE, "date");
		registerColumnType(Types.VARCHAR, "varchar");

	/*	registerFunction( "within", new StandardSQLFunction( "ST_Within", StandardBasicTypes.BOOLEAN ) ); */
		registerFunction( "dwithin", new StandardSQLFunction( "PtDistWithin", StandardBasicTypes.BOOLEAN ) );
		registerFunction( "distance", new StandardSQLFunction( "ST_Distance", StandardBasicTypes.DOUBLE ) );
	}
}
