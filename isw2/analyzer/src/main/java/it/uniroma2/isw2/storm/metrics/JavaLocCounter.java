package it.uniroma2.isw2.storm.metrics;

import java.util.Objects;

public final class JavaLocCounter {

    private enum LexicalState {
        NORMAL,
        BLOCK_COMMENT,
        STRING,
        CHARACTER,
        TEXT_BLOCK
    }

    private JavaLocCounter() {
        // Utility class.
    }

    public static int count(String source) {
        Objects.requireNonNull(
            source,
            "Source code cannot be null."
        );

        String[] lines =
            source.split("\\R", -1);

        LexicalState state =
            LexicalState.NORMAL;

        int linesOfCode = 0;

        for (String line : lines) {
            boolean hasCode = false;
            boolean escaped = false;

            for (
                int index = 0;
                index < line.length();
                index++
            ) {
                char current =
                    line.charAt(index);

                char next =
                    index + 1 < line.length()
                        ? line.charAt(index + 1)
                        : '\0';

                switch (state) {
                    case BLOCK_COMMENT -> {
                        if (
                            current == '*'
                                && next == '/'
                        ) {
                            state =
                                LexicalState.NORMAL;

                            index++;
                        }
                    }

                    case STRING -> {
                        hasCode = true;

                        if (escaped) {
                            escaped = false;

                        } else if (current == '\\') {
                            escaped = true;

                        } else if (current == '"') {
                            state =
                                LexicalState.NORMAL;
                        }
                    }

                    case CHARACTER -> {
                        hasCode = true;

                        if (escaped) {
                            escaped = false;

                        } else if (current == '\\') {
                            escaped = true;

                        } else if (current == '\'') {
                            state =
                                LexicalState.NORMAL;
                        }
                    }

                    case TEXT_BLOCK -> {
                        if (
                            isTripleQuote(
                                line,
                                index
                            )
                        ) {
                            hasCode = true;
                            state =
                                LexicalState.NORMAL;

                            index += 2;

                        } else if (
                            !Character.isWhitespace(
                                current
                            )
                        ) {
                            hasCode = true;
                        }
                    }

                    case NORMAL -> {
                        if (
                            Character.isWhitespace(
                                current
                            )
                        ) {
                            continue;
                        }

                        if (
                            current == '/'
                                && next == '/'
                        ) {
                            index = line.length();
                            continue;
                        }

                        if (
                            current == '/'
                                && next == '*'
                        ) {
                            state =
                                LexicalState.BLOCK_COMMENT;

                            index++;
                            continue;
                        }

                        if (
                            isTripleQuote(
                                line,
                                index
                            )
                        ) {
                            hasCode = true;
                            state =
                                LexicalState.TEXT_BLOCK;

                            index += 2;
                            continue;
                        }

                        hasCode = true;

                        if (current == '"') {
                            state =
                                LexicalState.STRING;

                        } else if (current == '\'') {
                            state =
                                LexicalState.CHARACTER;
                        }
                    }
                }
            }

            if (
                state == LexicalState.STRING
                    || state
                        == LexicalState.CHARACTER
            ) {
                state = LexicalState.NORMAL;
            }

            if (hasCode) {
                linesOfCode++;
            }
        }

        return linesOfCode;
    }

    private static boolean isTripleQuote(
            String line,
            int index) {

        return index + 2 < line.length()
            && line.charAt(index) == '"'
            && line.charAt(index + 1) == '"'
            && line.charAt(index + 2) == '"';
    }
}