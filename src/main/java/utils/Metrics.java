/*
 * Copyright 2013 Rackspace
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package utils;


import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.log4j.InstrumentedAppender;
import org.apache.log4j.LogManager;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Metrics {
    private static final MetricRegistry registry = new MetricRegistry();
    private static final GraphiteReporter reporter;

    static {
       String nodeName =  System.getenv().get("NODE_NAME");
        if(nodeName==null)
            nodeName="Default_Node";
        InstrumentedAppender appender = new InstrumentedAppender(registry);
        appender.activateOptions();
        LogManager.getRootLogger().addAppender(appender);

            Graphite graphite = new Graphite(new InetSocketAddress(Constants.GRAPHITE_HOSTS, Constants.GRAPHITE_PORT));

            reporter = GraphiteReporter
                    .forRegistry(registry)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .prefixedWith(nodeName)
                    .build(graphite);

            reporter.start(30l, TimeUnit.SECONDS);

    }

    static class PrefixedMetricSet implements MetricSet {
        private final Map<String, Metric> metricMap;

        PrefixedMetricSet(final MetricSet metricSet, final String prefix1, final String prefix2) {
            metricMap = Collections.unmodifiableMap(new HashMap<String, Metric>(){{
                for (Map.Entry<String, Metric> stringMetricEntry : metricSet.getMetrics().entrySet()) {
                    put(MetricRegistry.name(prefix1, prefix2, stringMetricEntry.getKey()), stringMetricEntry.getValue());
                }
            }});
        }

        @Override
        public Map<String, Metric> getMetrics() {
            return metricMap;
        }
    }

    public static MetricRegistry getRegistry() {
        return registry;
    }

    public static Meter meter(Class kls, String... names) {
        return getRegistry().meter(MetricRegistry.name(kls, names));
    }

    public static Timer timer(Class kls, String... names) {
        return getRegistry().timer(MetricRegistry.name(kls, names));
    }

    public static Histogram histogram(Class kls, String... names) {
        return getRegistry().histogram(MetricRegistry.name(kls, names));
    }

    public static Counter counter(Class kls, String... names) {
        return getRegistry().counter(MetricRegistry.name(kls, names));
    }
}
