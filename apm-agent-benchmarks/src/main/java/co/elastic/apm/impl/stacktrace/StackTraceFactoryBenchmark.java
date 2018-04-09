/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 the original author or authors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package co.elastic.apm.impl.stacktrace;

import co.elastic.apm.CpuProfiler;
import co.elastic.apm.objectpool.NoopObjectPool;
import co.elastic.apm.objectpool.ObjectPool;
import co.elastic.apm.objectpool.impl.RingBufferObjectPool;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StackTraceFactoryBenchmark {

    private StacktraceFactory.CurrentThreadStackTraceFactory currentThreadStackTraceFactory;
    private List<Stacktrace> stacktraces;
    private ObjectPool<Stacktrace> objectPool;
    private StacktraceFactory.CurrentThreadStackTraceFactory currentThreadStackTraceFactoryRecycling;

    /**
     * Convenience benchmark run method
     * <p>
     * For more accurate results, execute {@code mvn clean package} and run the benchmark via
     * {@code java -jar apm-agent-benchmarks/target/benchmarks.jar -prof gc}
     */
    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder()
            .include(StackTraceFactoryBenchmark.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .addProfiler(CpuProfiler.class)
            .build())
            .run();
    }

    @Setup
    public void setUp() {
        StacktraceConfiguration stacktraceConfiguration = new StacktraceConfiguration();
        objectPool = new RingBufferObjectPool<>(64, true, Stacktrace::new);
        currentThreadStackTraceFactory = new StacktraceFactory.CurrentThreadStackTraceFactory(stacktraceConfiguration,
            new NoopObjectPool<>(Stacktrace::new));
        currentThreadStackTraceFactoryRecycling = new StacktraceFactory.CurrentThreadStackTraceFactory(stacktraceConfiguration, objectPool);
        stacktraces = new ArrayList<>(50);
    }

    @Benchmark
    public int testCurrentThreadStackTraceFactory() {
        currentThreadStackTraceFactory.fillStackTrace(stacktraces);
        return stacktraces.size();
    }

    @Benchmark
    public int testCurrentThreadStackTraceFactoryRecycling() {
        currentThreadStackTraceFactoryRecycling.fillStackTrace(stacktraces);
        for (Stacktrace stacktrace : stacktraces) {
            objectPool.recycle(stacktrace);
        }
        int size = stacktraces.size();
        stacktraces.clear();
        return size;
    }

}
