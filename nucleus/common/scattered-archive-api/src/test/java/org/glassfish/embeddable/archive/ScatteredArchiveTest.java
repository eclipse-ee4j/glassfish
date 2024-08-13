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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 *
 * @author ondro
 */
public class ScatteredArchiveTest {

    private static final String REGEX_COMMA_WITH_SPACE_AROUND = "[ ]*,[ ]*";

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
                "path, '', path",
                "path:path2, '', 'path,path2'",
                "'jakarta.jakarta-api.jar:/folder/path2:/folder/glassfish-embedded-all.jar', '', /folder/path2",
                "'jakarta.jakarta-api.jar:glassfish-embedded-all.jar', '', ''",
                "path, 'path', ''",
                "path111:/folder/path222, path.*, ''",
                "path333:path111:/folder/path222, 'path1.* , path2.*', path333",
                "jakarta.jakarta-api.jar:path2:/some/folder/path3:somepath, path., somepath",
                "'', '', ''"
    })
    public void addCurrentClasspathUsingExcludes(String classpath,
            @ConvertWith(ConvertCSVToArrayOfStrings.class) String[] excludesPatterns,
            @ConvertWith(ConvertCSVToListOfFiles.class) List<File> expectedFilesInClasspath) {

        // GIVEN
        setCurrentClasspath(classpath);
        TestableScatteredArchive archive = new TestableScatteredArchive("test", ScatteredArchive.Type.WAR);

        // WHEN
        archive.addCurrentClassPath(excludesPatterns);

        // THEN
        assertAll("Classpath",
                () -> assertThat("Number of classpath elements",
                        archive.classpathElements, hasSize(expectedFilesInClasspath.size())),
                () -> assertThat("Classpath elements", archive.classpathElements, equalTo(expectedFilesInClasspath))
        );
    }

    @ParameterizedTest
    @CsvSource({
        "path, '', path",
        "path:path2, '', 'path,path2'",
        "'jakarta.jakarta-api.jar:/folder/path2:/folder/glassfish-embedded-all.jar', '0', '/folder/path2,/folder/glassfish-embedded-all.jar'",
        "'jakarta.jakarta-api.jar:glassfish-embedded-all.jar', '0,1', ''",
        "path, '0', ''",
        "path111:/folder/path222, '0,1', ''",
        "path111:/folder/path222, '0', /folder/path222",
        "'jakarta.jakarta-api.jar:path2:/some/folder/path3:somepath', '1,2', 'jakarta.jakarta-api.jar,somepath'",
        "'', '', ''"
    })
    public void addCurrentClasspathUsingPredicate(String classpath,
            @ConvertWith(ConvertCSVToArrayOfStrings.class) String[] excludesIndexes,
            @ConvertWith(ConvertCSVToListOfFiles.class) List<File> expectedFilesInClasspath) {

        // GIVEN
        var cpElements = setCurrentClasspath(classpath);
        Predicate<String> exclude = value -> {
            return Stream.of(excludesIndexes)
                    .filter(s -> !s.isBlank())
                    .map(Integer::valueOf)
                    .anyMatch(i -> cpElements[i].equals(value));
        };
        TestableScatteredArchive archive = new TestableScatteredArchive("test", ScatteredArchive.Type.WAR);

        // WHEN
        archive.addCurrentClassPath(exclude);

        // THEN
        assertAll("Classpath",
                () -> assertThat("Number of classpath elements",
                        archive.classpathElements, hasSize(expectedFilesInClasspath.size())),
                () -> assertThat("Classpath elements", archive.classpathElements, equalTo(expectedFilesInClasspath))
        );
    }

    private static String[] setCurrentClasspath(String classpath) {
        String osSpecificClasspath = classpath.replace('/', File.separatorChar).replace(':', File.pathSeparatorChar);
        System.setProperty(ScatteredArchive.JAVA_CLASS_PATH_PROPERTY_KEY, osSpecificClasspath);
        return osSpecificClasspath.split("\\" + File.pathSeparator);
    }

    private static class ConvertCSVToArrayOfStrings implements ArgumentConverter {

        @Override
        public Object convert(Object source, ParameterContext context)
                throws ArgumentConversionException {
            return convertToArrayOfStrings(source);
        }

        protected String[] convertToArrayOfStrings(Object source) throws IllegalArgumentException {
            if (!(source instanceof String)) {
                throw new IllegalArgumentException(
                        "The argument should be a string: " + source);
            }
            try {
                final String sourceString = (String) source;
                return !sourceString.isBlank() ? sourceString.split(REGEX_COMMA_WITH_SPACE_AROUND) : new String[0];
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to convert value " + source, e);
            }
        }
    }

    private static class ConvertCSVToListOfFiles extends ConvertCSVToArrayOfStrings implements ArgumentConverter {

        @Override
        public Object convert(Object source, ParameterContext context)
                throws ArgumentConversionException {
            return List.of(this.convertToArrayOfStrings(source)).stream()
                    .map(File::new)
                    .collect(Collectors.toList());
        }


    }


}
