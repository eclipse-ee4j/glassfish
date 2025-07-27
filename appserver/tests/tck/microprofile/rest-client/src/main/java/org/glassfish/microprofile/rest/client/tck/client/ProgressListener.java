package org.glassfish.microprofile.rest.client.tck.client;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.testng.IClassListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ProgressListener implements ITestListener, IClassListener {

    private int totalTests = 0;
    private final AtomicInteger executedTests = new AtomicInteger(0);
    private final Set<ITestResult> seen = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void onBeforeClass(ITestClass testClass) {
        System.out.println("\n\n\u001B[34m\u001B[1m>>> Starting class: " + testClass.getName() + "\u001B[0m\n");
    }

    @Override
    public void onStart(ITestContext context) {
        totalTests = context.getAllTestMethods().length;
        System.out.println("Starting test run with " + totalTests + " tests.");
    }

    @Override
    public void onTestStart(ITestResult result) {
        String className = result.getMethod().getTestClass().getName();

        int runSoFar = executedTests.get();

        System.out.println(
            "[\u001B[1;32mRunning\u001B[0m] \u001B[32m" + className + "#" + result.getMethod().getMethodName() +
            "\u001B[0m\" | \u001B[1;32m(" + runSoFar + "/" + totalTests + ")\u001B[0m");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        markTestComplete(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        markTestComplete(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        markTestComplete(result);
    }

    private void markTestComplete(ITestResult result) {
        // Ensure we only count a test once
        if (seen.add(result)) {
            executedTests.incrementAndGet();
        }
    }
}
