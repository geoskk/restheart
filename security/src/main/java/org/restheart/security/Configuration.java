/*
 * RESTHeart Security
 * 
 * Copyright (C) SoftInstigate Srl
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.restheart.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.util.LinkedHashMap;
import static org.restheart.security.ConfigurationKeys.AJP_HOST_KEY;
import static org.restheart.security.ConfigurationKeys.AJP_LISTENER_KEY;
import static org.restheart.security.ConfigurationKeys.AJP_PORT_KEY;
import static org.restheart.security.ConfigurationKeys.ALLOW_UNESCAPED_CHARACTERS_IN_URL;
import static org.restheart.security.ConfigurationKeys.ANSI_CONSOLE_KEY;
import static org.restheart.security.ConfigurationKeys.AUTHENTICATORS_KEY;
import static org.restheart.security.ConfigurationKeys.AUTHORIZERS_KEY;
import static org.restheart.security.ConfigurationKeys.AUTH_MECHANISMS_KEY;
import static org.restheart.security.ConfigurationKeys.AUTH_TOKEN;
import static org.restheart.security.ConfigurationKeys.BUFFER_SIZE_KEY;
import static org.restheart.security.ConfigurationKeys.CERT_PASSWORD_KEY;
import static org.restheart.security.ConfigurationKeys.CONNECTION_OPTIONS_KEY;
import static org.restheart.security.ConfigurationKeys.DEFAULT_AJP_HOST;
import static org.restheart.security.ConfigurationKeys.DEFAULT_AJP_PORT;
import static org.restheart.security.ConfigurationKeys.DEFAULT_HTTPS_HOST;
import static org.restheart.security.ConfigurationKeys.DEFAULT_HTTPS_PORT;
import static org.restheart.security.ConfigurationKeys.DEFAULT_HTTP_HOST;
import static org.restheart.security.ConfigurationKeys.DEFAULT_HTTP_PORT;
import static org.restheart.security.ConfigurationKeys.DEFAULT_INSTANCE_NAME;
import static org.restheart.security.ConfigurationKeys.DIRECT_BUFFERS_KEY;
import static org.restheart.security.ConfigurationKeys.ENABLE_LOG_CONSOLE_KEY;
import static org.restheart.security.ConfigurationKeys.ENABLE_LOG_FILE_KEY;
import static org.restheart.security.ConfigurationKeys.FORCE_GZIP_ENCODING_KEY;
import static org.restheart.security.ConfigurationKeys.HTTPS_HOST_KEY;
import static org.restheart.security.ConfigurationKeys.HTTPS_LISTENER;
import static org.restheart.security.ConfigurationKeys.HTTPS_PORT_KEY;
import static org.restheart.security.ConfigurationKeys.HTTP_HOST_KEY;
import static org.restheart.security.ConfigurationKeys.HTTP_LISTENER_KEY;
import static org.restheart.security.ConfigurationKeys.HTTP_PORT_KEY;
import static org.restheart.security.ConfigurationKeys.INSTANCE_NAME_KEY;
import static org.restheart.security.ConfigurationKeys.IO_THREADS_KEY;
import static org.restheart.security.ConfigurationKeys.KEYSTORE_FILE_KEY;
import static org.restheart.security.ConfigurationKeys.KEYSTORE_PASSWORD_KEY;
import static org.restheart.security.ConfigurationKeys.LOG_FILE_PATH_KEY;
import static org.restheart.security.ConfigurationKeys.LOG_LEVEL_KEY;
import static org.restheart.security.ConfigurationKeys.LOG_REQUESTS_LEVEL_KEY;
import static org.restheart.security.ConfigurationKeys.PLUGINS_ARGS_KEY;
import static org.restheart.security.ConfigurationKeys.PROXY_KEY;
import static org.restheart.security.ConfigurationKeys.REQUESTS_LIMIT_KEY;
import static org.restheart.security.ConfigurationKeys.SERVICES_KEY;
import static org.restheart.security.ConfigurationKeys.USE_EMBEDDED_KEYSTORE_KEY;
import static org.restheart.security.ConfigurationKeys.WORKER_THREADS_KEY;
import org.restheart.security.utils.URLUtils;

/**
 * Utility class to help dealing with the configuration file.
 *
 * @author Andrea Di Cesare {@literal <andrea@softinstigate.com>}
 */
public class Configuration {

    /**
     * the version is read from the JAR's MANIFEST.MF file, which is
     * automatically generated by the Maven build process
     */
    public static final String VERSION = Configuration.class.getPackage()
            .getImplementationVersion() == null
                    ? "unknown, not packaged"
                    : Configuration.class.getPackage().getImplementationVersion();

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    public static final String DEFAULT_ROUTE = "0.0.0.0";

