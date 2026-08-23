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

public final class RedisFilterBoltJava25Launcher {

    private RedisFilterBoltJava25Launcher() {
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
             * Preserve the generated EvoSuite runtime configuration.
             *
             * Compatibility override only:
             * EvoSuite 1.2.0's separate InstrumentingClassLoader
             * uses bundled ASM that cannot parse Java 25
             * class-file major version 69.
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

            /*
             * RedisFilterBolt:
             * no generated tests are excluded at this stage.
             */
            return new ArrayList<>(methods);
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
                "org.apache.storm.redis.bolt.RedisFilterBolt_ESTest";

        /*
         * Loading the generated class itself does not invoke
         * its @RunWith(EvoRunner.class). Runner selection
         * happens only when JUnit executes it normally.
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