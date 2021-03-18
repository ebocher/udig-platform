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
package org.locationtech.udig.catalog;

import static java.text.MessageFormat.format;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.locationtech.udig.catalog.h2gis.internal.Messages;
import org.locationtech.udig.catalog.internal.h2gis.H2GISPlugin;
import org.locationtech.udig.catalog.internal.h2gis.ui.H2GISServiceDialect;
import org.locationtech.udig.core.Pair;

import org.eclipse.core.runtime.Platform;
import org.geotools.data.DataStoreFactorySpi;
import org.h2gis.geotools.H2GISDataStoreFactory;
import org.geotools.data.DataAccessFactory.Param;
import static org.h2gis.geotools.H2GISDataStoreFactory.*;
import static org.geotools.jdbc.JDBCDataStoreFactory.DBTYPE;

/**
 * H2GIS ServiceExtension that has a hierarchy. It represents a Database and has folders within
 * it. One for each Schema that is known. The params object is the same as a normal H2GIS except
 * that the schema parameter can be a list of comma separated string.
 * 
 * @author Jesse Eichar, Refractions Research
 * @since 1.2
 */
public class H2GISServiceExtension2 extends AbstractDataStoreServiceExtension
        implements
            ServiceExtension2 {
    /** Constant to use with DBTYPE */
    public static final String TYPE = (String)DBTYPE.sample;
    /** Key used to test connection */
    private static final String IN_TESTING = "testing"; //$NON-NLS-1$
    /**
     * Common SQL functionalty needed for the user interface
     */
    public static final H2GISServiceDialect DIALECT = new H2GISServiceDialect();

    public H2GISService2 createService( URL id, Map<String, Serializable> params ) {
        try {
            if (getFactory() == null || !getFactory().isAvailable()) {
                return null; // factory not available
            }
            if (!getFactory().canProcess(params)) {
                return null; // the factory cannot use these parameters
            }
        } catch (Exception unexpected) {
            if (Platform.inDevelopmentMode()) {
                // this should never happen
                H2GISPlugin.log("H2GISExtension canProcess errored out with: " + unexpected,
                        unexpected);
            }
            return null; // the factory cannot really use these parameters
        }
        if (reasonForFailure(params) != null) {
            return null;
        }
        Map<String, Serializable> params2 = params;

        ensurePortIsInt(params2);

        try {
            URL finalID = DIALECT.toURL(params2);
            Pair<Map<String, Serializable>, String> split = processParams(params2);
            if (split.getRight() != null) {
                return null;
            }

            return new H2GISService2(finalID, split.getLeft());
        } catch (MalformedURLException e) {
            H2GISPlugin.log("Unable to construct proper service URL.", e); //$NON-NLS-1$
            return null;
        }

    }

    private void ensurePortIsInt( Map<String, Serializable> params ) {
        if (params != null && params.containsKey(PORT.key)
                && params.get(PORT.key) instanceof String) {
            int val = new Integer((String) params.get(PORT.key));
            params.put(PORT.key, val);
        }
    }

    /**
     * This is a guess ...
     */
    public Map<String, Serializable> createParams( URL url ) {
        if (!isH2GIS(url)) {
            return null;
        }

        ParamInfo info = parseParamInfo(url);

        Map<String, Serializable> h2GISParams = new HashMap<String, Serializable>();
        h2GISParams.put(DBTYPE.key, (Serializable)DBTYPE.sample); // dbtype //$NON-NLS-1$
        h2GISParams.put(USER.key, info.username); // user
        h2GISParams.put(PASSWD.key, info.password); // pass
        h2GISParams.put(HOST.key, info.host); // host
        h2GISParams.put(DATABASE.key, info.the_database); // database
        h2GISParams.put(PORT.key, info.the_port); // port
        h2GISParams.put(SCHEMA.key, info.the_schema); // database

        return h2GISParams;
    }

    private static H2GISDataStoreFactory factory;

    public synchronized static H2GISDataStoreFactory getFactory() {
        if (factory == null) {
            factory = new H2GISDataStoreFactory();
            // TODO look up in factory SPI in order to avoid
            // duplicate instances
        }
        return factory;
    }
    /**
     * Look up Param by key; used to access the correct sample
     * value for DBTYPE.
     *
     * @param key
     * @return
     */
    public static Param getPram( String key ){
        for( Param param : getFactory().getParametersInfo()){
            if( key.equals( param.key )){
                return param;
            }
        }
        return null;
    }

    /** A couple quick checks on the url */
    public static final boolean isH2GIS( URL url ) {
        if (url == null) {
            return false;
        }
        return url.getProtocol().toLowerCase().equals("h2gis") || url.getProtocol().toLowerCase().equals("h2gis.jdbc") || //$NON-NLS-1$ //$NON-NLS-2$
                url.getProtocol().toLowerCase().equals("jdbc.h2gis"); //$NON-NLS-1$
    }

    public String reasonForFailure( URL url ) {
        if (!isH2GIS(url))
            return Messages.H2GISServiceExtension_badURL;
        return reasonForFailure(createParams(url));
    }

    @Override
    protected String doOtherChecks( Map<String, Serializable> params ) {
        if (!"h2gis".equals(params.get(DBTYPE.key))) {
            return format("Parameter DBTYPE is required to be \"{0}\"", DBTYPE.sample);
        }

        // if the testing parameter is in params then this is
        // a recursive call originating in processParams and should be shorted
        // to prevent infinate loop.
        if (params.containsKey(IN_TESTING)) {
            return null;
        }

        Pair<Map<String, Serializable>, String> resultOfSplit = processParams(params);
        if (resultOfSplit.getRight() != null) {
            String reason = resultOfSplit.getRight();
            return reason;
        }
        return null;
    }

    private Pair<Map<String, Serializable>, String> processParams( Map<String, Serializable> params ) {
        String schemasString = (String) params.get(SCHEMA.key);

        Set<String> goodSchemas = new HashSet<String>();

        HashMap<String, Serializable> testedParams = new HashMap<String, Serializable>(params);
        testedParams.put(SCHEMA.key, "public"); //$NON-NLS-1$
        testedParams.put(IN_TESTING, true);
        String reason = super.reasonForFailure(testedParams);

        if (reason == null) {
            goodSchemas.add("public"); //$NON-NLS-1$
        }

        String[] schemas = schemasString.split(","); //$NON-NLS-1$

        for( String string : schemas ) {
            if (!goodSchemas.contains(string)) {
                testedParams = new HashMap<String, Serializable>(testedParams);
                String trimmedSchema = string.trim();
                testedParams.put(SCHEMA.key, trimmedSchema);

                String reasonForFailure = super.reasonForFailure(testedParams);
                if (reasonForFailure == null) {
                    goodSchemas.add(string);
                } else {
                    reason = reasonForFailure;
                }
            }
        }

        if (!goodSchemas.isEmpty()) {
            testedParams.put(SCHEMA.key, combineSchemaStrings(goodSchemas));
        }

        testedParams.remove(IN_TESTING);

        Pair<Map<String, Serializable>, String> result;
        result = new Pair<Map<String, Serializable>, String>(testedParams, reason);
        return result;
    }

    @Override
    protected DataStoreFactorySpi getDataStoreFactory() {
        return getFactory();
    };

    private Serializable combineSchemaStrings( Set<String> goodSchemas ) {
        StringBuilder builder = new StringBuilder();
        for( String string : goodSchemas ) {
            if (builder.length() > 0) {
                builder.append(',');
            }

            builder.append(string);
        }

        return builder.toString();
    }

}
