package benchmark;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import io.smallrye.mutiny.unchecked.UncheckedConsumer;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.google.common.base.Verify.verify;

/**
 Jackson Base64
 -vs-
 Vert.x/JDK Base64

 @see DatabindCodec#mapper
 @see org.openjdk.jmh.annotations.Benchmark
 */
@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@BenchmarkMode(Mode.Throughput) // Mode.AverageTime
@Fork(value = 1, jvmArgs = {
	"-XX:+UseParallelGC", "-XX:+UseCompressedOops", "-XX:+DoEscapeAnalysis", "-Xmx4G"
	//, "-XX:CompileCommand=inline,java/lang/String.charAt"
})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Slf4j
public class JacksonBase64 {

	static final List<Map<String,byte[]>> SAMPLES = IntStream.range(50_000, 50_100).mapToObj(i->{
			var bytes = new byte[i*4];
			ThreadLocalRandom.current().nextBytes(bytes);
			return Map.of(Integer.toString(i), bytes);
		}).toList();

	static final TypeReference<Map<String,byte[]>> MAP_TYPE = new TypeReference<>(){};

	static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.setBase64Variant(Base64Variants.MODIFIED_FOR_URL);// -_ instead of +/
		mapper.findAndRegisterModules();// for blackbird
		DatabindCodec.mapper().findAndRegisterModules();
	}

	public static <T> Consumer<T> consume (@NonNull UncheckedConsumer<T> lambda) {
		return lambda.toConsumer();
	}

	@Benchmark
	public void testVertxBase64 () {
		SAMPLES.forEach(consume(map->{
			String s = DatabindCodec.mapper().writeValueAsString(map);
			verify(s.length() > 100);
		}));
	}

	@Benchmark
	public void testDefaultJacksonBase64 () {
		SAMPLES.forEach(consume(map->{
			String s = mapper.writeValueAsString(map);
			verify(s.length() > 100);
		}));
	}

	@Benchmark
	public void testVertxBase64AndBack () {
		SAMPLES.forEach(consume(map->{
			var s = DatabindCodec.mapper().writeValueAsString(map);
			var map2 = DatabindCodec.mapper().readValue(s, MAP_TYPE);
			verify((map.size() == 1) && (map2.size() == 1));
			verify(Arrays.equals(map.values().iterator().next(), map2.values().iterator().next()));
		}));
	}

	@Benchmark
	public void testDefaultJacksonBase64AndBack () {
		SAMPLES.forEach(consume(map->{
			var s = mapper.writeValueAsString(map);
			var map2 = mapper.readValue(s, MAP_TYPE);
			verify((map.size() == 1) && (map2.size() == 1));
			verify(Arrays.equals(map.values().iterator().next(), map2.values().iterator().next()));
		}));
	}

	public static void main (String[] args) throws RunnerException, JsonProcessingException {
		SAMPLES.forEach(consume(map->
			Preconditions.checkState(Objects.equals(
					DatabindCodec.mapper().writeValueAsString(map),
					mapper.writeValueAsString(map)
			))));
		log.info("Same Json!");

		Options opt = new OptionsBuilder()
			.include(JacksonBase64.class.getSimpleName())
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