package org.api.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Single source of truth for every configurable value in the framework.
 *
 * <p>Nothing (base URIs, API keys, credentials) should be hardcoded in a test or a
 * service class. Everything flows through here.
 *
 * <p><b>Resolution order</b> — first match wins:
 * <ol>
 *   <li>JVM system property   — {@code mvn test -DbaseUrl=https://staging.example.com}</li>
 *   <li>OS environment variable — {@code BASE_URL} (key upper-cased, '.' -> '_')</li>
 *   <li>{@code config/<env>.properties} on the classpath</li>
 * </ol>
 *
 * <p>That order is deliberate: it means secrets never have to live in a committed file.
 * CI sets an env var, a developer passes {@code -D}, and the properties file only holds
 * non-sensitive defaults.
 *
 * <p>Environment is selected with {@code -Denv=qa} (default {@code qa}), which maps to
 * {@code src/test/resources/config/qa.properties}.
 */
public final class ConfigManager {

    private static final String ENV_KEY = "env";
    private static final String DEFAULT_ENV = "qa";

    private static final String environment = resolveEnvironment();
    private static final Properties FILE_PROPS = loadEnvironmentFile(environment);

    private ConfigManager() {
        // static utility
    }

    private static String resolveEnvironment() {
        String env = System.getProperty(ENV_KEY);
        if (isBlank(env)) {
            env = System.getenv("ENV");
        }
        return isBlank(env) ? DEFAULT_ENV : env.trim();
    }

    private static Properties loadEnvironmentFile(String env) {
        String path = "config/" + env + ".properties";
        Properties props = new Properties();
        try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Environment config not found on classpath: " + path
                                + ". Expected src/test/resources/" + path
                                + " (selected via -Denv=" + env + ").");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read environment config: " + path, e);
        }
        return props;
    }

    /** The active environment name, e.g. {@code qa}. */
    public static String environment() {
        return environment;
    }

    /**
     * Returns the value for {@code key}, or {@code null} when it is not set anywhere.
     * Prefer {@link #getRequired(String)} for values the test cannot run without.
     */
    public static String get(String key) {
        String fromSystemProperty = System.getProperty(key);
        if (!isBlank(fromSystemProperty)) {
            return fromSystemProperty.trim();
        }
        String fromEnvVar = System.getenv(toEnvVarName(key));
        if (!isBlank(fromEnvVar)) {
            return fromEnvVar.trim();
        }
        String fromFile = FILE_PROPS.getProperty(key);
        return isBlank(fromFile) ? null : fromFile.trim();
    }

    /** Returns the value for {@code key}, falling back to {@code defaultValue}. */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Returns the value for {@code key} or fails loudly. Use this for anything the test
     * genuinely needs — a missing base URI should be an immediate, readable error rather
     * than a confusing {@code NullPointerException} deep inside REST Assured.
     */
    public static String getRequired(String key) {
        String value = get(key);
        if (value == null) {
            throw new IllegalStateException(String.format(
                    "Required config '%s' is not set. Provide it via -D%s=<value>, "
                            + "the %s environment variable, or config/%s.properties",
                    key, key, toEnvVarName(key), environment));
        }
        return value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Config '" + key + "' is not a number: " + value, e);
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    /** {@code oauth.client.secret} -> {@code OAUTH_CLIENT_SECRET} */
    private static String toEnvVarName(String key) {
        return key.replace('.', '_').replace('-', '_').toUpperCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