    private boolean silent = false;
    private final boolean httpsListener;
    private final int httpsPort;
    private final String httpsHost;
    private final boolean httpListener;
    private final int httpPort;
    private final String httpHost;
    private final boolean ajpListener;
    private final int ajpPort;
    private final String ajpHost;
    private final String instanceName;
    private final boolean useEmbeddedKeystore;
    private final String keystoreFile;
    private final String keystorePassword;
    private final String certPassword;
    private final List<Map<String, Object>> proxies;
    private final Map<String, Map<String, Object>> pluginsArgs;
    private final List<Map<String, Object>> services;
    private final List<Map<String, Object>> authMechanisms;
    private final List<Map<String, Object>> authenticators;
    private final Map<String, Object> authorizers;
    private final Map<String, Object> tokenManager;
    private final String logFilePath;
    private final Level logLevel;
    private final boolean logToConsole;
    private final boolean logToFile;
    private final int requestsLimit;
    private final int ioThreads;
    private final int workerThreads;
    private final int bufferSize;
    private final boolean directBuffers;
    private final boolean forceGzipEncoding;
    private final Map<String, Object> connectionOptions;
    private final Integer logExchangeDump;
    private final boolean ansiConsole;
    private final boolean allowUnescapedCharactersInUrl;

    /**
     * Creates a new instance of Configuration with defaults values.
     */
    public Configuration() {
        ansiConsole = true;

        httpsListener = true;
        httpsPort = DEFAULT_HTTPS_PORT;
        httpsHost = DEFAULT_HTTPS_HOST;

        httpListener = true;
        httpPort = DEFAULT_HTTP_PORT;
        httpHost = DEFAULT_HTTP_HOST;

        ajpListener = false;
        ajpPort = DEFAULT_AJP_PORT;
        ajpHost = DEFAULT_AJP_HOST;

        instanceName = DEFAULT_INSTANCE_NAME;

        useEmbeddedKeystore = true;
        keystoreFile = null;
        keystorePassword = null;
        certPassword = null;

        proxies = new ArrayList<>();
        
        pluginsArgs = new LinkedHashMap<>();

        services = new ArrayList<>();

        authMechanisms = new ArrayList<>();

        authenticators = new ArrayList<>();

        authorizers = null;

        tokenManager = new HashMap<>();

        logFilePath = URLUtils.removeTrailingSlashes(System.getProperty("java.io.tmpdir"))
                .concat(File.separator + "restheart-security.log");

        logToConsole = true;
        logToFile = true;
        logLevel = Level.INFO;

        requestsLimit = 100;

        ioThreads = 2;
        workerThreads = 32;
        bufferSize = 16384;
        directBuffers = true;

        forceGzipEncoding = false;

        logExchangeDump = 0;

        connectionOptions = Maps.newHashMap();

        allowUnescapedCharactersInUrl = true;
    }
    
    /**
     * Creates a new instance of Configuration from the configuration file For
     * any missing property the default value is used.
     *
     * @param confFilePath the path of the configuration file
     * @throws org.restheart.security.ConfigurationException
     */
    public Configuration(final Path confFilePath) throws ConfigurationException {
        this(confFilePath, false);
    }

    /**
     * Creates a new instance of Configuration from the configuration file For
     * any missing property the default value is used.
     *
     * @param confFilePath the path of the configuration file
     * @param silent
     * @throws org.restheart.security.ConfigurationException
     */
    public Configuration(final Path confFilePath, boolean silent) throws ConfigurationException {
        this(getConfigurationFromFile(confFilePath), silent);
    }

