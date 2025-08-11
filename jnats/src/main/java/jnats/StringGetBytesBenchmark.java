package jnats;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.SECONDS)
public class StringGetBytesBenchmark {

	private String data;

	@Setup(Level.Trial)
	public void setUp() {
		data = "Some US-ASCII with few 128..255 and randomness × \u00A0 !".repeat(50)+Math.random();
	}

	/// as an upper bound
	@Benchmark
	public byte[] utf8() {
		return data.getBytes(StandardCharsets.UTF_8);
	}

	@Benchmark
	public byte[] iso88591() {
		return data.getBytes(StandardCharsets.ISO_8859_1);
	}

	@Benchmark
	public byte[] usAscii() {
		return data.getBytes(StandardCharsets.US_ASCII);
	}

	@Benchmark
	public byte[] rawIso88591() {
		int len = data.length();
		byte[] bytes = new byte[len];
		//noinspection deprecation
		data.getBytes(0, len, bytes, 0);
		return bytes;
	}


	public static void main (String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
			.include(StringGetBytesBenchmark.class.getSimpleName())
			.build();

		new Runner(opt).run();
/*
Options opt = new OptionsBuilder()
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
              .build();
          new Runner(opt).run(); */
	}
}