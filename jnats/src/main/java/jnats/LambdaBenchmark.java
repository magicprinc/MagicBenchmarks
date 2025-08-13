package jnats;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import java.util.stream.IntStream;

/**
 https://github.com/openjdk/jmh
 https://www.baeldung.com/java-microbenchmark-harness

 Benchmark                                    Mode  Cnt         Score         Error  Units


 ThroughputA/B
*/
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1, jvmArgs = {
	"-XX:+UseParallelGC", "-XX:+UseCompressedOops", "-Xmx4G",
	"-showversion", "-ea",
	"--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow" // JDK 24
	//, "-XX:CompileCommand=inline,java/lang/String.charAt"
})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class LambdaBenchmark {
	final AtomicLong cnt = new AtomicLong();

	long calc (LongSupplier op) {
		return IntStream.range(0, 1_000_000).mapToLong(i->i ^ op.getAsLong()).sum();
	}

	long calc (long op) {
		long sum = 0;
		for (int i = 0; i < 1_000_000; i++) {
			sum += i ^ op;
		}
		return sum;
	}

	@Benchmark
	public long lambda () {
		int v1 = (int) cnt.incrementAndGet();
		String v2 = ( cnt.incrementAndGet() & 1 ) == 1 ? "Yes" : "No";
		long v3 = cnt.incrementAndGet();
		Boolean v4 = ( cnt.incrementAndGet() & 1 ) == 1;

		LongSupplier op = ()->v1^v2.hashCode()^v3^v4.hashCode();

		return calc(op);
	}

	@Benchmark
	public long directCode () {
		int v1 = (int) cnt.incrementAndGet();
		String v2 = ( cnt.incrementAndGet() & 1 ) == 1 ? "Yes" : "No";
		long v3 = cnt.incrementAndGet();
		Boolean v4 = ( cnt.incrementAndGet() & 1 ) == 1;

		return calc(v1^v2.hashCode()^v3^v4.hashCode());
	}

	public static void main (String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
			.include(LambdaBenchmark.class.getSimpleName())
			.build();

		new Runner(opt).run();
/* Options opt ^= new OptionsBuilder()
					.include(this.getClass().getName() + ".*")
					.mode (Mode.AverageTime)
					.timeUnit(TimeUnit.MICROSECONDS)
					.warmupTime(TimeValue.seconds(1))
					.warmupIterations(2)
					.measurementTime(TimeValue.seconds(1))
					.measurementIterations(2)
					.threads(2)
					.forks(1)
					.shouldFailOnError(true)
					.shouldDoGC(true)
					.build();	*/
	}
}