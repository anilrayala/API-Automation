package org.api.automation.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URL;

/**
 * Reads static test resources — JSON payload files and binary upload fixtures.
 *
 * <h2>Classpath, not relative file paths</h2>
 * The old code addressed files as {@code "src/main/resources/AddPlace.json"}. That only
 * resolves when the working directory happens to be the project root, so it breaks when
 * run from an IDE with a different working directory, from a Jenkins workspace, or from a
 * packaged jar.
 *
 * <p>Reading through the classloader means the path is relative to the built classpath and
 * works in all of those cases.
 */
public final class ResourceReader {

    private ResourceReader() {
        // static utility
    }

    /**
     * Reads a classpath resource as a UTF-8 string.
     *
     * @param resourcePath e.g. {@code "payloads/add-place.json"}
     */
    public static String readAsString(String resourcePath) {
        try (InputStream in = open(resourcePath)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read classpath resource: " + resourcePath, e);
        }
    }

    /**
     * Reads a static JSON payload file and substitutes {@code {{placeholder}}} tokens.
     *
     * <p>This is the bridge between the "static payload" and "dynamic payload" topics: the
     * body still lives in a readable {@code .json} file under version control, but the
     * values that need to vary per test are injected.
     *
     * @param resourcePath  classpath path to the template
     * @param replacements  alternating key/value pairs, e.g. {@code "isbn", "abc1", "aisle", "12345"}
     */
    public static String readJsonTemplate(String resourcePath, String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new RuntimeException(
                    "Replacements must be alternating key/value pairs, got " + replacements.length + " arguments");
        }
        String content = readAsString(resourcePath);
        for (int i = 0; i < replacements.length; i += 2) {
            content = content.replace("{{" + replacements[i] + "}}", replacements[i + 1]);
        }
        return content;
    }

    /**
     * Resolves a classpath resource to a {@link File}, for APIs that need a real file on
     * disk — REST Assured's {@code .multiPart("productImage", file)} being the example.
     */
    public static File asFile(String resourcePath) {
        URL url = ResourceReader.class.getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new RuntimeException("Classpath resource not found: " + resourcePath);
        }
        return new File(url.getFile());
    }

    private static InputStream open(String resourcePath) {
        InputStream in = ResourceReader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new RuntimeException("Classpath resource not found: " + resourcePath
                    + " (expected under src/main/resources or src/test/resources)");
        }
        return in;
    }
}