    /**
     * Creates a new instance of Configuration from the configuration file For
     * any missing property the default value is used.
     *
     * @param conf the key-value configuration map
     * @param silent
     * @throws org.restheart.security.ConfigurationException
     */
    public Configuration(Map<String, Object> conf, boolean silent) throws ConfigurationException {
        this.silent = silent;

        ansiConsole = getOrDefault(conf, ANSI_CONSOLE_KEY, true);

        httpsListener = getOrDefault(conf, HTTPS_LISTENER, true);
        httpsPort = getOrDefault(conf, HTTPS_PORT_KEY, DEFAULT_HTTPS_PORT);
        httpsHost = getOrDefault(conf, HTTPS_HOST_KEY, DEFAULT_HTTPS_HOST);

        httpListener = getOrDefault(conf, HTTP_LISTENER_KEY, false);
        httpPort = getOrDefault(conf, HTTP_PORT_KEY, DEFAULT_HTTP_PORT);
        httpHost = getOrDefault(conf, HTTP_HOST_KEY, DEFAULT_HTTP_HOST);

        ajpListener = getOrDefault(conf, AJP_LISTENER_KEY, false);
        ajpPort = getOrDefault(conf, AJP_PORT_KEY, DEFAULT_AJP_PORT);
        ajpHost = getOrDefault(conf, AJP_HOST_KEY, DEFAULT_AJP_HOST);

        instanceName = getOrDefault(conf, INSTANCE_NAME_KEY, DEFAULT_INSTANCE_NAME);

        useEmbeddedKeystore = getOrDefault(conf, USE_EMBEDDED_KEYSTORE_KEY, true);
        keystoreFile = getOrDefault(conf, KEYSTORE_FILE_KEY, null);
        keystorePassword = getOrDefault(conf, KEYSTORE_PASSWORD_KEY, null);
        certPassword = getOrDefault(conf, CERT_PASSWORD_KEY, null);

        proxies = getAsListOfMaps(conf, PROXY_KEY, new ArrayList<>());
        
        pluginsArgs = getAsMapOfMaps(conf, PLUGINS_ARGS_KEY, new LinkedHashMap<>());

        services = getAsListOfMaps(conf, SERVICES_KEY, new ArrayList<>());

        authMechanisms = getAsListOfMaps(conf, AUTH_MECHANISMS_KEY, new ArrayList<>());

        authenticators = getAsListOfMaps(conf, AUTHENTICATORS_KEY, new ArrayList<>());

        authorizers = getAsMap(conf, AUTHORIZERS_KEY);

        tokenManager = getAsMap(conf, AUTH_TOKEN);

        logFilePath = getOrDefault(conf, LOG_FILE_PATH_KEY, URLUtils
                .removeTrailingSlashes(System.getProperty("java.io.tmpdir")).concat(File.separator + "restheart-security.log"));
        String _logLevel = getOrDefault(conf, LOG_LEVEL_KEY, "INFO");
        logToConsole = getOrDefault(conf, ENABLE_LOG_CONSOLE_KEY, true);
        logToFile = getOrDefault(conf, ENABLE_LOG_FILE_KEY, true);

        Level level;

        try {
            level = Level.valueOf(_logLevel);
        } catch (Exception e) {
            if (!silent) {
                LOGGER.info("wrong value for parameter {}: {}. using its default value {}", "log-level", _logLevel,
                        "INFO");
            }
            level = Level.INFO;
        }

        logLevel = level;

        requestsLimit = getOrDefault(conf, REQUESTS_LIMIT_KEY, 100);

        ioThreads = getOrDefault(conf, IO_THREADS_KEY, 2);
        workerThreads = getOrDefault(conf, WORKER_THREADS_KEY, 32);
        bufferSize = getOrDefault(conf, BUFFER_SIZE_KEY, 16384);
        directBuffers = getOrDefault(conf, DIRECT_BUFFERS_KEY, true);

        forceGzipEncoding = getOrDefault(conf, FORCE_GZIP_ENCODING_KEY, false);

        logExchangeDump = getOrDefault(conf, LOG_REQUESTS_LEVEL_KEY, 0);

        connectionOptions = getAsMap(conf, CONNECTION_OPTIONS_KEY);

        allowUnescapedCharactersInUrl = getOrDefault(conf, ALLOW_UNESCAPED_CHARACTERS_IN_URL, true);
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getConfigurationFromFile(final Path confFilePath) throws ConfigurationException {
        Yaml yaml = new Yaml();

        Map<String, Object> conf = null;

        FileInputStream fis = null;

        try {
            fis = new FileInputStream(confFilePath.toFile());
            conf = (Map<String, Object>) yaml.load(fis);
        } catch (FileNotFoundException fne) {
            throw new ConfigurationException("configuration file not found", fne);
        } catch (Throwable t) {
            throw new ConfigurationException("error parsing the configuration file", t);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                    LOGGER.warn("Can't close the FileInputStream", ioe);
                }
            }
        }

