/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package org.glassfish.embeddable.archive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 *
 * @author ondro
 */
public class ScatteredArchiveTest {

    private final String REGEX_COMMA_WITH_SPACE_AROUND = "[ ]*,[ ]*";

    public ScatteredArchiveTest() {
    }

    public static class TestableScatteredArchive extends ScatteredArchive {

        List<File> classpathElements = new ArrayList<>();

        public TestableScatteredArchive(String name, Type type) {
            super(name, type);
        }

        @Override
        public void addClassPath(File classpath) throws IOException {
            classpathElements.add(classpath);
        }

    }

    @ParameterizedTest
    @CsvSource({
                "path, '', 1",
                "path:path2, '', 2",
                "'jakarta.jakarta-api.jar:/folder/path2:/folder/glassfish-embedded-all.jar', '', 1",
                "'jakarta.jakarta-api.jar:glassfish-embedded-all.jar', '', 0",
                "path, 'path', 0",
                "path111:/folder/path222, 'path.*', 0",
                "path333:path111:/folder/path222, 'path1.* , path2.*', 1",
                "'jakarta.jakarta-api.jar:path2:/some/folder/path3:somepath', 'path.', 1",
                "'', '', 0"
            })
    public void addCurrentClasspathUsingExcludes(String classpath, String excludesList, int expectedElementsInClasspath) {

        // GIVEN
        setCurrentClasspath(classpath);
        String[] excludesPatterns = excludesList.split(REGEX_COMMA_WITH_SPACE_AROUND);
        TestableScatteredArchive archive = new TestableScatteredArchive("test", ScatteredArchive.Type.WAR);

        // WHEN
        archive.addCurrentClassPath(excludesPatterns);

        // THEN
        assertAll("classpath",
                () -> assertThat("Number of classpath elements",
                        archive.classpathElements, hasSize(expectedElementsInClasspath))
        );
    }

    @ParameterizedTest
    @CsvSource({
        "path, '', 1",
        "path:path2, '', 2",
        "'jakarta.jakarta-api.jar:/folder/path2:/folder/glassfish-embedded-all.jar', '0', 2",
        "'jakarta.jakarta-api.jar:glassfish-embedded-all.jar', '0,1', 0",
        "path, '0', 0",
        "path111:/folder/path222, '0,1', 0",
        "path111:/folder/path222, '0', 1",
        "'jakarta.jakarta-api.jar:path2:/some/folder/path3:somepath', '1,2', 2",
        "'', '', 0"
    })
    public void addCurrentClasspathUsingPredicate(String classpath, String excludesIndexes, int expectedElementsInClasspath) {

        // GIVEN
        var cpElements = setCurrentClasspath(classpath);
        Predicate<String> exclude = value -> {
            return Stream.of(excludesIndexes.split(REGEX_COMMA_WITH_SPACE_AROUND))
                    .filter(s -> !s.isBlank())
                    .map(Integer::valueOf)
                    .anyMatch(i -> cpElements[i].equals(value));
        };
        TestableScatteredArchive archive = new TestableScatteredArchive("test", ScatteredArchive.Type.WAR);

        // WHEN
        archive.addCurrentClassPath(exclude);

        // THEN
        try {
            assertAll("classpath",
                    () -> assertThat("Number of classpath elements " + archive.classpathElements,
                            archive.classpathElements, hasSize(expectedElementsInClasspath))
            );
        } catch (AssertionError e) {
            throw e;
        }
    }

    private static String[] setCurrentClasspath(String classpath) {
        String osSpecificClasspath = classpath.replace('/', File.separatorChar).replace(':', File.pathSeparatorChar);
        System.setProperty(ScatteredArchive.JAVA_CLASS_PATH_PROPERTY_KEY, osSpecificClasspath);
        return osSpecificClasspath.split("\\" + File.pathSeparator);
    }

}
