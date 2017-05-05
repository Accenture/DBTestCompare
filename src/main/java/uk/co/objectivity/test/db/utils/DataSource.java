// <copyright file="DataSource.java" company="Objectivity Bespoke Software Specialists">
// Copyright (c) Objectivity Bespoke Software Specialists. All rights reserved.
// </copyright>
// <license>
//     The MIT License (MIT)
//     Permission is hereby granted, free of charge, to any person obtaining a copy
//     of this software and associated documentation files (the "Software"), to deal
//     in the Software without restriction, including without limitation the rights
//     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//     copies of the Software, and to permit persons to whom the Software is
//     furnished to do so, subject to the following conditions:
//     The above copyright notice and this permission notice shall be included in all
//     copies or substantial portions of the Software.
//     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//     SOFTWARE.
// </license>

package uk.co.objectivity.test.db.utils;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import uk.co.objectivity.test.db.beans.xml.Datasource;
import uk.co.objectivity.test.db.beans.xml.Datasources;

public class DataSource {

    private final static Logger log = Logger.getLogger(DataSource.class);

    private static final Map<String, ComboPooledDataSource> DATASOURCES_MAP = new HashMap<>();

    public static void init(Datasources datasources) {
        if (datasources == null) {
            log.error("Datasources are not set (or misconfigured)");
            return;
        }
        log.debug("Adding shutdown hook (closing connections)...");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                DataSource.closeAllConnections();
            }
        });
        log.debug("Initializing data source connection pools...");
        Iterator<Datasource> it = datasources.getDatasources().iterator();
        while (it.hasNext()) {
            Datasource datasource = it.next();
            Integer maxPoolSize = datasource.getMaxPollSize();
            log.debug("Max Pool Size:" + maxPoolSize + " for datasource: " + datasource.getName());
            ComboPooledDataSource cpds = new ComboPooledDataSource();
            cpds.setJdbcUrl(datasource.getUrl());
            cpds.setUser(datasource.getUser());
            cpds.setPassword(datasource.getPassword());

            cpds.setAcquireRetryAttempts(3); // default is 30
            cpds.setAcquireRetryDelay(10000);

            cpds.setInitialPoolSize(1);
            cpds.setMinPoolSize(1);
            cpds.setAcquireIncrement(1);
            if (maxPoolSize == null || maxPoolSize == 0 || maxPoolSize > 500) {
                log.warn("Poll size for '" + datasource.getName() + "' is incorrect (allowed 1-500). It will be set" +
                        " to 1.");
                cpds.setMaxPoolSize(1);
            } else {
                cpds.setMaxPoolSize(maxPoolSize);
            }

            try {
                cpds.setDriverClass(datasource.getDriver());
                DATASOURCES_MAP.put(datasource.getName(), cpds);
            } catch (PropertyVetoException e) {
                // we only log it but not quit application (even it is misconfigured) because we want to run all test
                // anyway and mark them as failed (instead of just one "silent" exception in logs). They will fail
                // while trying to getConnection (see method below).
                log.error(e);
            }
        }
        log.debug("Max Pool Size for each DataSource should be >= Threads");
    }

    public static Connection getConnection(String datasourceName) throws SQLException, NoSuchElementException {
        if (!DATASOURCES_MAP.containsKey(datasourceName)) {
            throw new NoSuchElementException("Datasource '" + datasourceName + "' is probably misconfigured!");
        }
        return DATASOURCES_MAP.get(datasourceName).getConnection();
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.warn(e);
            }
        }
    }

    public static void closeAllConnections() {
        log.info("Releasing connections ...");
        Iterator<ComboPooledDataSource> it = DATASOURCES_MAP.values().iterator();
        while (it.hasNext()) {
            it.next().close();
        }
    }

}
