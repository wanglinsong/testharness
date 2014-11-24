package com.tascape.qa.th;

import com.tascape.qa.th.db.DbHandler;
import com.tascape.qa.th.test.Priority;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class SystemConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(SystemConfiguration.class);

    public static final String CONSTANT_LOG_KEEP_ALIVE_PREFIX = "_thlkah_";

    public static final String SYSPROP_CONF_FILE = "qa.th.conf.file";

    public static final String SYSPROP_EXECUTION_ID = "qa.th.exec.id";

    public static final String SYSPROP_EXECUTION_THREAD_COUNT = "qa.th.exec.thread.count";

    public static final String SYSPROP_TEST_STATION = "qa.th.test.station";

    public static final String SYSPROP_TEST_RETRY = "qa.th.test.retry";

    public static final String SYSPROP_ROOT_PATH = "qa.th.root.path";

    public static final String SYSPROP_LOG_PATH = "qa.th.log.path";

    public static final String SYSPROP_TEST_SUITE = "qa.th.test.suite";

    public static final String SYSPROP_DEBUG_CLASS_REGEX = "qa.th.debug.class.regex";

    public static final String SYSPROP_DEBUG_METHOD_RESGX = "qa.th.debug.method.regex";

    public static final String SYSPROP_TEST_PRIORITY = "qa.th.test.priority";

    public static final String SYSPROP_PRODUCT_UNDER_TEST = "qa.th.product.under.test";

    public static final String SYSENV_JOB_NAME = "JOB_NAME";

    public static final String SYSENV_JOB_NAME_CUSTOM = "JOB_NAME_CUSTOM";

    public static final String SYSENV_JOB_NUMBER = "BUILD_NUMBER";

    public static final String SYSENV_JOB_BUILD_URL = "BUILD_URL";

    private static final SystemConfiguration CONFIG = new SystemConfiguration();

    private final Properties properties = new Properties();

    private SystemConfiguration() {
        this.listSysProperties();

        Path conf = Paths.get(System.getProperty("user.home"), ".th", "th.properties");
        String confFile = System.getProperty(SYSPROP_CONF_FILE);
        if (confFile != null) {
            conf = Paths.get(confFile);
        }
        LOG.info("Loading system configuration from {}", conf);
        try {
            FileUtils.touch(conf.toFile());
        } catch (IOException ex) {
            throw new RuntimeException("Cannot touch system configuration from " + conf, ex);
        }
        try (InputStream is = new FileInputStream(conf.toFile())) {
            this.properties.load(is);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load system configuration from " + conf, ex);
        }
        List<String> keys = new ArrayList<>(System.getProperties().stringPropertyNames());
        keys.stream().filter((key) -> (key.startsWith("qa."))).forEach((key) -> {
            this.properties.setProperty(key, System.getProperty(key));
        });

        String execId = this.properties.getProperty(SYSPROP_EXECUTION_ID);
        if (execId == null || execId.trim().isEmpty()) {
            execId = Utils.getUniqueId("wx" + System.currentTimeMillis());
            LOG.warn("There is no execution id specified, using local new UUID: {}", execId);
            this.properties.setProperty(SYSPROP_EXECUTION_ID, execId);
        }

        this.listAppProperties();
    }

    public static SystemConfiguration getInstance() {
        return CONFIG;
    }

    public String getProperty(String name) {
        return this.properties.getProperty(name);
    }

    public String getProperty(String name, String defaultValue) {
        return this.properties.getProperty(name, defaultValue);
    }

    public Path getRootPath() {
        return Paths.get(this.properties.getProperty(SYSPROP_ROOT_PATH, "/qa"));
    }

    public Path getLogPath() {
        return Paths.get(this.properties.getProperty(SYSPROP_LOG_PATH, "/qa/logs"));
    }

    public int getTestRetry() {
        return Integer.parseInt(this.properties.getProperty(SYSPROP_TEST_RETRY, "0"));
    }

    public int getExecutionThreadCount() {
        return Integer.parseInt(this.properties.getProperty(SYSPROP_EXECUTION_THREAD_COUNT, "1"));
    }

    public String getHostName() {
        String hn = this.properties.getProperty(SYSPROP_TEST_STATION);
        if (hn == null || hn.isEmpty()) {
            try {
                hn = Utils.cmd("hostname").get(0);
            } catch (IOException | InterruptedException ex) {
                LOG.warn("Cannot get host name", ex);
                hn = "unknow host";
            }
            this.properties.setProperty(SYSPROP_TEST_STATION, hn);
        }
        return hn;
    }

    public String getExecId() {
        return this.properties.getProperty(SYSPROP_EXECUTION_ID);
    }

    /**
     * Gets the array of test suite class name.
     *
     * @return
     */
    public String getTestSuite() {
        String suite = this.properties.getProperty(SYSPROP_TEST_SUITE);
        if (suite == null || suite.isEmpty()) {
            throw new RuntimeException("There is no test suite class name specified (system property "
                    + SYSPROP_TEST_SUITE + ")");
        }
        return suite;
    }

    public Pattern getTestClassRegex() {
        String regex = this.properties.getProperty(SYSPROP_DEBUG_CLASS_REGEX, ".+");
        return Pattern.compile(regex);
    }

    public Pattern getTestMethodRegex() {
        String regex = this.properties.getProperty(SYSPROP_DEBUG_METHOD_RESGX, ".+");
        return Pattern.compile(regex);
    }

    public int getTestPriority() {
        String v = this.properties.getProperty(SYSPROP_TEST_PRIORITY);
        if (v == null || v.isEmpty()) {
            return Priority.P3;
        }
        return Integer.parseInt(v);
    }

    public String getDatabaseType() {
        return this.properties.getProperty(DbHandler.SYSPROP_DATABASE_TYPE, "mysql");
    }

    public String getDatabaseHost() {
        return this.properties.getProperty(DbHandler.SYSPROP_DATABASE_HOST, "127.0.0.1:3306");
    }

    public String getDatabaseSchema() {
        return this.properties.getProperty(DbHandler.SYSPROP_DATABASE_SCHEMA, "testharness");
    }

    public String getDatabaseUser() {
        return this.properties.getProperty(DbHandler.SYSPROP_DATABASE_USER, "th");
    }

    public String getDatabasePass() {
        return this.properties.getProperty(DbHandler.SYSPROP_DATABASE_PASS, "p@ssword");
    }

    public String getProdUnderTest() {
        return this.properties.getProperty(SYSPROP_PRODUCT_UNDER_TEST, SYSPROP_PRODUCT_UNDER_TEST + " was not set");
    }

    public String getJobName() {
        String value = System.getenv().get(SYSENV_JOB_NAME_CUSTOM);
        if (value == null) {
            value = System.getenv().get(SYSENV_JOB_NAME);
            if (value == null) {
                value = this.getLocalJobName();
            } else {
                value = value.split("/")[0];
            }
        }
        return value.trim();
    }

    public int getJobBuildNumber() {
        String value = System.getenv().get(SYSENV_JOB_NUMBER);
        try {
            return value == null ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            LOG.warn("Cannot parse {}={}", SYSENV_JOB_NUMBER, value, ex);
            return -1;
        }
    }

    public String getJobBuildUrl() {
        String value = System.getenv().get(SYSENV_JOB_BUILD_URL);
        return value == null ? "#" : value;
    }

    private void listSysProperties() {
        List<String> keys = new ArrayList<>(System.getProperties().stringPropertyNames());
        Collections.sort(keys);
        LOG.debug("Java system properties");
        for (String key : keys) {
            LOG.debug(String.format("%50s : %s", key, System.getProperties().getProperty(key)));
        }

        keys = new ArrayList<>(System.getenv().keySet());
        Collections.sort(keys);
        LOG.debug("Java environment properties");
        for (String key : keys) {
            LOG.debug(String.format("%50s : %s", key, System.getenv().get(key)));
        }
    }

    private void listAppProperties() {
        LOG.debug("Application properties");
        List<String> keys = new ArrayList<>(this.properties.stringPropertyNames());
        Collections.sort(keys);
        keys.stream().forEach((key) -> {
            LOG.debug(String.format("%50s : %s", key, this.properties.getProperty(key)));
        });
    }

    private String getLocalJobName() {
        return this.getHostName() + " | " + this.getTestSuite();
    }
}