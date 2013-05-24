package com.brianfromoregon.metrics;

import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.*;
import com.codahale.metrics.logback.InstrumentedAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

/**
 *
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        final MetricRegistry metrics = new MetricRegistry();

        // Instrument logback
        LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);
        InstrumentedAppender metricsAppender = new InstrumentedAppender(metrics);
        metricsAppender.setContext(root.getLoggerContext());
        metricsAppender.start();
        root.addAppender(metricsAppender);

        // Instrument JVM
        metrics.register(ThreadStatesGaugeSet.class.getName(), new ThreadStatesGaugeSet());
        metrics.register(MemoryUsageGaugeSet.class.getName(), new MemoryUsageGaugeSet());
        metrics.register(GarbageCollectorMetricSet.class.getName(), new GarbageCollectorMetricSet());
        metrics.register(BufferPoolMetricSet.class.getName(), new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metrics.register(FileDescriptorRatioGauge.class.getName(), new FileDescriptorRatioGauge());

        // Export to JMX
        final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
        reporter.start();

        log.info("Going to sleep");
        Thread.sleep(Long.MAX_VALUE);
    }
}
