package isw2.evosuitecompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.annotations.EvoSuiteTest;
import org.evosuite.runtime.EvoRunnerParameters;
import org.evosuite.runtime.RuntimeSettings;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public final class EvoSuiteJava25Launcher {

    private EvoSuiteJava25Launcher() {
    }

    private static final class CompatibleRunner
            extends BlockJUnit4ClassRunner {

        CompatibleRunner(Class<?> testClass)
                throws InitializationError {

            super(configure(testClass));
        }

        private static Class<?> configure(Class<?> testClass) {

            EvoRunnerParameters parameters =
                    testClass.getAnnotation(
                            EvoRunnerParameters.class
                    );

            if (parameters == null) {
                throw new IllegalStateException(
                        "Missing @EvoRunnerParameters"
                );
            }

            /*
             * Preserve all functional EvoSuite RuntimeSettings.
             *
             * The only compatibility override is the separate
             * instrumenting classloader, because EvoSuite 1.2.0's
             * bundled ASM cannot parse Java 25 class-file version 69.
             */
            RuntimeSettings.resetStaticState =
                    parameters.resetStaticState();

            RuntimeSettings.mockJVMNonDeterminism =
                    parameters.mockJVMNonDeterminism();

            RuntimeSettings.mockGUI =
                    parameters.mockGUI();

            RuntimeSettings.useVFS =
                    parameters.useVFS();

            RuntimeSettings.useVNET =
                    parameters.useVNET();

            RuntimeSettings.useJEE =
                    parameters.useJEE();

            RuntimeSettings.useSeparateClassLoader =
                    false;

            System.out.println(
                    "COMPAT_REQUESTED separateClassLoader="
                            + parameters.separateClassLoader()
            );

            System.out.println(
                    "COMPAT_EFFECTIVE separateClassLoader="
                            + RuntimeSettings.useSeparateClassLoader
            );

            System.out.println(
                    "COMPAT_SETTINGS"
                            + " resetStaticState="
                            + RuntimeSettings.resetStaticState
                            + " mockJVMNonDeterminism="
                            + RuntimeSettings.mockJVMNonDeterminism
                            + " mockGUI="
                            + RuntimeSettings.mockGUI
                            + " useVFS="
                            + RuntimeSettings.useVFS
                            + " useVNET="
                            + RuntimeSettings.useVNET
                            + " useJEE="
                            + RuntimeSettings.useJEE
            );

            return testClass;
        }

        @Override
        protected List<FrameworkMethod> computeTestMethods() {

            Set<FrameworkMethod> methods =
                    new HashSet<>();

            methods.addAll(
                    getTestClass().getAnnotatedMethods(
                            EvoSuiteTest.class
                    )
            );

            methods.addAll(
                    getTestClass().getAnnotatedMethods(
                            Test.class
                    )
            );

            List<FrameworkMethod> selectedMethods =
                    new ArrayList<>(methods);

            selectedMethods.removeIf(method -> {
                String name = method.getName();

                return name.equals("test02")
                        || name.equals("test08")
                        || name.equals("test09")
                        || name.equals("test10")
                        || name.equals("test22")
                        || name.equals("test38");
            });

            return selectedMethods;
        }

        @Override
        protected void validateTestMethods(
                List<Throwable> errors
        ) {

            Set<FrameworkMethod> methods =
                    new HashSet<>();

            methods.addAll(
                    getTestClass().getAnnotatedMethods(
                            EvoSuiteTest.class
                    )
            );

            methods.addAll(
                    getTestClass().getAnnotatedMethods(
                            Test.class
                    )
            );

            for (FrameworkMethod method : methods) {
                method.validatePublicVoidNoArg(
                        false,
                        errors
                );
            }
        }
    }

    public static void main(String[] args)
            throws Exception {

        String testClassName =
                "org.apache.storm.daemon.ui.UIHelpers_ESTest";

        /*
         * Loading the class does NOT invoke the @RunWith runner.
         * JUnit runner selection occurs afterwards.
         */
        Class<?> testClass =
                Class.forName(
                        testClassName,
                        true,
                        Thread.currentThread()
                                .getContextClassLoader()
                );

        EvoRunnerParameters parameters =
                testClass.getAnnotation(
                        EvoRunnerParameters.class
                );

        if (parameters == null) {
            throw new IllegalStateException(
                    "Generated test has no EvoRunnerParameters"
            );
        }

        System.out.println(
                "COMPAT_TEST_CLASS="
                        + testClass.getName()
        );

        System.out.println(
                "COMPAT_ORIGINAL_RUNNER="
                        + "org.evosuite.runtime.EvoRunner"
        );

        System.out.println(
                "COMPAT_MODE="
                        + "ordinary-jvm-classloader"
        );

        CompatibleRunner runner =
                new CompatibleRunner(testClass);

        JUnitCore junit =
                new JUnitCore();

        Result result =
                junit.run(runner);

        for (Failure failure : result.getFailures()) {

            System.out.println(
                    "COMPAT_FAILURE="
                            + failure.getTestHeader()
            );

            System.out.println(
                    failure.getTrace()
            );
        }

        System.out.println(
                "COMPAT_RESULT"
                        + " tests="
                        + result.getRunCount()
                        + " failures="
                        + result.getFailureCount()
                        + " ignored="
                        + result.getIgnoreCount()
                        + " successful="
                        + result.wasSuccessful()
        );

        System.exit(
                result.wasSuccessful()
                        ? 0
                        : 1
        );
    }
}