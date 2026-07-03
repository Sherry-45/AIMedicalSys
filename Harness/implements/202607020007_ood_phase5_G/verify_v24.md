# 验证报告（v24）

## 结果
FAILED

## 统计
- 通过：425（common 225 + ai-api 200）
- 失败：0（但 ai-impl 测试编译失败，14个编译错误，未运行任何测试）
- 跳过：5

## 测试执行日志
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] aimedical-sys                                                      [pom]
[INFO] common                                                             [jar]
[INFO] AI Module Aggregator                                               [pom]
[INFO] ai-api                                                             [jar]
[INFO] ai-impl                                                            [jar]
[INFO] 
[INFO] --------------------< com.aimedical:aimedical-sys >---------------------
[INFO] Building aimedical-sys 0.0.1-SNAPSHOT                              [1/5]
[INFO]   from pom.xml
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] 
[INFO] --- clean:3.3.2:clean (default-clean) @ aimedical-sys ---
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ aimedical-sys ---
[INFO] Skipping JaCoCo execution because property jacoco.skip is set.
[INFO] argLine set to empty
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ aimedical-sys ---
[INFO] Skipping JaCoCo execution because property jacoco.skip is set.
[INFO] 
[INFO] ------------------------< com.aimedical:common >------------------------
[INFO] Building common 0.0.1-SNAPSHOT                                     [2/5]
[INFO]   from common\pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.3.2:clean (default-clean) @ common ---
[INFO] Deleting C:\Develop\Software\AIMedicalSys\AIMedical\backend\common\target
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ common ---
[INFO] argLine set to -javaagent:C:\\Users\\laoE\\.m2\\repository\\org\\jacoco\\org.jacoco.agent\\0.8.12\\org.jacoco.agent-0.8.12-runtime.jar=destfile=C:\\Develop\\Software\\AIMedicalSys\\AIMedical\\backend\\common\\target\\jacoco.exec
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ common ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\common\src\main\resources
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\common\src\main\resources
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ common ---
[INFO] Changes detected - recompiling the module! :source
[INFO] Compiling 16 source files with javac [debug release 17] to target\classes
[WARNING] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/common/src/main/java/com/aimedical/common/entity/DosageStandard.java:[20,1] Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ common ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\common\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ common ---
[INFO] Changes detected - recompiling the module! :dependency
[INFO] Compiling 27 source files with javac [debug release 17] to target\test-classes
[INFO] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/common/src/test/java/com/aimedical/common/config/GlobalExceptionHandlerTest.java: C:\Develop\Software\AIMedicalSys\AIMedical\backend\common\src\test\java\com\aimedical\common\config\GlobalExceptionHandlerTest.javaʹ�û򸲸����ѹ�ʱ�� API��
[INFO] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/common/src/test/java/com/aimedical/common/config/GlobalExceptionHandlerTest.java: �й���ϸ��Ϣ, ��ʹ�� -Xlint:deprecation ���±��롣
[INFO] 
[INFO] --- surefire:3.1.2:test (default-test) @ common ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.aimedical.common.base.BaseEntityAuditTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

