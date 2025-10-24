package org.glassfish.runnablejar.commandline;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.of;

class CommandLineParserTest {

    private PrintStream originalPrintStream;

    @BeforeEach
    void setup() {
        originalPrintStream = System.err;
    }

    @AfterEach
    void restore() {
        System.setErr(originalPrintStream);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> portValues() {
        return Stream.of(
                of((Object) new String[]{"--port=9090"}),
                of((Object) new String[]{"--port", "9090"})
        );
    }

    @ParameterizedTest
    @MethodSource("portValues")
    void parsePortOption(String[] arguments) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setErr(new PrintStream(out));
        Arguments parse = new CommandLineParser().parse(arguments);
        int port = parse.glassFishProperties.getPort("http-listener");
        assertEquals(0, out.size(),  "Expected no message to be printed");
        assertEquals(9090, port);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> verboseValues() {
        return Stream.of(
                of((Object) new String[]{"--verbose"}),
                of((Object) new String[]{"--verbose", "true"}),
                of((Object) new String[]{"--verbose=true"})
        );
    }

    @ParameterizedTest
    @MethodSource("verboseValues")
    void parseVerboseOption(String[] arguments) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setErr(new PrintStream(out));
        Arguments parse = new CommandLineParser().parse(arguments);
        assertEquals(0, out.size(),  "Expected no message to be printed");
        assertEquals("true", parse.glassFishProperties.getProperty("verbose"));
    }

    @Test
    void libraryAndWarOptionsTest() {
        String[] arguments = {
                "--noPort",
                "--stop",
                "add-library",
                "testLibrary.jar",
                "testLibraryApp.war"
        };
        Arguments parse = new CommandLineParser().parse(arguments);
        String value = parse.glassFishProperties.getProperty("embedded-glassfish-config.server.network-config.network-listeners.network-listener.http-listener.enabled");
        assertEquals("false", value);
        assertEquals(3, parse.commands.size());
        assertTrue(parse.shutdown);
    }


}