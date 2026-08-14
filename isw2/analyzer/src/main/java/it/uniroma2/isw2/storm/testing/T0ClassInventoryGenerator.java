package it.uniroma2.isw2.storm.testing;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class T0ClassInventoryGenerator {

    private static final String RELEASE_TAG =
            "v3.0.0";

    private static final Path OUTPUT =
            Paths.get(
                    "isw2",
                    "datasets",
                    "testing",
                    "class_inventory_v3.0.0.csv"
            );

    private T0ClassInventoryGenerator() {
        // Utility class.
    }

    public static void main(String[] args)
            throws Exception {

        Path repository =
                Paths.get(".")
                        .toAbsolutePath()
                        .normalize();

        validateRepository(repository);

        List<String> productionFiles =
                productionFiles(repository);

        if (productionFiles.isEmpty()) {
            throw new IllegalStateException(
                    "No production Java files found."
            );
        }

        List<InventoryRow> rows =
                parseSources(
                        repository,
                        productionFiles
                );

        rows.sort(
                Comparator
                        .comparing(
                                InventoryRow::fqcn,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .thenComparing(
                                InventoryRow::filePath
                        )
        );

        validateRows(
                productionFiles,
                rows
        );

        writeCsv(rows);

        printSummary(
                productionFiles,
                rows
        );
    }

    private static void validateRepository(
            Path repository
    ) throws Exception {

        String tagCommit =
                gitSingleLine(
                        repository,
                        "rev-parse",
                        RELEASE_TAG + "^{commit}"
                );

        if (tagCommit.isBlank()) {
            throw new IllegalStateException(
                    "Cannot resolve " + RELEASE_TAG
            );
        }

        List<String> productionDrift =
                gitLines(
                        repository,
                        "diff",
                        "--name-only",
                        RELEASE_TAG + "..HEAD",
                        "--"
                ).stream()
                        .filter(
                                T0ClassInventoryGenerator
                                        ::isProductionJavaPath
                        )
                        .toList();

        if (!productionDrift.isEmpty()) {
            throw new IllegalStateException(
                    "Production source drift from "
                            + RELEASE_TAG
                            + ": "
                            + productionDrift
            );
        }

        System.out.println(
                "Release tag             : "
                        + RELEASE_TAG
        );

        System.out.println(
                "Release commit          : "
                        + tagCommit
        );

        System.out.println(
                "Production source drift : 0"
        );
    }

    private static List<String> productionFiles(
            Path repository
    ) throws Exception {

        return gitLines(
                repository,
                "ls-tree",
                "-r",
                "--name-only",
                RELEASE_TAG
        ).stream()
                .filter(
                        T0ClassInventoryGenerator
                                ::isProductionJavaPath
                )
                .sorted()
                .toList();
    }

    private static boolean isProductionJavaPath(
            String path
    ) {

        String normalized =
                path.replace('\\', '/');

        return normalized.endsWith(".java")
                && normalized.contains(
                        "/src/main/java/"
                )
                && !normalized.startsWith(
                        "isw2/"
                );
    }

    private static List<InventoryRow> parseSources(
            Path repository,
            List<String> relativePaths
    ) throws IOException {

        JavaCompiler compiler =
                ToolProvider
                        .getSystemJavaCompiler();

        if (compiler == null) {
            throw new IllegalStateException(
                    "System Java compiler unavailable. "
                            + "Run with a JDK, not a JRE."
            );
        }

        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

        List<File> sourceFiles =
                relativePaths.stream()
                        .map(repository::resolve)
                        .map(Path::toFile)
                        .toList();

        for (File sourceFile : sourceFiles) {

            if (!sourceFile.isFile()) {
                throw new IllegalStateException(
                        "Tracked source missing from working tree: "
                                + sourceFile
                );
            }
        }

        List<InventoryRow> rows =
                new ArrayList<>();

        try (StandardJavaFileManager fileManager =
                     compiler.getStandardFileManager(
                             diagnostics,
                             Locale.ROOT,
                             StandardCharsets.UTF_8
                     )) {

            Iterable<? extends JavaFileObject> units =
                    fileManager
                            .getJavaFileObjectsFromFiles(
                                    sourceFiles
                            );

            JavacTask task =
                    (JavacTask) compiler.getTask(
                            null,
                            fileManager,
                            diagnostics,
                            List.of(
                                    "-proc:none",
                                    "--release",
                                    "21"
                            ),
                            null,
                            units
                    );

            Iterable<? extends CompilationUnitTree>
                    parsedUnits =
                    task.parse();

            Trees trees =
                    Trees.instance(task);

            SourcePositions positions =
                    trees.getSourcePositions();

            for (CompilationUnitTree unit
                    : parsedUnits) {

                rows.add(
                        inventoryFor(
                                repository,
                                unit,
                                positions
                        )
                );
            }
        }

        List<Diagnostic<? extends JavaFileObject>>
                errors =
                diagnostics.getDiagnostics()
                        .stream()
                        .filter(
                                diagnostic ->
                                        diagnostic.getKind()
                                                == Diagnostic.Kind.ERROR
                        )
                        .toList();

        if (!errors.isEmpty()) {

            System.err.println(
                    "Java parse errors: "
                            + errors.size()
            );

            errors.stream()
                    .limit(20)
                    .forEach(
                            diagnostic ->
                                    System.err.println(
                                            diagnostic.toString()
                                    )
                    );

            throw new IllegalStateException(
                    "Source parsing failed."
            );
        }

        return rows;
    }

    private static InventoryRow inventoryFor(
            Path repository,
            CompilationUnitTree unit,
            SourcePositions positions
    ) throws IOException {

        Path sourcePath =
                Paths.get(
                        unit.getSourceFile()
                                .toUri()
                ).toAbsolutePath()
                        .normalize();

        String relativePath =
                repository.relativize(
                        sourcePath
                ).toString()
                        .replace('\\', '/');

        String fileName =
                sourcePath
                        .getFileName()
                        .toString();

        String expectedSimpleName =
                fileName.substring(
                        0,
                        fileName.length()
                                - ".java".length()
                );

        String packageName =
                unit.getPackageName() == null
                        ? ""
                        : unit.getPackageName()
                                .toString();

        List<ClassTree> topLevelTypes =
                unit.getTypeDecls()
                        .stream()
                        .filter(
                                ClassTree.class::isInstance
                        )
                        .map(
                                ClassTree.class::cast
                        )
                        .toList();

        ClassTree primary =
                topLevelTypes.stream()
                        .filter(
                                type ->
                                        type.getSimpleName()
                                                .contentEquals(
                                                        expectedSimpleName
                                                )
                        )
                        .findFirst()
                        .orElse(null);

        int fileLoc =
                countLines(
                        sourcePath
                );

        if (primary == null) {

            return new InventoryRow(
                    RELEASE_TAG,
                    relativePath,
                    moduleOf(relativePath),
                    packageName,
                    "",
                    expectedSimpleName,
                    "NONE",
                    false,
                    false,
                    false,
                    fileLoc,
                    0,
                    0,
                    0,
                    0,
                    topLevelTypes.size(),
                    false
            );
        }

        String simpleName =
                primary.getSimpleName()
                        .toString();

        String fqcn =
                packageName.isBlank()
                        ? simpleName
                        : packageName
                        + "."
                        + simpleName;

        Set<Modifier> modifiers =
                primary.getModifiers()
                        .getFlags();

        int methods = 0;
        int constructors = 0;

        for (Tree member
                : primary.getMembers()) {

            if (!(member instanceof MethodTree method)) {
                continue;
            }

            if (method.getName()
                    .contentEquals("<init>")) {

                constructors++;

            } else {

                methods++;
            }
        }

        int typeLoc =
                typeLoc(
                        unit,
                        primary,
                        positions
                );

        return new InventoryRow(
                RELEASE_TAG,
                relativePath,
                moduleOf(relativePath),
                packageName,
                fqcn,
                simpleName,
                kindOf(primary),
                modifiers.contains(
                        Modifier.PUBLIC
                ),
                modifiers.contains(
                        Modifier.ABSTRACT
                ),
                modifiers.contains(
                        Modifier.FINAL
                ),
                fileLoc,
                typeLoc,
                methods,
                constructors,
                methods + constructors,
                topLevelTypes.size(),
                true
        );
    }

    private static int typeLoc(
            CompilationUnitTree unit,
            ClassTree type,
            SourcePositions positions
    ) {

        long start =
                positions.getStartPosition(
                        unit,
                        type
                );

        long end =
                positions.getEndPosition(
                        unit,
                        type
                );

        if (start < 0 || end < 0) {
            return 0;
        }

        long firstLine =
                unit.getLineMap()
                        .getLineNumber(
                                start
                        );

        long lastLine =
                unit.getLineMap()
                        .getLineNumber(
                                Math.max(
                                        start,
                                        end - 1
                                )
                        );

        return Math.toIntExact(
                lastLine
                        - firstLine
                        + 1
        );
    }

    private static int countLines(
            Path source
    ) throws IOException {

        try (var lines =
                     Files.lines(
                             source,
                             StandardCharsets.UTF_8
                     )) {

            return Math.toIntExact(
                    lines.count()
            );
        }
    }

    private static String kindOf(
            ClassTree type
    ) {

        return switch (type.getKind()) {

            case CLASS ->
                    "CLASS";

            case INTERFACE ->
                    "INTERFACE";

            case ENUM ->
                    "ENUM";

            case ANNOTATION_TYPE ->
                    "ANNOTATION";

            case RECORD ->
                    "RECORD";

            default ->
                    type.getKind()
                            .name();
        };
    }

    private static String moduleOf(
            String relativePath
    ) {

        String normalized =
                relativePath.replace(
                        '\\',
                        '/'
                );

        String marker =
                "/src/main/java/";

        int index =
                normalized.indexOf(
                        marker
                );

        if (index <= 0) {
            return "<root>";
        }

        return normalized.substring(
                0,
                index
        );
    }

    private static void validateRows(
            List<String> productionFiles,
            List<InventoryRow> rows
    ) {

        if (rows.size()
                != productionFiles.size()) {

            throw new IllegalStateException(
                    "Expected one inventory row per source file. "
                            + "Files="
                            + productionFiles.size()
                            + ", rows="
                            + rows.size()
            );
        }

        long duplicatedPaths =
                rows.stream()
                        .collect(
                                java.util.stream.Collectors
                                        .groupingBy(
                                                InventoryRow::filePath,
                                                java.util.stream.Collectors
                                                        .counting()
                                        )
                        )
                        .values()
                        .stream()
                        .filter(
                                count ->
                                        count != 1
                        )
                        .count();

        if (duplicatedPaths != 0) {
            throw new IllegalStateException(
                    "Duplicate file paths in inventory."
            );
        }
    }

    private static void writeCsv(
            List<InventoryRow> rows
    ) throws IOException {

        Files.createDirectories(
                OUTPUT.getParent()
        );

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             OUTPUT,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "ReleaseTag,"
                            + "FilePath,"
                            + "Module,"
                            + "Package,"
                            + "FQCN,"
                            + "SimpleName,"
                            + "TypeKind,"
                            + "Public,"
                            + "Abstract,"
                            + "Final,"
                            + "FileLOC,"
                            + "TypeLOC,"
                            + "DeclaredMethods,"
                            + "Constructors,"
                            + "TotalOperations,"
                            + "TopLevelTypes,"
                            + "PrimaryTypeFound"
            );

            writer.newLine();

            for (InventoryRow row : rows) {

                writer.write(
                        csv(row.releaseTag())
                                + ","
                                + csv(row.filePath())
                                + ","
                                + csv(row.module())
                                + ","
                                + csv(row.packageName())
                                + ","
                                + csv(row.fqcn())
                                + ","
                                + csv(row.simpleName())
                                + ","
                                + csv(row.typeKind())
                                + ","
                                + row.isPublic()
                                + ","
                                + row.isAbstract()
                                + ","
                                + row.isFinal()
                                + ","
                                + row.fileLoc()
                                + ","
                                + row.typeLoc()
                                + ","
                                + row.declaredMethods()
                                + ","
                                + row.constructors()
                                + ","
                                + row.totalOperations()
                                + ","
                                + row.topLevelTypes()
                                + ","
                                + row.primaryTypeFound()
                );

                writer.newLine();
            }
        }
    }

    private static void printSummary(
            List<String> productionFiles,
            List<InventoryRow> rows
    ) {

        long primaryTypes =
                rows.stream()
                        .filter(
                                InventoryRow
                                        ::primaryTypeFound
                        )
                        .count();

        long noPrimary =
                rows.size()
                        - primaryTypes;

        Map<TypeCategory, Long> counts =
                new EnumMap<>(
                        TypeCategory.class
                );

        for (TypeCategory category
                : TypeCategory.values()) {

            counts.put(
                    category,
                    0L
            );
        }

        for (InventoryRow row : rows) {

            TypeCategory category =
                    TypeCategory.from(
                            row.typeKind()
                    );

            counts.put(
                    category,
                    counts.get(category)
                            + 1
            );
        }

        long abstractClasses =
                rows.stream()
                        .filter(
                                row ->
                                        row.typeKind()
                                                .equals("CLASS")
                                                && row.isAbstract()
                        )
                        .count();

        System.out.println();
        System.out.println(
                "===== T0 CLASS INVENTORY ====="
        );

        System.out.println(
                "Tracked main Java files : "
                        + productionFiles.size()
        );

        System.out.println(
                "Inventory rows          : "
                        + rows.size()
        );

        System.out.println(
                "Primary types found     : "
                        + primaryTypes
        );

        System.out.println(
                "No primary type         : "
                        + noPrimary
        );

        System.out.println();

        for (TypeCategory category
                : TypeCategory.values()) {

            System.out.printf(
                    Locale.ROOT,
                    "%-12s : %d%n",
                    category.label,
                    counts.get(category)
            );
        }

        System.out.println(
                "Abstract CLASS     : "
                        + abstractClasses
        );

        System.out.println();
        System.out.println(
                "CSV                    : "
                        + OUTPUT.toAbsolutePath()
        );

        System.out.println();
        System.out.println(
                "RESULT: T0 CLASS INVENTORY OK"
        );
    }

    private static String csv(
            String value
    ) {

        if (value == null) {
            return "";
        }

        boolean quote =
                value.contains(",")
                        || value.contains("\"")
                        || value.contains("\n")
                        || value.contains("\r");

        String escaped =
                value.replace(
                        "\"",
                        "\"\""
                );

        return quote
                ? "\""
                + escaped
                + "\""
                : escaped;
    }

    private static List<String> gitLines(
            Path repository,
            String... arguments
    ) throws Exception {

        List<String> command =
                new ArrayList<>();

        command.add("git");

        command.addAll(
                List.of(arguments)
        );

        Process process =
                new ProcessBuilder(command)
                        .directory(
                                repository.toFile()
                        )
                        .redirectErrorStream(true)
                        .start();

        List<String> output;

        try (var reader =
                     process.inputReader(
                             StandardCharsets.UTF_8
                     )) {

            output =
                    reader.lines()
                            .toList();
        }

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {
            throw new IllegalStateException(
                    "Git command failed: "
                            + String.join(
                                    " ",
                                    command
                            )
                            + System.lineSeparator()
                            + String.join(
                                    System.lineSeparator(),
                                    output
                            )
            );
        }

        return output;
    }

    private static String gitSingleLine(
            Path repository,
            String... arguments
    ) throws Exception {

        List<String> lines =
                gitLines(
                        repository,
                        arguments
                );

        return lines.isEmpty()
                ? ""
                : lines.getFirst()
                        .trim();
    }

    private record InventoryRow(
            String releaseTag,
            String filePath,
            String module,
            String packageName,
            String fqcn,
            String simpleName,
            String typeKind,
            boolean isPublic,
            boolean isAbstract,
            boolean isFinal,
            int fileLoc,
            int typeLoc,
            int declaredMethods,
            int constructors,
            int totalOperations,
            int topLevelTypes,
            boolean primaryTypeFound
    ) {
    }

    private enum TypeCategory {

        CLASS("CLASS"),
        INTERFACE("INTERFACE"),
        ENUM("ENUM"),
        ANNOTATION("ANNOTATION"),
        RECORD("RECORD"),
        NONE("NONE"),
        OTHER("OTHER");

        private final String label;

        TypeCategory(
                String label
        ) {
            this.label = label;
        }

        private static TypeCategory from(
                String kind
        ) {

            for (TypeCategory category
                    : values()) {

                if (category.label
                        .equals(kind)) {

                    return category;
                }
            }

            return OTHER;
        }
    }
}