/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.cf.objects;

import org.eclipse.orion.server.core.ProtocolConstants;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.orion.server.core.resources.*;
import org.eclipse.orion.server.core.resources.annotations.PropertyDescription;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class CFObject {

	protected Serializer<JSONObject> jsonSerializer;

	private static final ResourceShape DEFAULT_RESOURCE_SHAPE = new ResourceShape();
	{
		Property[] defaultProperties = new Property[] {new Property(ProtocolConstants.KEY_LOCATION)};
		DEFAULT_RESOURCE_SHAPE.setProperties(defaultProperties);
	}

	protected CFObject() {
		this.jsonSerializer = new JSONSerializer();
	}

	@PropertyDescription(name = ProtocolConstants.KEY_LOCATION)
	abstract protected URI getLocation() throws URISyntaxException;

	public abstract JSONObject toJSON() throws JSONException;
}