2026-07-02T15:21:00.268+08:00  INFO 30996 --- [           main] c.a.common.base.BaseEntityAuditTest      : Starting BaseEntityAuditTest using Java 21.0.11 with PID 30996 (started by laoE in C:\Develop\Software\AIMedicalSys\AIMedical\backend\common)
2026-07-02T15:21:00.271+08:00  INFO 30996 --- [           main] c.a.common.base.BaseEntityAuditTest      : No active profile set, falling back to 1 default profile: "default"
2026-07-02T15:21:00.863+08:00  INFO 30996 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-02T15:21:00.897+08:00  INFO 30996 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 22 ms. Found 0 JPA repository interfaces.
2026-07-02T15:21:00.983+08:00  INFO 30996 --- [           main] beddedDataSourceBeanFactoryPostProcessor : Replacing 'dataSource' DataSource bean with embedded version
2026-07-02T15:21:01.235+08:00  INFO 30996 --- [           main] o.s.j.d.e.EmbeddedDatabaseFactory        : Starting embedded database: url='jdbc:h2:mem:181fd872-1eb2-4458-a208-a07448e5575d;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', username='sa'
2026-07-02T15:21:01.860+08:00  INFO 30996 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-02T15:21:01.985+08:00  INFO 30996 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.4.4.Final
2026-07-02T15:21:02.049+08:00  INFO 30996 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-02T15:21:02.492+08:00  INFO 30996 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-02T15:21:03.955+08:00  INFO 30996 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: drop table if exists test_audit_entity cascade 
Hibernate: create table test_audit_entity (deleted boolean not null, created_at timestamp(6), id bigint generated by default as identity, updated_at timestamp(6), version bigint, primary key (id))
2026-07-02T15:21:04.007+08:00  INFO 30996 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-02T15:21:04.147+08:00  INFO 30996 --- [           main] c.a.common.base.BaseEntityAuditTest      : Started BaseEntityAuditTest in 4.484 seconds (process running for 6.931)
WARNING: A Java agent has been loaded dynamically (C:\Users\laoE\.m2\repository\net\bytebuddy\byte-buddy-agent\1.14.13\byte-buddy-agent-1.14.13.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
Hibernate: insert into test_audit_entity (created_at,deleted,updated_at,version,id) values (?,?,?,?,default)
Hibernate: update test_audit_entity set created_at=?,deleted=?,updated_at=?,version=? where id=? and version=?
Hibernate: insert into test_audit_entity (created_at,deleted,updated_at,version,id) values (?,?,?,?,default)
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.883 s -- in com.aimedical.common.base.BaseEntityAuditTest
[INFO] Running com.aimedical.common.base.BaseEntityTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.aimedical.common.base.BaseEntityTest
[INFO] Running com.aimedical.common.base.BaseEnumTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.common.base.BaseEnumTest
[INFO] Running com.aimedical.common.CommonPlaceholderTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.common.CommonPlaceholderTest
[INFO] Running com.aimedical.common.config.GlobalExceptionHandlerTest
2026-07-02T15:21:04.965+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=BIZ_ERR, message=ҵ���쳣
2026-07-02T15:21:04.979+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=ACCOUNT_LOCKED, message=�˻�����������30���Ӻ�����
2026-07-02T15:21:04.980+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=RATE_LIMITED, message=��¼���Թ���Ƶ�������Ժ�����
2026-07-02T15:21:04.982+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=RATE_LIMITED, message=��¼���Թ���Ƶ�������Ժ�����
2026-07-02T15:21:04.984+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=ACCOUNT_LOCKED, message=�˻�����������30���Ӻ�����
2026-07-02T15:21:04.986+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=BIZ_ERR, message=ҵ���쳣
2026-07-02T15:21:05.000+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Validation failed: ����У��ʧ��
2026-07-02T15:21:05.002+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=NUM_ERR, message=����ORD-001�ѹ��ڣ�ʣ��3��
2026-07-02T15:21:05.004+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=BIZ_ERR, message=ҵ���쳣
2026-07-02T15:21:05.006+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=NUM_ERR, message=����ORD-001�ѹ��ڣ�ʣ��3��
2026-07-02T15:21:05.008+08:00 ERROR 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : System exception

java.lang.RuntimeException: unexpected
	at com.aimedical.common.config.GlobalExceptionHandlerTest.shouldHandleGenericExceptionWith500(GlobalExceptionHandlerTest.java:263) ~[test-classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:728) ~[junit-platform-commons-1.10.2.jar:1.10.2]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$7(TestMethodTestDescriptor.java:218) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:214) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:139) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.1.2.jar:3.1.2]

2026-07-02T15:21:05.014+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Request body malformed

org.springframework.http.converter.HttpMessageNotReadableException: Malformed JSON
	at com.aimedical.common.config.GlobalExceptionHandlerTest.shouldHandleMessageNotReadableWith400(GlobalExceptionHandlerTest.java:215) ~[test-classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:728) ~[junit-platform-commons-1.10.2.jar:1.10.2]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$7(TestMethodTestDescriptor.java:218) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:214) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:139) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.1.2.jar:3.1.2]

