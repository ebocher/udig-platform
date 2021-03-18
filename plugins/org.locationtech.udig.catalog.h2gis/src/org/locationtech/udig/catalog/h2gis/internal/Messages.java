/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 *
 */
package org.locationtech.udig.catalog.h2gis.internal;

import org.eclipse.osgi.util.NLS;
import org.locationtech.udig.catalog.h2gis.internal.Messages;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.locationtech.udig.catalog.h2gis.internal.messages"; //$NON-NLS-1$

	public static String H2GISServiceExtension_badURL;

    public static String H2GISWizardPage_0;
	public static String H2GISWizardPage_title;
	public static String H2GISWizardPage_button_looseBBox_tooltip;
	public static String H2GISWizardPage_button_looseBBox_text;
	public static String H2GISWizardPage_button_wkb_tooltip;
	public static String H2GISWizardPage_button_wkb_text;
	public static String H2GISGeoResource_error_layer_bounds;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
