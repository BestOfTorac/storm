package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.apache.storm.daemon.ui.UIHelpers;
import org.apache.storm.generated.ExecutorInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UIHelpersFormattingBBTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("uptimeSecondsCases")
    void testF1_1UptimeFormattingSeconds(
            String frameId,
            int input,
            String expected) {

        assertEquals(
                expected,
                UIHelpers.prettyUptimeSec(input)
        );

        assertEquals(
                expected,
                UIHelpers.prettyUptimeSec(
                        String.valueOf(input)
                )
        );
    }

    static Stream<Arguments> uptimeSecondsCases() {
        return Stream.of(
                Arguments.of("U-S-01", 0, ""),
                Arguments.of("U-S-02", 1, "1s"),
                Arguments.of("U-S-03", 59, "59s"),
                Arguments.of("U-S-04", 60, "1m 0s"),
                Arguments.of("U-S-05", 61, "1m 1s"),
                Arguments.of(
                        "U-S-06",
                        3599,
                        "59m 59s"
                ),
                Arguments.of(
                        "U-S-07",
                        3600,
                        "1h 0m 0s"
                ),
                Arguments.of(
                        "U-S-08",
                        3601,
                        "1h 0m 1s"
                ),
                Arguments.of(
                        "U-S-09",
                        86399,
                        "23h 59m 59s"
                ),
                Arguments.of(
                        "U-S-10",
                        86400,
                        "1d 0h 0m 0s"
                ),
                Arguments.of(
                        "U-S-11",
                        90061,
                        "1d 1h 1m 1s"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("uptimeMillisecondsCases")
    void testF1_2UptimeFormattingMilliseconds(
            String frameId,
            int input,
            String expected) {

        assertEquals(
                expected,
                UIHelpers.prettyUptimeMs(input)
        );

        assertEquals(
                expected,
                UIHelpers.prettyUptimeMs(
                        String.valueOf(input)
                )
        );
    }

    static Stream<Arguments> uptimeMillisecondsCases() {
        return Stream.of(
                Arguments.of("U-MS-01", 0, ""),
                Arguments.of("U-MS-02", 1, "1ms"),
                Arguments.of(
                        "U-MS-03",
                        999,
                        "999ms"
                ),
                Arguments.of(
                        "U-MS-04",
                        1000,
                        "1s 0ms"
                ),
                Arguments.of(
                        "U-MS-05",
                        1001,
                        "1s 1ms"
                ),
                Arguments.of(
                        "U-MS-06",
                        59999,
                        "59s 999ms"
                ),
                Arguments.of(
                        "U-MS-07",
                        60000,
                        "1m 0s 0ms"
                ),
                Arguments.of(
                        "U-MS-08",
                        60001,
                        "1m 0s 1ms"
                ),
                Arguments.of(
                        "U-MS-09",
                        3600000,
                        "1h 0m 0s 0ms"
                ),
                Arguments.of(
                        "U-MS-10",
                        86400000,
                        "1d 0h 0m 0s 0ms"
                ),
                Arguments.of(
                        "U-MS-11",
                        90061001,
                        "1d 1h 1m 1s 1ms"
                )
        );
    }

    @ParameterizedTest(name = "TBB-003/{0}")
    @MethodSource("invalidUptimeCases")
    void testF1_3InvalidUptimeTextualRepresentation(
            String category,
            String input,
            boolean exceptionExpected,
            String expected) {

        if (exceptionExpected) {

            assertThrows(
                    NumberFormatException.class,
                    () -> UIHelpers.prettyUptimeSec(input)
            );

        } else {

            assertEquals(
                    expected,
                    UIHelpers.prettyUptimeSec(input)
            );
        }
    }

    static Stream<Arguments> invalidUptimeCases() {
        return Stream.of(
                Arguments.of(
                        "nonNumeric",
                        "not-a-number",
                        true,
                        null
                ),
                Arguments.of(
                        "empty",
                        "",
                        true,
                        null
                ),
                Arguments.of(
                        "null",
                        null,
                        true,
                        null
                ),
                Arguments.of(
                        "negative",
                        "-1",
                        false,
                        ""
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("executorCases")
    void testF1_4ExecutorRangeFormatting(
            String frameId,
            int start,
            int end,
            String expected) {

        ExecutorInfo executor =
                new ExecutorInfo();

        executor.set_task_start(start);
        executor.set_task_end(end);

        assertEquals(
                expected,
                UIHelpers.prettyExecutorInfo(executor)
        );
    }

    static Stream<Arguments> executorCases() {
        return Stream.of(
                Arguments.of(
                        "EX-01",
                        1,
                        1,
                        "[1-1]"
                ),
                Arguments.of(
                        "EX-02",
                        1,
                        5,
                        "[1-5]"
                ),
                Arguments.of(
                        "EX-03",
                        5,
                        1,
                        "[5-1]"
                ),
                Arguments.of(
                        "EX-04",
                        0,
                        0,
                        "[0-0]"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("windowHintCases")
    void testF1_5WindowHint(
            String frameId,
            String input,
            String expected) {

        assertEquals(
                expected,
                UIHelpers.getWindowHint(input)
        );
    }

    static Stream<Arguments> windowHintCases() {
        return Stream.of(
                Arguments.of(
                        "WH-01",
                        ":all-time",
                        "All time"
                ),
                Arguments.of(
                        "WH-02",
                        "0",
                        ""
                ),
                Arguments.of(
                        "WH-03",
                        "59",
                        "59s"
                ),
                Arguments.of(
                        "WH-04",
                        "60",
                        "1m 0s"
                ),
                Arguments.of(
                        "WH-05",
                        "3600",
                        "1h 0m 0s"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("streamNameCases")
    void testF1_6StreamNameSanitization(
            String frameId,
            String input,
            String expected) {

        assertEquals(
                expected,
                UIHelpers.sanitizeStreamName(input)
        );
    }

    static Stream<Arguments> streamNameCases() {
        return Stream.of(
                Arguments.of(
                        "SN-01",
                        "stream",
                        "stream"
                ),
                Arguments.of(
                        "SN-02",
                        "stream_1",
                        "stream__"
                ),
                Arguments.of(
                        "SN-03",
                        "stream-name",
                        "stream-name"
                ),
                Arguments.of(
                        "SN-04",
                        "stream.name",
                        "stream.name"
                ),
                Arguments.of(
                        "SN-05",
                        "stream name",
                        "stream_name"
                ),
                Arguments.of(
                        "SN-06",
                        "stream@name",
                        "stream_name"
                ),
                Arguments.of(
                        "SN-07",
                        "1stream",
                        "_s_stream"
                ),
                Arguments.of(
                        "SN-08",
                        "_stream",
                        "_s_stream"
                ),
                Arguments.of(
                        "SN-09",
                        "-stream",
                        "_s-stream"
                ),
                Arguments.of(
                        "SN-10",
                        ".stream",
                        "_s.stream"
                ),
                Arguments.of(
                        "SN-11",
                        "",
                        "_s"
                )
        );
    }
}
