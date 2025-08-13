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

/**
 https://github.com/openjdk/jmh
 https://www.baeldung.com/java-microbenchmark-harness

 Hard to make right 🤷‍♀️
 Benchmark                          Mode  Cnt        Score        Error  Units
 IsCastHeavyBenchmark.noCast       thrpt    5  9759226,055 ± 685787,610  ops/s
 IsCastHeavyBenchmark.withCast     thrpt    5  9782138,463 ± 384069,622  ops/s
 IsCastHeavyBenchmark.withGeneric  thrpt    5  9748511,700 ± 835214,577  ops/s

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
public class IsCastHeavyBenchmark {
	static final AtomicLong cnt = new AtomicLong();

	static class HolderObj {
		Object value;

		Object getValue () {
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			return value;
		}
	}

	static class Holder<T> {
		T value;

		T getValue () {
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			return value;
		}
	}

	/// direct type
	static class HolderStr {
		String value;

		String getValue () {
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			cnt.incrementAndGet();
			return value;
		}
	}

	final HolderObj holder1 = new HolderObj();
	final Holder<String> holder2 = new Holder<>();
	final HolderStr holder3 = new HolderStr();

	@Benchmark
	public String withCast () {
		holder1.value = (cnt.incrementAndGet() & 1) == 1 ? "Some String" : "Other";
		return (String) holder1.getValue();
	}

	@Benchmark
	public String withGeneric () {
		holder2.value = (cnt.incrementAndGet() & 1) == 1 ? "Some String" : "Other";
		return holder2.getValue();
	}

	@Benchmark
	public String noCast () {
		holder3.value = (cnt.incrementAndGet() & 1) == 1 ? "Some String" : "Other";
		return holder3.getValue();
	}

	public static void main (String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
			.include(IsCastHeavyBenchmark.class.getSimpleName())
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