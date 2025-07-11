/*
 * Copyright (c) 2018, FPS BOSA DG DT
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
package be.bosa.dt.best.dao;

/**
 * Helper class for BeST objects with namespace and ID
 * 
 * @author Bart Hanssens
 */
public abstract class BestObject {
	private String namespace = "";
	private String id = "";
	private String version = "";
	private String status = "";

	
	/**
	 * Get namespace provided by the Region.
	 * Combine the namespace with the ID and version to get a globally unique ID.
	 * 
	 * @return namespace as string
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Set namespace provided by the Region
	 * 
	 * @param namespace 
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace.endsWith("/") 
							? namespace.substring(0, namespace.length() - 1).intern()
							: namespace.intern();
	}

	/**
	 * Get ID provided by the region
	 * Combine the namespace with the ID and version to get a globally unique ID.
	 * 
	 * @return ID as string
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set ID provided by the region
	 * 
	 * @param id 
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Get status
	 * 
	 * @return status
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Set status
	 * 
	 * @param status 
	 */
	public void setStatus(String status) {
		this.status = status.intern();
	}

	/**
	 * Get version ID provided by the region
	 * Combine the namespace with the ID and version to get a globally unique ID.
	 * 
	 * @return ID as string
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Set version ID provided by the region
	 * 
	 * @param version 
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Convenience method
	 * 
	 * @return 
	 */
	public String getIDVersion() {
		String str = String.join("/", this.namespace, this.id, this.version);
		return str.length() > 2 ? str : "";
	}
	
	/**
	 * Constructor
	 */
	public BestObject() {
	}
}