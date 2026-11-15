// <copyright file="JdbcDriverLoader.java" company="Objectivity Bespoke Software Specialists">
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

public final class JdbcDriverLoader {

    private static final Logger log = Logger.getLogger(JdbcDriverLoader.class);
    private static URLClassLoader driverClassLoader;

    private JdbcDriverLoader() {
    }

    public static void loadDrivers() throws IOException {
        Path applicationLocation;
        try {
            applicationLocation = Paths.get(
                    JdbcDriverLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IOException("Unable to determine application location", e);
        }
        Path applicationDirectory = Files.isDirectory(applicationLocation)
                ? Paths.get(System.getProperty("user.dir"))
                : applicationLocation.getParent();
        Path driverDirectory = applicationDirectory.resolve("jdbc_drivers").toAbsolutePath().normalize();
        if (!Files.isDirectory(driverDirectory)) {
            log.warn("JDBC driver directory does not exist: " + driverDirectory);
            return;
        }

        List<URL> driverJars = new ArrayList<>();
        try (Stream<Path> paths = Files.list(driverDirectory)) {
            Path[] jars = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                    .sorted()
                    .toArray(Path[]::new);
            for (Path jar : jars) {
                driverJars.add(jar.toUri().toURL());
                log.debug("Found JDBC driver JAR: " + jar);
            }
        }

        if (driverJars.isEmpty()) {
            log.warn("No JDBC driver JARs found in: " + driverDirectory);
            return;
        }

        driverClassLoader = new URLClassLoader(
                driverJars.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(driverClassLoader);
        for (Driver driver : ServiceLoader.load(Driver.class, driverClassLoader)) {
            try {
                DriverManager.registerDriver(new DriverShim(driver));
                log.debug("Registered JDBC driver: " + driver.getClass().getName());
            } catch (SQLException e) {
                throw new IOException("Unable to register JDBC driver " + driver.getClass().getName(), e);
            }
        }
        log.info("Loaded " + driverJars.size() + " JDBC driver JAR(s) from " + driverDirectory);
    }

    private static final class DriverShim implements Driver {

        private final Driver delegate;

        private DriverShim(Driver delegate) {
            this.delegate = delegate;
        }

        @Override
        public java.sql.Connection connect(String url, Properties info) throws SQLException {
            return delegate.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return delegate.acceptsURL(url);
        }

        @Override
        public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return delegate.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return delegate.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return delegate.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return delegate.jdbcCompliant();
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return delegate.getParentLogger();
        }
    }
}
