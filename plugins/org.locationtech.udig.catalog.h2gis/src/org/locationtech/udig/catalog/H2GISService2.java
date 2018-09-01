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

import static org.h2gis.geotools.H2GISDataStoreFactory.PORT;
import static org.h2gis.geotools.H2GISDataStoreFactory.SCHEMA;
import static org.geotools.jdbc.JDBCDataStoreFactory.DATABASE;
import static org.geotools.jdbc.JDBCDataStoreFactory.HOST;
import static org.geotools.jdbc.JDBCDataStoreFactory.PASSWD;
import static org.geotools.jdbc.JDBCDataStoreFactory.USER;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.locationtech.udig.catalog.internal.h2gis.ui.H2GISLookUpSchemaRunnable;
import org.locationtech.udig.catalog.service.database.TableDescriptor;
import org.locationtech.udig.ui.UDIGDisplaySafeLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A H2GIS service that represents the database. Its children are "folders" that each resolve to a
 * H2GISDatastore. Each folder has georesources
 * 
 * @author jesse
 * @since 1.1.0
 */
public class H2GISService2 extends IService {

    private final URL id;
    private Map<String, Serializable> params;
    private Status status;
    private final List<IResolve> members = new ArrayList<IResolve>();
    private Lock lock = new UDIGDisplaySafeLock();
    private Throwable message;

    public H2GISService2( URL finalID, Map<String, Serializable> map ) {
        this.id = finalID;
        this.params = new HashMap<String, Serializable>(map);
        status = Status.NOTCONNECTED;
    }
    
    @Override
    public String getTitle() {
        URL id = getIdentifier();
        return ("H2GIS " +id.getHost()+ "/" +id.getPath()).replaceAll("//","/"); //$NON-NLS-1$
    }

    @Override
    public Map<String, Serializable> getConnectionParams() {
        if (members.isEmpty()) {
            return params;
        }
        StringBuilder builder = new StringBuilder();
        for( IResolve member : members ) {
            if (builder.length() > 0) {
                builder.append(","); //$NON-NLS-1$
            }

            H2GISSchemaFolder folder = (H2GISSchemaFolder) member;

            builder.append(folder.getSchemaName());
        }
        params.put(SCHEMA.key, builder.toString());

        // for wkt for a moment!
        // params.put( H2GISDataStoreFactory.WKBENABLED.key, Boolean.FALSE );

        //params.put(LOOSEBBOX.key, Boolean.TRUE);
        return params;
    }

    @Override
    public H2GISServiceInfo getInfo( IProgressMonitor monitor ) throws IOException {
        return (H2GISServiceInfo) super.getInfo(monitor);
    }
    protected H2GISServiceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        // make sure members are loaded cause they're needed for info
        members(monitor);
        return new H2GISServiceInfo(this);
    }

    @Override
    public List<H2GISGeoResource2> resources( IProgressMonitor monitor ) throws IOException {
        List<IResolve> resolves = members(monitor);
        List<H2GISGeoResource2> resources = new ArrayList<H2GISGeoResource2>();

        for( IResolve resolve : resolves ) {
            List<IResolve> folderChildren = resolve.members(monitor);
            for( IResolve resolve2 : folderChildren ) {
                resources.add((H2GISGeoResource2) resolve2);
            }
        }
        return resources;
    }

    @Override
    public List<IResolve> members( IProgressMonitor monitor ) throws IOException {
        lock.lock();
        try {
            if (members.isEmpty()) {
                Map<String, Collection<TableDescriptor>> schemas = lookupSchemasInDB(SubMonitor.convert(monitor,
                        "looking up schemas", 1));
                if (schemas == null) {
                    // couldn't look up schema so...
                    String commaSeperated = (String) params.get(SCHEMA.key);
                    String[] schemaNames = commaSeperated.split(","); //$NON-NLS-1$
                    schemas = new HashMap<String, Collection<TableDescriptor>>();
                    for (String name : schemaNames) {
                        schemas.put(name, Collections.<TableDescriptor>emptyList());
                    }
                }
                createSchemaFolder(schemas);
                message = null;
                status = Status.CONNECTED;
            }
            return Collections.unmodifiableList(members);
        } finally {
            lock.unlock();
        }
    }

    private Map<String, Collection<TableDescriptor>> lookupSchemasInDB( IProgressMonitor monitor ) {
        String host = (String) params.get(HOST.key);
        Integer port = (Integer) params.get(PORT.key);
        String database = (String) params.get(DATABASE.key);
        String user = (String) params.get(USER.key);
        String pass = (String) params.get(PASSWD.key);

        H2GISLookUpSchemaRunnable runnable = new H2GISLookUpSchemaRunnable(host, port, user,
                pass, database);
        runnable.run(monitor);

        if (runnable.getError() != null) {
            message = new Exception(runnable.getError());
            status = Status.BROKEN;
            return null;
        }
        Set<TableDescriptor> tables = runnable.getTableDescriptors();
        Multimap<String, TableDescriptor> schemas = HashMultimap.create();
        for( TableDescriptor schema : tables ) {
            schemas.put(schema.schema,schema);
        }
        return schemas.asMap();
    }

    private void createSchemaFolder( Map<String, Collection<TableDescriptor>> schemas ) {

        for( Map.Entry<String, Collection<TableDescriptor>> schema : schemas.entrySet() ) {
            String trimmed = schema.getKey().trim();
            if (trimmed.length() == 0) {
                continue;
            }

            members.add(new H2GISSchemaFolder(this, trimmed,schema.getValue()));
        }
    }

    public URL getIdentifier() {
        return id;
    }

    public Throwable getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public void dispose( IProgressMonitor monitor ) {
        super.dispose( monitor ); // responsible for calling dispose on each folder
//        for( IResolve folder : members ) {
//            folder.dispose(monitor);
//        }
//        status = Status.NOTCONNECTED;
        status = Status.DISPOSED;
    }
}
