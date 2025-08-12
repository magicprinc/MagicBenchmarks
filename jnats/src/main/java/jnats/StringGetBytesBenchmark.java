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
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.*;

/**
 https://github.com/openjdk/jmh
 https://www.baeldung.com/java-microbenchmark-harness

 Benchmark                                    Mode  Cnt         Score         Error  Units
 StringGetBytesBenchmark.rawIso88591_direct  thrpt    5  52688916,779 ± 1330213,604  ops/s
 StringGetBytesBenchmark.rawIso88591_1       thrpt    5   4994702,469 ±  164145,042  ops/s
 StringGetBytesBenchmark.iso88591_2          thrpt    5   5088032,248 ±  101246,895  ops/s
 StringGetBytesBenchmark.usAscii_3           thrpt    5   1733596,022 ±   49036,954  ops/s
 StringGetBytesBenchmark.utf8_4              thrpt    5    556368,909 ±   45252,563  ops/s
 StringGetBytesBenchmark.manualIso88591_5    thrpt    5    452574,288 ±   11224,975  ops/s

 ThroughputA/B
 ISO_8859_1 is 2-3 times faster than US_ASCII (for ASCII Strings!)

 52688916/452574 = 116 😱 getBytes direct into a target array is 116 (!!!) times faster than manual for-loop-copy 🔥
*/
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
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
public class StringGetBytesBenchmark {
	private static final String BASE = "Some US-ASCII with few 128..255 and randomness × \u00A0 !";
	private static String data;// not final ⇒ not const

	static {// @Setup(Level.Trial) public void setUp() → doesn't work 🤔
		System.out.println(System.getProperty("java.specification.version"));
		data = BASE.repeat(50)+Math.random();
		System.out.println(data);

		String iso = new String(BASE.getBytes(ISO_8859_1), ISO_8859_1);
		System.out.println(iso);
		assert BASE.equals(iso);

		String us = new String(BASE.getBytes(US_ASCII), US_ASCII);
		System.out.println(us);
		assert "Some US-ASCII with few 128..255 and randomness ? ? !".equals(us);
	}

	@Benchmark  // simply to show the scale
	public byte[] utf8_4 () {
		return data.getBytes(StandardCharsets.UTF_8);
	}

	@Benchmark
	public byte[] iso88591_2 () {
		return data.getBytes(ISO_8859_1); // first 256 Unicode chars
	}

	@Benchmark
	public byte[] usAscii_3 () {
		return data.getBytes(US_ASCII); // first 128 Unicode chars
	}

	@Benchmark  @SuppressWarnings("deprecation")
	public byte[] rawIso88591_1 () {
		int len = data.length();
		byte[] bytes = new byte[len];
		data.getBytes(0, len, bytes, 0);// deprecated: first 256 Unicode chars
		return bytes;
	}

	/// getBytes can be used to copy directly into a target array
	@Benchmark  @SuppressWarnings("deprecation")
	public byte[] rawIso88591_direct () {
		int len = data.length();
		data.getBytes(0, len, targetArray, 0);
		return targetArray;
	}
	final byte[] targetArray = new byte[50_000];// in reality this is some array passed as argument

	@Benchmark // simply to show the scale
	public byte[] manualIso88591_5 () {
		int len = data.length();
		byte[] bytes = new byte[len];
		for (int i = 0; i < len; i++){
			bytes[i] = (byte) data.charAt(i);
		}
		return bytes;
	}


	public static void main (String[] args) throws RunnerException {
		StringGetBytesBenchmark x = new StringGetBytesBenchmark();
		assert Arrays.equals(x.iso88591_2(), x.manualIso88591_5());
		assert Arrays.equals(x.iso88591_2(), x.rawIso88591_1());

		Options opt = new OptionsBuilder()
			.include(StringGetBytesBenchmark.class.getSimpleName())
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