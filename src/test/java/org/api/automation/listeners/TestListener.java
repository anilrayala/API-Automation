package org.api.automation.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Logs the boundaries and outcome of every test.
 *
 * <p>The value shows up when a suite fails in CI. Without it you get a stack trace and have
 * to work out which test produced it and what ran before. With it, the log reads as a
 * timeline: each test's start, its result, its duration, and a per-run summary at the end.
 *
 * <p>This is also the natural place to attach evidence on failure — the request/response
 * pair, a correlation id — if you later add an Allure or ExtentReports layer.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        log.info("=== START {} ===", qualifiedName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("=== PASS  {} ({} ms) ===", qualifiedName(result), duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("=== FAIL  {} ({} ms) ===", qualifiedName(result), duration(result));
        if (result.getThrowable() != null) {
            log.error("Reason: {}", result.getThrowable().getMessage());
        }
        log.error("Full request/response traffic: target/logs/api-traffic.log");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // A skip is usually a failed dependency or an unmet precondition, so say which.
        log.warn("=== SKIP  {} === {}", qualifiedName(result),
                result.getThrowable() == null ? "" : result.getThrowable().getMessage());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("Suite '{}' finished — passed: {}, failed: {}, skipped: {}",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    private String qualifiedName(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
    }

    private long duration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
