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

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.*;

/**
 Benchmark                             Mode  Cnt        Score          Error  Units
 StringGetBytesBenchmark.iso88591     thrpt    3  4829818,688 ± 2485312,187  ops/s
 StringGetBytesBenchmark.rawIso88591  thrpt    3  4826448,322 ± 1332517,925  ops/s
 StringGetBytesBenchmark.usAscii      thrpt    3  1580261,242 ±  829945,617  ops/s
 StringGetBytesBenchmark.utf8         thrpt    3   538043,175 ±  540737,007  ops/s

 4829818/1580261 = 3
 ⇒ ISO_8859_1 is 3 ^ times faster than US_ASCII
 */
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1, jvmArgs = {
	"-XX:+UseParallelGC", "-XX:+UseCompressedOops", "-XX:+DoEscapeAnalysis", "-Xmx4G"
	//, "-XX:CompileCommand=inline,java/lang/String.charAt"
})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class StringGetBytesBenchmark {

	private static final String BASE = "Some US-ASCII with few 128..255 and randomness × \u00A0 !";

	private static String data;

	// @Setup(Level.Trial) public void setUp()
	static {
		data = BASE.repeat(50)+Math.random();
		System.out.println(data);

		String iso = new String(BASE.getBytes(ISO_8859_1), ISO_8859_1);
		System.out.println(iso);
		assert BASE.equals(iso);

		String us = new String(BASE.getBytes(US_ASCII), US_ASCII);
		System.out.println(us);
		assert "Some US-ASCII with few 128..255 and randomness ? ? !".equals(us);
	}

	/// as an upper bound
	@Benchmark
	public byte[] utf8() {
		return data.getBytes(StandardCharsets.UTF_8);
	}

	@Benchmark
	public byte[] iso88591() {
		return data.getBytes(ISO_8859_1);
	}

	@Benchmark
	public byte[] usAscii() {
		return data.getBytes(US_ASCII);
	}

	@Benchmark
	@SuppressWarnings("deprecation")
	public byte[] rawIso88591() {
		int len = data.length();
		byte[] bytes = new byte[len];
		data.getBytes(0, len, bytes, 0);// deprecated
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