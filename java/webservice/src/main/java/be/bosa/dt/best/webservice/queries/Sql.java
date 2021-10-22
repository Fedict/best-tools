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
	protected String from = "";
	protected String where = "";
	protected String order = "";
	protected String limit = "";
	int vars = 0;

	/**
	 * Add order by clause
	 * 
	 * @param str 
	 */
	public void order(String str) {
		this.order = str;
	}
	
	public void orderById() {
		order("identifier");
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
	 * Add pagination with start identifier (don't use OFFSET, that's more resource intensive)
	 */
	public void paginate() {
		where("identifier > ");
		limit();
	}
	
	/**
	 * Add limit clause
	 */
	public void limit(){
		this.limit = "$" + ++vars;
	}

	/**
	 * Build the SQL select string
	 * 
	 * @return 
	 */
	public String build() {
		String str = String.join(" ", "SELECT", select, "FROM", from);
		if (!where.isEmpty()) {
			str = String.join(" ", str, "WHERE", where);
		}
		if (!order.isEmpty()) {
			str = String.join(" ", str, "ORDER BY", order, "LIMIT", limit);
		}
		Log.debug(str);
		return str;
	}
}
