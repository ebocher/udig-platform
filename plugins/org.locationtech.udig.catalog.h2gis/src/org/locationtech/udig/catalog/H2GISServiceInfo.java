/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package org.locationtech.udig.catalog;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.locationtech.udig.catalog.internal.h2gis.H2GISPlugin;

/**
 * The service info for the H2GISService
 * 
 * @author jesse
 * @since 1.1.0
 */
public class H2GISServiceInfo extends IServiceInfo {

    private H2GISService2 service;
    public H2GISServiceInfo( H2GISService2 service ) throws IOException {
        this.service = service;
        List<String> tmpKeywords = new ArrayList<String>();
        tmpKeywords.add("H2GIS"); //$NON-NLS-1$
        
        List<H2GISGeoResource2> resources = service.resources(new NullProgressMonitor());
        for( H2GISGeoResource2 h2gisGeoResource2 : resources ) {
            tmpKeywords.add(h2gisGeoResource2.typename);
        }
        keywords = tmpKeywords.toArray(new String[0]);
        
        try {
            schema = new URI("jdbc://h2gis/gml"); //$NON-NLS-1$
        } catch (URISyntaxException e) {
            H2GISPlugin.log(null, e);
        }
        
        icon = AbstractUIPlugin.imageDescriptorFromPlugin(H2GISPlugin.ID,
            "icons/obj16/h2gis_16.gif"); //$NON-NLS-1$
    }

    public String getDescription() {
        return service.getIdentifier().toString();
    }

    public URI getSource() {
        try {
            return service.getIdentifier().toURI();
        } catch (URISyntaxException e) {
            // This would be bad 
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
    }

    public String getTitle() {
    	return service.getTitle();
    }

}