2026-07-02T15:21:05.017+08:00  WARN 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=TOKEN_REFRESH_FAILED, message=����ˢ��ʧ�ܣ������µ�¼
2026-07-02T15:21:05.018+08:00 ERROR 30996 --- [           main] c.a.c.config.GlobalExceptionHandler      : Response body serialization failed

org.springframework.http.converter.HttpMessageNotWritableException: Serialization failed
	at com.aimedical.common.config.GlobalExceptionHandlerTest.shouldHandleMessageNotWritableWith500(GlobalExceptionHandlerTest.java:239) ~[test-classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:728) ~[junit-platform-commons-1.10.2.jar:1.10.2]
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$7(TestMethodTestDescriptor.java:218) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:214) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:139) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:69) ~[junit-jupiter-engine-5.10.2.jar:5.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596) ~[na:na]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54) ~[junit-platform-engine-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47) ~[junit-platform-launcher-1.10.2.jar:1.10.2]
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122) ~[surefire-junit-platform-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385) ~[surefire-booter-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162) ~[surefire-booter-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507) ~[surefire-booter-3.1.2.jar:3.1.2]
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495) ~[surefire-booter-3.1.2.jar:3.1.2]

[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.087 s -- in com.aimedical.common.config.GlobalExceptionHandlerTest
[INFO] Running com.aimedical.common.config.JacksonConfigTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.071 s -- in com.aimedical.common.config.JacksonConfigTest
[INFO] Running com.aimedical.common.config.JpaConfigTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.common.config.JpaConfigTest
[INFO] Running com.aimedical.common.entity.DosageStandardAuditTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

2026-07-02T15:21:05.134+08:00  INFO 30996 --- [           main] c.a.c.entity.DosageStandardAuditTest     : Starting DosageStandardAuditTest using Java 21.0.11 with PID 30996 (started by laoE in C:\Develop\Software\AIMedicalSys\AIMedical\backend\common)
2026-07-02T15:21:05.134+08:00  INFO 30996 --- [           main] c.a.c.entity.DosageStandardAuditTest     : No active profile set, falling back to 1 default profile: "default"
2026-07-02T15:21:05.222+08:00  INFO 30996 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-02T15:21:05.225+08:00  INFO 30996 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 2 ms. Found 0 JPA repository interfaces.
2026-07-02T15:21:05.234+08:00  INFO 30996 --- [           main] beddedDataSourceBeanFactoryPostProcessor : Replacing 'dataSource' DataSource bean with embedded version
2026-07-02T15:21:05.259+08:00  INFO 30996 --- [           main] o.s.j.d.e.EmbeddedDatabaseFactory        : Starting embedded database: url='jdbc:h2:mem:1c72e119-5074-4e98-b250-866a296d95b0;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', username='sa'
2026-07-02T15:21:05.281+08:00  INFO 30996 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-02T15:21:05.283+08:00  INFO 30996 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-02T15:21:05.287+08:00  INFO 30996 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-02T15:21:05.382+08:00  INFO 30996 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: drop table if exists dosage_standard cascade 
Hibernate: create table dosage_standard (age_range_end integer, age_range_start integer, daily_max numeric(12,3), deleted boolean not null, single_max numeric(12,3) not null, weight_range_end numeric(10,2), weight_range_start numeric(10,2), created_at timestamp(6), id bigint generated by default as identity, updated_at timestamp(6), version bigint, route_of_administration varchar(20) not null, unit varchar(20) not null, drug_code varchar(50) not null, primary key (id))
Hibernate: create index idx_dosage_drug_route on dosage_standard (drug_code, route_of_administration)
Hibernate: create index idx_dosage_drug_route_age_weight on dosage_standard (drug_code, route_of_administration, age_range_start, age_range_end, weight_range_start, weight_range_end)
2026-07-02T15:21:05.389+08:00  INFO 30996 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-02T15:21:05.421+08:00  INFO 30996 --- [           main] c.a.c.entity.DosageStandardAuditTest     : Started DosageStandardAuditTest in 0.307 seconds (process running for 8.205)
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: update dosage_standard set age_range_end=?,age_range_start=?,created_at=?,daily_max=?,deleted=?,drug_code=?,route_of_administration=?,single_max=?,unit=?,updated_at=?,version=?,weight_range_end=?,weight_range_start=? where id=? and version=?
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.439 s -- in com.aimedical.common.entity.DosageStandardAuditTest
[INFO] Running com.aimedical.common.entity.DosageStandardLombokTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.020 s -- in com.aimedical.common.entity.DosageStandardLombokTest
[INFO] Running com.aimedical.common.entity.DosageStandardTest
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.048 s -- in com.aimedical.common.entity.DosageStandardTest
[INFO] Running com.aimedical.common.exception.BusinessExceptionTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s -- in com.aimedical.common.exception.BusinessExceptionTest
[INFO] Running com.aimedical.common.exception.GlobalErrorCodeTest
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in com.aimedical.common.exception.GlobalErrorCodeTest
[INFO] Running com.aimedical.common.pom.AggregatorPomTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.093 s -- in com.aimedical.common.pom.AggregatorPomTest
[INFO] Running com.aimedical.common.pom.ApplicationPomTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in com.aimedical.common.pom.ApplicationPomTest
[INFO] Running com.aimedical.common.pom.CommonPomTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in com.aimedical.common.pom.CommonPomTest
[INFO] Running com.aimedical.common.pom.MovedModulePomTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.034 s -- in com.aimedical.common.pom.MovedModulePomTest
[INFO] Running com.aimedical.common.pom.MovedModulePomVerificationTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in com.aimedical.common.pom.MovedModulePomVerificationTest
[INFO] Running com.aimedical.common.pom.NewModulePomTest
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.035 s -- in com.aimedical.common.pom.NewModulePomTest
[INFO] Running com.aimedical.common.pom.ParentPomDependencyManagementCleanupTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s -- in com.aimedical.common.pom.ParentPomDependencyManagementCleanupTest
[INFO] Running com.aimedical.common.pom.ParentPomModuleRegistrationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.022 s -- in com.aimedical.common.pom.ParentPomModuleRegistrationTest
[INFO] Running com.aimedical.common.pom.ParentPomTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in com.aimedical.common.pom.ParentPomTest
[INFO] Running com.aimedical.common.pom.ParentPomVerificationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in com.aimedical.common.pom.ParentPomVerificationTest
[INFO] Running com.aimedical.common.pom.ParentPomVersionTest
[WARNING] Tests run: 5, Failures: 0, Errors: 0, Skipped: 5, Time elapsed: 0 s -- in com.aimedical.common.pom.ParentPomVersionTest
[INFO] Running com.aimedical.common.result.PageQueryTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.031 s -- in com.aimedical.common.result.PageQueryTest
[INFO] Running com.aimedical.common.result.PageResponseTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.common.result.PageResponseTest
[INFO] Running com.aimedical.common.result.ResultTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.015 s -- in com.aimedical.common.result.ResultTest
[INFO] Running com.aimedical.common.util.SimpleMessageInterpolatorTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.common.util.SimpleMessageInterpolatorTest
[INFO] 
[INFO] Results:
[INFO] 
[WARNING] Tests run: 225, Failures: 0, Errors: 0, Skipped: 5
[INFO] 
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ common ---
[INFO] Loading execution data file C:\Develop\Software\AIMedicalSys\AIMedical\backend\common\target\jacoco.exec
[INFO] Analyzed bundle 'common' with 10 classes
[INFO] 
[INFO] --------------------------< com.aimedical:ai >--------------------------
[INFO] Building AI Module Aggregator 0.0.1-SNAPSHOT                       [3/5]
[INFO]   from modules\ai\pom.xml
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] 
[INFO] --- clean:3.3.2:clean (default-clean) @ ai ---
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ ai ---
[INFO] Skipping JaCoCo execution because property jacoco.skip is set.
[INFO] argLine set to empty
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ ai ---
[INFO] Skipping JaCoCo execution because property jacoco.skip is set.
[INFO] 
[INFO] ------------------------< com.aimedical:ai-api >------------------------
[INFO] Building ai-api 0.0.1-SNAPSHOT                                     [4/5]
[INFO]   from modules\ai\ai-api\pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.3.2:clean (default-clean) @ ai-api ---
[INFO] Deleting C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-api\target
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ ai-api ---
[INFO] argLine set to -javaagent:C:\\Users\\laoE\\.m2\\repository\\org\\jacoco\\org.jacoco.agent\\0.8.12\\org.jacoco.agent-0.8.12-runtime.jar=destfile=C:\\Develop\\Software\\AIMedicalSys\\AIMedical\\backend\\modules\\ai\\ai-api\\target\\jacoco.exec
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ ai-api ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-api\src\main\resources
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-api\src\main\resources
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ ai-api ---
[INFO] Changes detected - recompiling the module! :dependency
[INFO] Compiling 51 source files with javac [debug release 17] to target\classes
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ ai-api ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-api\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ ai-api ---
[INFO] Changes detected - recompiling the module! :dependency
[INFO] Compiling 15 source files with javac [debug release 17] to target\test-classes
[INFO] 
[INFO] --- surefire:3.1.2:test (default-test) @ ai-api ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.aimedical.modules.ai.api.AiResultFactoryTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.130 s -- in com.aimedical.modules.ai.api.AiResultFactoryTest
[INFO] Running com.aimedical.modules.ai.api.AiResultTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.022 s -- in com.aimedical.modules.ai.api.AiResultTest
[INFO] Running com.aimedical.modules.ai.api.AiServiceTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.063 s -- in com.aimedical.modules.ai.api.AiServiceTest
[INFO] Running com.aimedical.modules.ai.api.degradation.DegradationContextTest
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.030 s -- in com.aimedical.modules.ai.api.degradation.DegradationContextTest
[INFO] Running com.aimedical.modules.ai.api.degradation.DegradationReasonTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.033 s -- in com.aimedical.modules.ai.api.degradation.DegradationReasonTest
[INFO] Running com.aimedical.modules.ai.api.degradation.DegradationStrategyTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in com.aimedical.modules.ai.api.degradation.DegradationStrategyTest
[INFO] Running com.aimedical.modules.ai.api.dto.base.CallContextTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.560 s -- in com.aimedical.modules.ai.api.dto.base.CallContextTest
[INFO] Running com.aimedical.modules.ai.api.dto.base.Phase4BusinessExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.021 s -- in com.aimedical.modules.ai.api.dto.base.Phase4BusinessExceptionTest
[INFO] Running com.aimedical.modules.ai.api.dto.base.Phase4ServiceMetaCapableTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.ai.api.dto.base.Phase4ServiceMetaCapableTest
[INFO] Running com.aimedical.modules.ai.api.dto.base.Phase4ServiceMetaTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in com.aimedical.modules.ai.api.dto.base.Phase4ServiceMetaTest
[INFO] Running com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequestTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequestTest
[INFO] Running com.aimedical.modules.ai.api.dto.discussion.DiscussionTranscriptTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.api.dto.discussion.DiscussionTranscriptTest
[INFO] Running com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordDtoTest
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.020 s -- in com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordDtoTest
[INFO] Running com.aimedical.modules.ai.api.dto.prescription.PrescriptionDtoTest
[INFO] Tests run: 33, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.035 s -- in com.aimedical.modules.ai.api.dto.prescription.PrescriptionDtoTest
[INFO] Running com.aimedical.modules.ai.api.dto.triage.TriageDtoTest
[INFO] Tests run: 38, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.023 s -- in com.aimedical.modules.ai.api.dto.triage.TriageDtoTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 200, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ ai-api ---
[INFO] Loading execution data file C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-api\target\jacoco.exec
[INFO] Analyzed bundle 'ai-api' with 50 classes
[INFO] 
[INFO] -----------------------< com.aimedical:ai-impl >------------------------
[INFO] Building ai-impl 0.0.1-SNAPSHOT                                    [5/5]
[INFO]   from modules\ai\ai-impl\pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.3.2:clean (default-clean) @ ai-impl ---
[INFO] Deleting C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\target
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ ai-impl ---
[INFO] argLine set to -javaagent:C:\\Users\\laoE\\.m2\\repository\\org\\jacoco\\org.jacoco.agent\\0.8.12\\org.jacoco.agent-0.8.12-runtime.jar=destfile=C:\\Develop\\Software\\AIMedicalSys\\AIMedical\\backend\\modules\\ai\\ai-impl\\target\\jacoco.exec
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ ai-impl ---
[INFO] Copying 0 resource from src\main\resources to target\classes
[INFO] Copying 1 resource from src\main\resources to target\classes
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ ai-impl ---
[INFO] Changes detected - recompiling the module! :dependency
[INFO] Compiling 88 source files with javac [debug release 17] to target\classes
[INFO] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutor.java: C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\src\main\java\com\aimedical\modules\ai\impl\orchestrator\AbstractCapabilityExecutor.javaʹ����δ�����򲻰�ȫ�Ĳ�����
[INFO] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutor.java: �й���ϸ��Ϣ, ��ʹ�� -Xlint:unchecked ���±��롣
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ ai-impl ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ ai-impl ---
[INFO] Changes detected - recompiling the module! :dependency
[INFO] Compiling 62 source files with javac [debug release 17] to target\test-classes
[INFO] ĳЩ��Ϣ�Ѿ�����; ��ʹ�� -Xdiags:verbose ���±����Ի���������
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR : 
[INFO] -------------------------------------------------------------
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/RecommendExecutionOrderCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/RecommendExaminationCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/ImageAnalysisCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java:[273,59] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java:[336,59] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java:[375,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java:[415,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutorTest.java:[1471,17] �����ݵ�����: java.util.Map<java.lang.String,java.time.Duration>�޷�ת��Ϊjava.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiagnosisCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/AnalysisReportForInspectionCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/TriageCapabilityExecutorTest.java:[85,45] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/TriageCapabilityExecutorTest.java:[104,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/router/DefaultModelRouterTest.java:[20,25] �����ݵ�����: java.util.Map<java.lang.String,java.util.List<com.aimedical.modules.ai.impl.router.ModelRoute>>�޷�ת��Ϊjava.util.Map<java.lang.String,java.util.List<com.aimedical.modules.ai.impl.config.ModelRouteConfig>>
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/AnalysisReportForLabTestCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[INFO] 14 errors 
[INFO] -------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for aimedical-sys 0.0.1-SNAPSHOT:
[INFO] 
[INFO] aimedical-sys ...................................... SUCCESS [  0.534 s]
[INFO] common ............................................. SUCCESS [ 14.837 s]
[INFO] AI Module Aggregator ............................... SUCCESS [  0.008 s]
[INFO] ai-api ............................................. SUCCESS [  5.060 s]
[INFO] ai-impl ............................................ FAILURE [  4.555 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  25.403 s
[INFO] Finished at: 2026-07-02T15:21:16+08:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:testCompile (default-testCompile) on project ai-impl: Compilation failure: Compilation failure: 
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/RecommendExecutionOrderCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/RecommendExaminationCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/ImageAnalysisCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java:[273,59] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java:[336,59] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java:[375,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java:[415,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutorTest.java:[1471,17] �����ݵ�����: java.util.Map<java.lang.String,java.time.Duration>�޷�ת��Ϊjava.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiagnosisCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/AnalysisReportForInspectionCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/TriageCapabilityExecutorTest.java:[85,45] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/TriageCapabilityExecutorTest.java:[104,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/router/DefaultModelRouterTest.java:[20,25] �����ݵ�����: java.util.Map<java.lang.String,java.util.List<com.aimedical.modules.ai.impl.router.ModelRoute>>�޷�ת��Ϊjava.util.Map<java.lang.String,java.util.List<com.aimedical.modules.ai.impl.config.ModelRouteConfig>>
[ERROR] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/AnalysisReportForLabTestCapabilityExecutorTest.java:[95,16] �����ݵ�����: ���������ͱ���K,V��ʵ��, ��ʹjava.util.Map<K,V>��java.util.concurrent.atomic.AtomicReference<java.util.Map<java.lang.String,java.time.Duration>>һ��
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
[ERROR] 
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <args> -rf :ai-impl
