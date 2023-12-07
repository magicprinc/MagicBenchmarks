# benchmarks4vertx

## Jackson Base64 _-vs-_ JDK Base64
```
JMH 1.37
JDK 17.0.9, OpenJDK 64-Bit Server VM, 17.0.9+11-LTS
JDK 21.0.1, OpenJDK 64-Bit Server VM, 21.0.1+12-LTS
Jackson 2.16.0
```
### JDK 17
```
Benchmark                                       Mode  Cnt   Score   Error  Units
JacksonBase64.testDefaultJacksonBase64         thrpt   10  21,129 ± 0,551  ops/s
JacksonBase64.testDefaultJacksonBase64AndBack  thrpt   10  10,315 ± 0,146  ops/s
JacksonBase64.testVertxBase64                  thrpt   10  20,442 ± 2,851  ops/s
JacksonBase64.testVertxBase64AndBack           thrpt   10   7,574 ± 0,210  ops/s
```
### JDK 21
```
Benchmark                                       Mode  Cnt   Score   Error  Units
JacksonBase64.testDefaultJacksonBase64         thrpt   10  23,342 ± 0,325  ops/s
JacksonBase64.testDefaultJacksonBase64AndBack  thrpt   10  10,967 ± 0,217  ops/s
JacksonBase64.testVertxBase64                  thrpt   10  23,784 ± 0,906  ops/s
JacksonBase64.testVertxBase64AndBack           thrpt   10  10,135 ± 0,068  ops/s
```
### JDK 17 jackson-module-blackbird
```
Benchmark                                       Mode  Cnt   Score   Error  Units
JacksonBase64.testDefaultJacksonBase64         thrpt   10  24,628 ± 1,056  ops/s
JacksonBase64.testDefaultJacksonBase64AndBack  thrpt   10  10,471 ± 0,065  ops/s
JacksonBase64.testVertxBase64                  thrpt   10  22,038 ± 0,247  ops/s
JacksonBase64.testVertxBase64AndBack           thrpt   10   7,917 ± 0,050  ops/s
```
### JDK 21 jackson-module-blackbird
```
Benchmark                                       Mode  Cnt   Score   Error  Units
JacksonBase64.testDefaultJacksonBase64         thrpt   10  22,977 ± 0,291  ops/s
JacksonBase64.testDefaultJacksonBase64AndBack  thrpt   10  11,243 ± 0,044  ops/s
JacksonBase64.testVertxBase64                  thrpt   10  24,319 ± 0,205  ops/s
JacksonBase64.testVertxBase64AndBack           thrpt   10  10,066 ± 0,115  ops/s
```