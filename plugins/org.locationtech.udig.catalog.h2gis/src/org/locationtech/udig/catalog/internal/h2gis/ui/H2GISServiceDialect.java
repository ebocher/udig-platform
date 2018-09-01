/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2012, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package org.locationtech.udig.catalog.internal.h2gis.ui;
import static org.h2gis.geotools.H2GISDataStoreFactory.*;

import java.util.Map;

import org.locationtech.udig.catalog.H2GISServiceExtension2;
import org.locationtech.udig.catalog.internal.h2gis.H2GISPlugin;
import org.locationtech.udig.catalog.internal.h2gis.ui.SQLTab;
import org.locationtech.udig.catalog.service.database.DataConnectionPage;
import org.locationtech.udig.catalog.service.database.DatabaseConnectionRunnable;
import org.locationtech.udig.catalog.service.database.DatabaseServiceDialect;
import org.locationtech.udig.catalog.service.database.DatabaseWizardLocalization;
import org.locationtech.udig.catalog.service.database.LookUpSchemaRunnable;
import org.locationtech.udig.catalog.service.database.Tab;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Describes the H2GIS parameters for creating the H2GIS DataStore and ServiceExtension
 * 
 * @author jeichar
 */
public class H2GISServiceDialect extends DatabaseServiceDialect {

    public H2GISServiceDialect() {
        super(SCHEMA, DATABASE, HOST, PORT, USER, PASSWD, H2GISServiceExtension2
                .getPram(DBTYPE.key), "h2gis", "jdbc.h2gis", new DatabaseWizardLocalization()); //$NON-NLS-1$

    }
    @Override
    public IDialogSettings getDialogSetting() {
        return H2GISPlugin.getDefault().getDialogSettings();
    }

    @Override
    public void log( String message, Throwable e ) {
        H2GISPlugin.log(message, e);
    }

    @Override
    public DatabaseConnectionRunnable createDatabaseConnectionRunnable( String host, int port,
            String username, String password ) {
        return new H2GISDatabaseConnectionRunnable(host, port, username, password);
    }

    @Override
    public Map<Control, Tab> createOptionConnectionPageTabs( TabFolder tabFolder,
            DataConnectionPage containingPage ) {
        Map<Control, Tab> tabs = super.createOptionConnectionPageTabs(tabFolder, containingPage);
        //addSQLTab(tabFolder, containingPage, tabs);
        return tabs;
    }

    private void addSQLTab( TabFolder tabFolder, DataConnectionPage containingPage,
            Map<Control, Tab> tabs ) {
        SQLTab sqlComposite = new SQLTab(getDialogSetting());

        sqlComposite.setWizard(containingPage.getWizard());
        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText("SQL"); //$NON-NLS-1$
        item.setControl(sqlComposite.createControl(tabFolder, SWT.NONE));
        tabs.put(item.getControl(), sqlComposite);
    }

    @Override
    public LookUpSchemaRunnable createLookupSchemaRunnable( String host, int port, String username,
            String password, String database ) {
        return new H2GISLookUpSchemaRunnable(host, port, username, password, database);
    }

}