        return conf;
    }

    /**
     *
     * @param integers
     * @return
     */
    public static int[] convertListToIntArray(List<Object> integers) {
        int[] ret = new int[integers.size()];
        Iterator<Object> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) {
            Object o = iterator.next();

            if (o instanceof Integer) {
                ret[i] = (Integer) o;
            } else {
                return new int[0];
            }
        }

        return ret;
    }

    @Override
    public String toString() {
        return "Configuration{" + "silent=" + silent + ", httpsListener=" + httpsListener + ", httpsPort=" + httpsPort
                + ", httpsHost=" + httpsHost + ", httpListener=" + httpListener + ", httpPort=" + httpPort
                + ", httpHost=" + httpHost + ", ajpListener=" + ajpListener + ", ajpPort=" + ajpPort + ", ajpHost="
                + ajpHost + ", instanceName=" + instanceName + ", useEmbeddedKeystore=" + useEmbeddedKeystore
                + ", keystoreFile=" + keystoreFile + ", keystorePassword=" + keystorePassword + ", certPassword="
                + certPassword + ", proxies=" + proxies + ", services=" + services + ", authMechanisms="
                + authMechanisms + ", authenticators=" + authenticators + ", authorizers=" + getAuthorizers() + ", logFilePath="
                + logFilePath + ", logLevel=" + logLevel + ", logToConsole=" + logToConsole + ", logToFile=" + logToFile
                + ", requestsLimit=" + requestsLimit + ", ioThreads=" + ioThreads + ", workerThreads=" + workerThreads
                + ", bufferSize=" + bufferSize + ", directBuffers=" + directBuffers + ", forceGzipEncoding="
                + forceGzipEncoding + ", authToken=" + tokenManager
                + ", connectionOptions=" + connectionOptions + ", logExchangeDump=" + logExchangeDump + ", ansiConsole="
                + ansiConsole + ", cursorBatchSize="
                + allowUnescapedCharactersInUrl + ", pluginsArgs=" + pluginsArgs + '}';
    }
    
    /**
     * @return the proxies
     */
    public List<Map<String, Object>> getProxies() {
        return proxies;
    }

    /**
     *
     * @return true if the Ansi console is enabled
     */
    public boolean isAnsiConsole() {
        return ansiConsole;
    }

    /**
     *
     * @param conf
     * @param key
     * @param defaultValue
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getAsListOfMaps(final Map<String, Object> conf, final String key,
            final List<Map<String, Object>> defaultValue) {
        if (conf == null) {
            if (!silent) {
                LOGGER.debug("parameters group {} not specified in the configuration file. using its default value {}",
                        key, defaultValue);
            }

            return defaultValue;
        }

        Object o = conf.get(key);

        if (o instanceof List) {
            return (List<Map<String, Object>>) o;
        } else {
            if (!silent) {
                LOGGER.debug("parameters group {} not specified in the configuration file, using its default value {}",
                        key, defaultValue);
            }
            return defaultValue;
        }
    }
    
    /**
     *
     * @param conf
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> getAsMapOfMaps(
            final Map<String, Object> conf,
            final String key,
            final Map<String, Map<String, Object>> defaultVal) {
        if (conf == null) {
            if (!silent) {
                LOGGER.debug("parameters {} not specified in the configuration file.", key);
            }
            return defaultVal;
        }

        Object o = conf.get(key);

        if (o instanceof Map) {
            try {
                return (Map<String, Map<String, Object>>) o;
            } catch (Throwable t) {
                LOGGER.warn("Invalid configuration parameter {}", key);
                return defaultVal;
            }
        } else {
            if (!silent) {
                LOGGER.debug("parameters {} not specified in the configuration file.", key);
            }
            return defaultVal;
        }
    }

    /**
     *
     * @param conf
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getAsMap(final Map<String, Object> conf, final String key) {
        if (conf == null) {
            if (!silent) {
                LOGGER.debug("parameters group {} not specified in the configuration file.", key);
            }
            return null;
        }

        Object o = conf.get(key);

        if (o instanceof Map) {
            return (Map<String, Object>) o;
        } else {
            if (!silent) {
                LOGGER.debug("parameters group {} not specified in the configuration file.", key);
            }
            return null;
        }
    }

    /**
     *
     * @param conf
     * @param key
     * @param defaultValue
     * @param silent
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <V extends Object> V getOrDefault(final Map<String, Object> conf, final String key, final V defaultValue, boolean silent) {
        if (conf == null || conf.get(key) == null) {
            // if default value is null there is no default value actually
            if (defaultValue != null && !silent) {
                LOGGER.debug("parameter {} not specified in the configuration file. using its default value {}", key,
                        defaultValue);
            }
            return defaultValue;
        }

        try {
            if (!silent) {
                LOGGER.debug("paramenter {} set to {}", key, conf.get(key));
            }
            return (V) conf.get(key);
        } catch (ClassCastException cce) {
            if (!silent) {
                LOGGER.warn("wrong value for parameter {}: {}. using its default value {}", key, conf.get(key),
                        defaultValue);
            }
            return defaultValue;
        }
    }

    /**
     *
     * @param conf
     * @param key
     * @param defaultValue
     * @return
     */
    private <V extends Object> V getOrDefault(final Map<String, Object> conf, final String key, final V defaultValue) {
        return getOrDefault(conf, key, defaultValue, this.silent);
    }

    /**
     * @return the httpsListener
     */
    public boolean isHttpsListener() {
        return httpsListener;
    }

    /**
     * @return the httpsPort
     */
    public int getHttpsPort() {
        return httpsPort;
    }

    /**
     * @return the httpsHost
     */
    public String getHttpsHost() {
        return httpsHost;
    }

    /**
     * @return the httpListener
     */
    public boolean isHttpListener() {
        return httpListener;
    }

    /**
     * @return the httpPort
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * @return the httpHost
     */
    public String getHttpHost() {
        return httpHost;
    }

    /**
     * @return the ajpListener
     */
    public boolean isAjpListener() {
        return ajpListener;
    }

    /**
     * @return the ajpPort
     */
    public int getAjpPort() {
        return ajpPort;
    }

    /**
     * @return the ajpHost
     */
    public String getAjpHost() {
        return ajpHost;
    }

    /**
     * @return the useEmbeddedKeystore
     */
    public boolean isUseEmbeddedKeystore() {
        return useEmbeddedKeystore;
    }

    /**
     * @return the keystoreFile
     */
    public String getKeystoreFile() {
        return keystoreFile;
    }

    /**
     * @return the keystorePassword
     */
    public String getKeystorePassword() {
        return keystorePassword;
    }

    /**
     * @return the certPassword
     */
    public String getCertPassword() {
        return certPassword;
    }

    /**
     * @return the logFilePath
     */
    public String getLogFilePath() {
        return logFilePath;
    }

    /**
     * @return the logLevel
     */
    public Level getLogLevel() {

        String logbackConfigurationFile = System.getProperty("logback.configurationFile");
        if (logbackConfigurationFile != null && !logbackConfigurationFile.isEmpty()) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger("org.restheart.security");
            return logger.getLevel();
        }

        return logLevel;
    }

    /**
     * @return the logToConsole
     */
    public boolean isLogToConsole() {
        return logToConsole;
    }

    /**
     * @return the logToFile
     */
    public boolean isLogToFile() {
        return logToFile;
    }

    /**
     * @return the ioThreads
     */
    public int getIoThreads() {
        return ioThreads;
    }

    /**
     * @return the workerThreads
     */
    public int getWorkerThreads() {
        return workerThreads;
    }

    /**
     * @return the bufferSize
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * @return the directBuffers
     */
    public boolean isDirectBuffers() {
        return directBuffers;
    }

    /**
     * @return the forceGzipEncoding
     */
    public boolean isForceGzipEncoding() {
        return forceGzipEncoding;
    }
    
    /**
     * @return the pluginsArgs
     */
    public Map<String, Map<String, Object>> getPluginsArgs() {
        return Collections.unmodifiableMap(pluginsArgs);
    }

    /**
     * @return the authMechanisms
     */
    public List<Map<String, Object>> getAuthMechanisms() {
        return authMechanisms;
    }

    /**
     * @return the authenticators
     */
    public List<Map<String, Object>> getAuthenticators() {
        return authenticators;
    }

    /**
     * @return the amClass
     */
    public Map<String, Object> getAm() {
        return getAuthorizers();
    }

    /**
     * @return the requestsLimit
     */
    public int getRequestsLimit() {
        return requestsLimit;
    }

    /**
     * @return the services
     */
    public List<Map<String, Object>> getServices() {
        return Collections.unmodifiableList(services);
    }

    /**
     * @return the authToken
     */
    public Map<String, Object> getTokenManager() {
        return tokenManager;
    }

    /**
     *
     * @return the logExchangeDump Boolean
     */
    public Integer logExchangeDump() {
        return logExchangeDump;
    }

    /**
     * @return the connectionOptions
     */
    public Map<String, Object> getConnectionOptions() {
        return Collections.unmodifiableMap(connectionOptions);
    }

    /**
     * @return the instanceName
     */
    public String getInstanceName() {
        return instanceName;
    }

    public boolean isAllowUnescapedCharactersInUrl() {
        return allowUnescapedCharactersInUrl;
    }

    /**
     * @return the authorizers
     */
    public Map<String, Object> getAuthorizers() {
        return authorizers;
    }

}
