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
package be.bosa.dt.best.webservice.queries;

import io.quarkus.logging.Log;

/**
 * Helper class for building SQL select statements
 * 
 * @author Bart.Hanssens
 */
public abstract class Sql {
	protected String select = "";
	protected String alias = "";
	protected String from = "";
	protected String join = "";
	protected String where = "";
	protected String order = "";
	protected String limit = "";
	protected int vars = 0;
	protected boolean rewriteHack = false;

	/**
	 * Add order by clause
	 * 
	 * @param str 
	 */
	public void order(String str) {
		this.order = str;
	}
	
	public void orderById() {
		order(alias + ".identifier");
	}

	/**
	 * Add where clause with variable
	 * 
	 * @param str 
	 */
	public void where(String str) {
		String tmp = str + " $" + ++vars;
		this.where = (this.where.equals("")) ? tmp : this.where + " AND " + tmp;
	}

	/**
	 * Add where clause, searching for names in three languages
	 * 
	 * @param nl Dutch field name
	 * @param fr French field name
	 * @param de German field name
	 * @param matchType
	 */
	public void whereNames(String nl, String fr, String de, String matchType) {
		String v = "$" + ++vars;
		
		String op = (matchType == null || matchType.equals("exact")) ? "=" : "%";
		String tmp = "(" + 
						"LOWER(IM_UNACCENT(" + nl + "))" + op + "LOWER(IM_UNACCENT(" + v + ")) OR " +
						"LOWER(IM_UNACCENT(" + fr + "))" + op + "LOWER(IM_UNACCENT(" + v + ")) OR " +
						"LOWER(IM_UNACCENT(" + de + "))" + op + "LOWER(IM_UNACCENT(" + v + ")) " +
					")";
		this.where = (this.where.equals("")) ? tmp : this.where + " AND " + tmp;
	}

	
	/**
	 * Add pagination with start identifier (don't use OFFSET, that's more resource intensive)
	 */
	public void paginate() {
		where(alias + ".identifier > ");
		limit();
	}
	
	/**
	 * Add limit clause
	 */
	public void limit(){
		this.limit = "$" + ++vars;
	}

	/**
	 * Add limit clause
	 */
	public void unlimited(){
		this.limit = "ALL";
	}

	public void setRewriteHack() {
		rewriteHack = true;
	}

	/**
	 * Build the SQL select string
	 * 
	 * @return 
	 */
	public String build() {
		StringBuilder bld = new StringBuilder(1024);

		bld.append("SELECT ").append(select)
			.append(" FROM ").append(from).append(" ").append(alias)
			.append(" ").append(join);

		if (!where.isEmpty()) {
			bld.append(" WHERE ").append(where);
		}
		if (!order.isEmpty()) {
			bld.append(" ORDER BY ").append(order);
		}
		if (!limit.isEmpty()) {
			if (!rewriteHack) {
				bld.append(" LIMIT ").append(limit);
			} else {
				bld.insert(0, "WITH q AS (");
				bld.append(") SELECT * FROM q LIMIT ").append(limit);	
		}
		}
		Log.debug(bld.toString());
		return bld.toString();
	}
}
