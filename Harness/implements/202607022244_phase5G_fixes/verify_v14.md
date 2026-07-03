# 验证报告（v14）

## 结果
FAILED

## 统计
- 通过：569
- 失败：2
- 错误：1
- 跳过：0

## 失败详情
### AiRouterPropertiesTest（v14 新引入问题）
- `shouldLogWarningForInvalidClientType:73` — AssertionFailedError: expected: `<true>` but was: `<false>`
- `shouldLogWarningForInvalidAuthType:95` — AssertionFailedError: expected: `<true>` but was: `<false>`
- `shouldHandleNullConfigGracefully:160` — NullPointerException

## 测试执行日志
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] aimedical-sys                                                      [pom]
[INFO] common                                                             [jar]
[INFO] Common Module Aggregator                                           [pom]
[INFO] common-module-api                                                  [jar]
[INFO] common-module-impl                                                 [jar]
[INFO] AI Module Aggregator                                               [pom]
[INFO] ai-api                                                             [jar]
[INFO] ai-impl                                                            [jar]
[INFO] patient                                                            [jar]
[INFO] doctor                                                             [jar]
[INFO] admin                                                              [jar]
[INFO] registration                                                       [jar]
[INFO] medical-order                                                      [jar]
[INFO] consultation                                                       [jar]
[INFO] prescription                                                       [jar]
[INFO] medical-record                                                     [jar]
[INFO] application                                                        [jar]
[INFO] integration                                                        [jar]
[INFO] 
[INFO] --------------------< com.aimedical:aimedical-sys >---------------------
[INFO] Building aimedical-sys 0.0.1-SNAPSHOT                             [1/18]
[INFO]   from pom.xml
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ aimedical-sys ---
[INFO] Skipping JaCoCo execution because property jacoco.skip is set.
[INFO] argLine set to empty
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ aimedical-sys ---
[INFO] Skipping JaCoCo execution because property jacoco.skip is set.
[INFO] 
[INFO] ------------------------< com.aimedical:common >------------------------
[INFO] Building common 0.0.1-SNAPSHOT                                    [2/18]
[INFO]   from common\pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ common ---
[INFO] argLine set to -javaagent:C:\\Users\\laoE\\.m2\\repository\\org\\jacoco\\org.jacoco.agent\\0.8.12\\org.jacoco.agent-0.8.12-runtime.jar=destfile=C:\\Develop\\Software\\AIMedicalSys\\AIMedical\\backend\\common\\target\\jacoco.exec
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ common ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\common\src\main\resources
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\common\src\main\resources
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ common ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ common ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\common\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ common ---
[INFO] Nothing to compile - all classes are up to date
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

2026-07-03T04:18:38.993+08:00  INFO 4344 --- [           main] c.a.common.base.BaseEntityAuditTest      : Starting BaseEntityAuditTest using Java 21.0.11 with PID 4344 (started by laoE in C:\Develop\Software\AIMedicalSys\AIMedical\backend\common)
2026-07-03T04:18:38.996+08:00  INFO 4344 --- [           main] c.a.common.base.BaseEntityAuditTest      : No active profile set, falling back to 1 default profile: "default"
2026-07-03T04:18:39.458+08:00  INFO 4344 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-03T04:18:39.482+08:00  INFO 4344 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 16 ms. Found 0 JPA repository interfaces.
2026-07-03T04:18:39.537+08:00  INFO 4344 --- [           main] beddedDataSourceBeanFactoryPostProcessor : Replacing 'dataSource' DataSource bean with embedded version
2026-07-03T04:18:39.735+08:00  INFO 4344 --- [           main] o.s.j.d.e.EmbeddedDatabaseFactory        : Starting embedded database: url='jdbc:h2:mem:8f584cdf-a08f-4f2f-a9b9-c307b53ca658;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', username='sa'
2026-07-03T04:18:40.142+08:00  INFO 4344 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-03T04:18:40.217+08:00  INFO 4344 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.4.4.Final
2026-07-03T04:18:40.264+08:00  INFO 4344 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-03T04:18:40.560+08:00  INFO 4344 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-03T04:18:41.681+08:00  INFO 4344 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: drop table if exists test_audit_entity cascade 
Hibernate: create table test_audit_entity (deleted boolean not null, created_at timestamp(6), id bigint generated by default as identity, updated_at timestamp(6), version bigint, primary key (id))
2026-07-03T04:18:41.723+08:00  INFO 4344 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-03T04:18:41.849+08:00  INFO 4344 --- [           main] c.a.common.base.BaseEntityAuditTest      : Started BaseEntityAuditTest in 3.297 seconds (process running for 4.755)
WARNING: A Java agent has been loaded dynamically (C:\Users\laoE\.m2\repository\net\bytebuddy\byte-buddy-agent\1.14.13\byte-buddy-agent-1.14.13.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
Hibernate: insert into test_audit_entity (created_at,deleted,updated_at,version,id) values (?,?,?,?,default)
Hibernate: update test_audit_entity set created_at=?,deleted=?,updated_at=?,version=? where id=? and version=?
Hibernate: insert into test_audit_entity (created_at,deleted,updated_at,version,id) values (?,?,?,?,default)
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.531 s -- in com.aimedical.common.base.BaseEntityAuditTest
[INFO] Running com.aimedical.common.base.BaseEntityTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.aimedical.common.base.BaseEntityTest
[INFO] Running com.aimedical.common.base.BaseEnumTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in com.aimedical.common.base.BaseEnumTest
[INFO] Running com.aimedical.common.CommonPlaceholderTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in com.aimedical.common.CommonPlaceholderTest
[INFO] Running com.aimedical.common.config.GlobalExceptionHandlerTest
2026-07-03T04:18:42.635+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=BIZ_ERR, message=ҵ���쳣
2026-07-03T04:18:42.647+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=ACCOUNT_LOCKED, message=�˻�����������30���Ӻ�����
2026-07-03T04:18:42.648+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=RATE_LIMITED, message=��¼���Թ���Ƶ�������Ժ�����
2026-07-03T04:18:42.649+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=RATE_LIMITED, message=��¼���Թ���Ƶ�������Ժ�����
2026-07-03T04:18:42.650+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=ACCOUNT_LOCKED, message=�˻�����������30���Ӻ�����
2026-07-03T04:18:42.651+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=BIZ_ERR, message=ҵ���쳣
2026-07-03T04:18:42.660+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Validation failed: ����У��ʧ��
2026-07-03T04:18:42.662+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=NUM_ERR, message=����ORD-001�ѹ��ڣ�ʣ��3��
2026-07-03T04:18:42.664+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=BIZ_ERR, message=ҵ���쳣
2026-07-03T04:18:42.666+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=NUM_ERR, message=����ORD-001�ѹ��ڣ�ʣ��3��
2026-07-03T04:18:42.668+08:00 ERROR 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : System exception

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

2026-07-03T04:18:42.675+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Request body malformed

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

2026-07-03T04:18:42.678+08:00  WARN 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Business exception: code=TOKEN_REFRESH_FAILED, message=����ˢ��ʧ�ܣ������µ�¼
2026-07-03T04:18:42.680+08:00 ERROR 4344 --- [           main] c.a.c.config.GlobalExceptionHandler      : Response body serialization failed

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

[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.053 s -- in com.aimedical.common.config.GlobalExceptionHandlerTest
[INFO] Running com.aimedical.common.config.JacksonConfigTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.058 s -- in com.aimedical.common.config.JacksonConfigTest
[INFO] Running com.aimedical.common.config.JpaConfigTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.common.config.JpaConfigTest
[INFO] Running com.aimedical.common.entity.DosageStandardAuditTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

2026-07-03T04:18:42.773+08:00  INFO 4344 --- [           main] c.a.c.entity.DosageStandardAuditTest     : Starting DosageStandardAuditTest using Java 21.0.11 with PID 4344 (started by laoE in C:\Develop\Software\AIMedicalSys\AIMedical\backend\common)
2026-07-03T04:18:42.773+08:00  INFO 4344 --- [           main] c.a.c.entity.DosageStandardAuditTest     : No active profile set, falling back to 1 default profile: "default"
2026-07-03T04:18:42.835+08:00  INFO 4344 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-03T04:18:42.838+08:00  INFO 4344 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 2 ms. Found 0 JPA repository interfaces.
2026-07-03T04:18:42.848+08:00  INFO 4344 --- [           main] beddedDataSourceBeanFactoryPostProcessor : Replacing 'dataSource' DataSource bean with embedded version
2026-07-03T04:18:42.864+08:00  INFO 4344 --- [           main] o.s.j.d.e.EmbeddedDatabaseFactory        : Starting embedded database: url='jdbc:h2:mem:8286eb6b-f1e0-49ff-a343-5d6ae50e9db9;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', username='sa'
2026-07-03T04:18:42.883+08:00  INFO 4344 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-03T04:18:42.885+08:00  INFO 4344 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-03T04:18:42.889+08:00  INFO 4344 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-03T04:18:42.977+08:00  INFO 4344 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: drop table if exists dosage_standard cascade 
Hibernate: create table dosage_standard (age_range_end integer, age_range_start integer, daily_max numeric(12,3), deleted boolean not null, single_max numeric(12,3) not null, weight_range_end numeric(10,2), weight_range_start numeric(10,2), created_at timestamp(6), id bigint generated by default as identity, updated_at timestamp(6), version bigint, route_of_administration varchar(20) not null, unit varchar(20) not null, drug_code varchar(50) not null, primary key (id))
Hibernate: create index idx_dosage_drug_route on dosage_standard (drug_code, route_of_administration)
Hibernate: create index idx_dosage_drug_route_age_weight on dosage_standard (drug_code, route_of_administration, age_range_start, age_range_end, weight_range_start, weight_range_end)
2026-07-03T04:18:42.984+08:00  INFO 4344 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-03T04:18:43.007+08:00  INFO 4344 --- [           main] c.a.c.entity.DosageStandardAuditTest     : Started DosageStandardAuditTest in 0.251 seconds (process running for 5.914)
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: update dosage_standard set age_range_end=?,age_range_start=?,created_at=?,daily_max=?,deleted=?,drug_code=?,route_of_administration=?,single_max=?,unit=?,updated_at=?,version=?,weight_range_end=?,weight_range_start=? where id=? and version=?
Hibernate: insert into dosage_standard (age_range_end,age_range_start,created_at,daily_max,deleted,drug_code,route_of_administration,single_max,unit,updated_at,version,weight_range_end,weight_range_start,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,default)
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.364 s -- in com.aimedical.common.entity.DosageStandardAuditTest
[INFO] Running com.aimedical.common.entity.DosageStandardLombokTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s -- in com.aimedical.common.entity.DosageStandardLombokTest
[INFO] Running com.aimedical.common.entity.DosageStandardTest
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.039 s -- in com.aimedical.common.entity.DosageStandardTest
[INFO] Running com.aimedical.common.exception.BusinessExceptionTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.common.exception.BusinessExceptionTest
[INFO] Running com.aimedical.common.exception.GlobalErrorCodeTest
[INFO] Tests run: 26, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in com.aimedical.common.exception.GlobalErrorCodeTest
[INFO] Running com.aimedical.common.pom.AggregatorPomTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.058 s -- in com.aimedical.common.pom.AggregatorPomTest
[INFO] Running com.aimedical.common.pom.ApplicationPomTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s -- in com.aimedical.common.pom.ApplicationPomTest
[INFO] Running com.aimedical.common.pom.CommonPomTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.aimedical.common.pom.CommonPomTest
[INFO] Running com.aimedical.common.pom.MovedModulePomTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.029 s -- in com.aimedical.common.pom.MovedModulePomTest
[INFO] Running com.aimedical.common.pom.MovedModulePomVerificationTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in com.aimedical.common.pom.MovedModulePomVerificationTest
[INFO] Running com.aimedical.common.pom.NewModulePomTest
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.025 s -- in com.aimedical.common.pom.NewModulePomTest
[INFO] Running com.aimedical.common.pom.ParentPomDependencyManagementCleanupTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in com.aimedical.common.pom.ParentPomDependencyManagementCleanupTest
[INFO] Running com.aimedical.common.pom.ParentPomModuleRegistrationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in com.aimedical.common.pom.ParentPomModuleRegistrationTest
[INFO] Running com.aimedical.common.pom.ParentPomTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in com.aimedical.common.pom.ParentPomTest
[INFO] Running com.aimedical.common.pom.ParentPomVerificationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s -- in com.aimedical.common.pom.ParentPomVerificationTest
[INFO] Running com.aimedical.common.pom.ParentPomVersionTest
[WARNING] Tests run: 5, Failures: 0, Errors: 0, Skipped: 5, Time elapsed: 0 s -- in com.aimedical.common.pom.ParentPomVersionTest
[INFO] Running com.aimedical.common.result.PageQueryTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.025 s -- in com.aimedical.common.result.PageQueryTest
[INFO] Running com.aimedical.common.result.PageResponseTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.common.result.PageResponseTest
[INFO] Running com.aimedical.common.result.ResultTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.common.result.ResultTest
[INFO] Running com.aimedical.common.util.SimpleMessageInterpolatorTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.common.util.SimpleMessageInterpolatorTest
[INFO] 
[INFO] Results:
[INFO] 
[WARNING] Tests run: 226, Failures: 0, Errors: 0, Skipped: 5
[INFO] 
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ common ---
[INFO] Loading execution data file C:\Develop\Software\AIMedicalSys\AIMedical\backend\common\target\jacoco.exec
[INFO] Analyzed bundle 'common' with 11 classes
[INFO] 
[INFO] --------------------< com.aimedical:common-module >---------------------
[INFO] Building Common Module Aggregator 0.0.1-SNAPSHOT                  [3/18]
[INFO]   from modules\common-module\pom.xml
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ common-module ---
[INFO] Skipping JaCoCo execution because property jacoco.skip is set.
[INFO] argLine set to empty
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ common-module ---
[INFO] Skipping JaCoCo execution because property jacoco.skip is set.
[INFO] 
[INFO] ------------------< com.aimedical:common-module-api >-------------------
[INFO] Building common-module-api 0.0.1-SNAPSHOT                         [4/18]
[INFO]   from modules\common-module\common-module-api\pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ common-module-api ---
[INFO] argLine set to -javaagent:C:\\Users\\laoE\\.m2\\repository\\org\\jacoco\\org.jacoco.agent\\0.8.12\\org.jacoco.agent-0.8.12-runtime.jar=destfile=C:\\Develop\\Software\\AIMedicalSys\\AIMedical\\backend\\modules\\common-module\\common-module-api\\target\\jacoco.exec
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ common-module-api ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\common-module\common-module-api\src\main\resources
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\common-module\common-module-api\src\main\resources
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ common-module-api ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ common-module-api ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\common-module\common-module-api\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ common-module-api ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- surefire:3.1.2:test (default-test) @ common-module-api ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.aimedical.modules.commonmodule.api.AuthErrorCodeTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.082 s -- in com.aimedical.modules.commonmodule.api.AuthErrorCodeTest
[INFO] Running com.aimedical.modules.commonmodule.api.dto.CurrentUserResponseTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.aimedical.modules.commonmodule.api.dto.CurrentUserResponseTest
[INFO] Running com.aimedical.modules.commonmodule.api.dto.LoginRequestTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in com.aimedical.modules.commonmodule.api.dto.LoginRequestTest
[INFO] Running com.aimedical.modules.commonmodule.api.dto.RegisterRequestTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.aimedical.modules.commonmodule.api.dto.RegisterRequestTest
[INFO] Running com.aimedical.modules.commonmodule.api.dto.TokenResponseTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in com.aimedical.modules.commonmodule.api.dto.TokenResponseTest
[INFO] Running com.aimedical.modules.commonmodule.api.dto.UserDtoTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in com.aimedical.modules.commonmodule.api.dto.UserDtoTest
[INFO] Running com.aimedical.modules.commonmodule.api.PositionEnumTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in com.aimedical.modules.commonmodule.api.PositionEnumTest
[INFO] Running com.aimedical.modules.commonmodule.api.UserTypeTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in com.aimedical.modules.commonmodule.api.UserTypeTest
[INFO] Running com.aimedical.modules.commonmodule.auth.UserInfoResponseTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.020 s -- in com.aimedical.modules.commonmodule.auth.UserInfoResponseTest
[INFO] Running com.aimedical.modules.commonmodule.doctor.AvailableDoctorTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.030 s -- in com.aimedical.modules.commonmodule.doctor.AvailableDoctorTest
[INFO] Running com.aimedical.modules.commonmodule.doctor.DoctorFacadeTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.modules.commonmodule.doctor.DoctorFacadeTest
[INFO] Running com.aimedical.modules.commonmodule.drug.DrugFacadeTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.commonmodule.drug.DrugFacadeTest
[INFO] Running com.aimedical.modules.commonmodule.drug.DrugInfoTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.013 s -- in com.aimedical.modules.commonmodule.drug.DrugInfoTest
[INFO] Running com.aimedical.modules.commonmodule.event.RegistrationEventTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in com.aimedical.modules.commonmodule.event.RegistrationEventTest
[INFO] Running com.aimedical.modules.commonmodule.store.impl.ConcurrentHashMapStoreTest
[INFO] Tests run: 33, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.062 s -- in com.aimedical.modules.commonmodule.store.impl.ConcurrentHashMapStoreTest
[INFO] Running com.aimedical.modules.commonmodule.store.impl.DraftContextStoreImplTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in com.aimedical.modules.commonmodule.store.impl.DraftContextStoreImplTest
[INFO] Running com.aimedical.modules.commonmodule.visit.VisitFacadeTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.commonmodule.visit.VisitFacadeTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 152, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ common-module-api ---
[INFO] Loading execution data file C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\common-module\common-module-api\target\jacoco.exec
[INFO] Analyzed bundle 'common-module-api' with 16 classes
[INFO] 
[INFO] ------------------< com.aimedical:common-module-impl >------------------
[INFO] Building common-module-impl 0.0.1-SNAPSHOT                        [5/18]
[INFO]   from modules\common-module\common-module-impl\pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ common-module-impl ---
[INFO] argLine set to -javaagent:C:\\Users\\laoE\\.m2\\repository\\org\\jacoco\\org.jacoco.agent\\0.8.12\\org.jacoco.agent-0.8.12-runtime.jar=destfile=C:\\Develop\\Software\\AIMedicalSys\\AIMedical\\backend\\modules\\common-module\\common-module-impl\\target\\jacoco.exec
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ common-module-impl ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\common-module\common-module-impl\src\main\resources
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\common-module\common-module-impl\src\main\resources
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ common-module-impl ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ common-module-impl ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\common-module\common-module-impl\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ common-module-impl ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- surefire:3.1.2:test (default-test) @ common-module-impl ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.aimedical.modules.commonmodule.auth.audit.LoggingSecurityAuditLoggerTest
04:18:46.930 [main] WARN com.aimedical.modules.commonmodule.auth.audit.LoggingSecurityAuditLogger -- Audit log write failed: Cannot invoke "com.aimedical.modules.commonmodule.auth.audit.SecurityAuditEvent.timestamp()" because "event" is null
04:18:46.956 [main] INFO SECURITY_AUDIT -- timestamp=2026-07-03T04:18:46.956 eventType=LOGIN_FAILED userId=null username=null clientIp=10.0.0.1 success=false failureReason=BAD_CREDENTIALS refreshTokenMasked=abc123*** newJti=new-jti
04:18:46.960 [main] INFO SECURITY_AUDIT -- timestamp=2026-07-03T04:18:46.96 eventType=LOGIN_FAILED userId=null username=null clientIp=10.0.0.1 success=false failureReason=USER_NOT_FOUND
04:18:46.964 [main] INFO SECURITY_AUDIT -- timestamp=2026-07-03T04:18:46.964 eventType=LOGIN_FAILED userId=null username=null clientIp=192.168.1.1 success=false failureReason=BAD_CREDENTIALS
04:18:46.965 [main] INFO SECURITY_AUDIT -- timestamp=2026-07-03T04:18:46.965 eventType=LOGOUT userId=2 username=user clientIp=10.0.0.1 success=true
04:18:46.969 [main] INFO SECURITY_AUDIT -- timestamp=2026-07-03T04:18:46.969 eventType=LOGIN_SUCCESS userId=1 username=testuser clientIp=127.0.0.1 success=true newJti=jti-xxx
04:18:46.972 [main] INFO SECURITY_AUDIT -- timestamp=2026-07-03T04:18:46.972 eventType=LOGOUT userId=2 username=johndoe clientIp=10.0.0.1 success=true refreshTokenMasked=abc123***
04:18:46.974 [main] INFO SECURITY_AUDIT -- timestamp=2026-07-03T04:18:46.974 eventType=LOGIN_SUCCESS userId=1 username=testuser clientIp=127.0.0.1 success=true
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.222 s -- in com.aimedical.modules.commonmodule.auth.audit.LoggingSecurityAuditLoggerTest
[INFO] Running com.aimedical.modules.commonmodule.auth.blacklist.InMemoryTokenBlacklistTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.036 s -- in com.aimedical.modules.commonmodule.auth.blacklist.InMemoryTokenBlacklistTest
[INFO] Running com.aimedical.modules.commonmodule.auth.config.AuthModuleConfigTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in com.aimedical.modules.commonmodule.auth.config.AuthModuleConfigTest
[INFO] Running com.aimedical.modules.commonmodule.auth.converter.UserConverterTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.022 s -- in com.aimedical.modules.commonmodule.auth.converter.UserConverterTest
[INFO] Running com.aimedical.modules.commonmodule.auth.exception.AccountDisabledAuthenticationExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in com.aimedical.modules.commonmodule.auth.exception.AccountDisabledAuthenticationExceptionTest
[INFO] Running com.aimedical.modules.commonmodule.auth.exception.PasswordChangeRequiredExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.commonmodule.auth.exception.PasswordChangeRequiredExceptionTest
[INFO] Running com.aimedical.modules.commonmodule.auth.jwt.JwtTokenProviderTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.643 s -- in com.aimedical.modules.commonmodule.auth.jwt.JwtTokenProviderTest
[INFO] Running com.aimedical.modules.commonmodule.auth.login.LoginAttemptTrackerTest
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.250 s -- in com.aimedical.modules.commonmodule.auth.login.LoginAttemptTrackerTest
[INFO] Running com.aimedical.modules.commonmodule.auth.password.PasswordChangeServiceImplTest
WARNING: A Java agent has been loaded dynamically (C:\Users\laoE\.m2\repository\net\bytebuddy\byte-buddy-agent\1.14.13\byte-buddy-agent-1.14.13.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.507 s -- in com.aimedical.modules.commonmodule.auth.password.PasswordChangeServiceImplTest
[INFO] Running com.aimedical.modules.commonmodule.auth.password.PasswordPolicyImplTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.023 s -- in com.aimedical.modules.commonmodule.auth.password.PasswordPolicyImplTest
[INFO] Running com.aimedical.modules.commonmodule.auth.rateLimit.InMemoryRateLimitGuardTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.13 s -- in com.aimedical.modules.commonmodule.auth.rateLimit.InMemoryRateLimitGuardTest
[INFO] Running com.aimedical.modules.commonmodule.auth.rateLimit.SlidingWindowCounterTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.587 s -- in com.aimedical.modules.commonmodule.auth.rateLimit.SlidingWindowCounterTest
[INFO] Running com.aimedical.modules.commonmodule.auth.security.CurrentUserImplTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.154 s -- in com.aimedical.modules.commonmodule.auth.security.CurrentUserImplTest
[INFO] Running com.aimedical.modules.commonmodule.auth.security.GlobalRateLimitFilterTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.190 s -- in com.aimedical.modules.commonmodule.auth.security.GlobalRateLimitFilterTest
[INFO] Running com.aimedical.modules.commonmodule.auth.security.JwtAuthenticationFilterTest
04:19:01.669 [main] WARN com.aimedical.modules.commonmodule.auth.security.JwtAuthenticationFilter -- JWT token validation failed, uri=
04:19:01.924 [main] WARN com.aimedical.modules.commonmodule.auth.security.JwtAuthenticationFilter -- JWT token validation failed, uri=
04:19:01.928 [main] WARN com.aimedical.modules.commonmodule.auth.security.JwtAuthenticationFilter -- User not found or deleted, userId=1, uri=
04:19:01.930 [main] WARN com.aimedical.modules.commonmodule.auth.security.JwtAuthenticationFilter -- Token is blacklisted, jti=some-jti, uri=
04:19:01.934 [main] WARN com.aimedical.modules.commonmodule.auth.security.JwtAuthenticationFilter -- Account disabled, userId=1
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.347 s -- in com.aimedical.modules.commonmodule.auth.security.JwtAuthenticationFilterTest
[INFO] Running com.aimedical.modules.commonmodule.auth.security.PasswordChangeCheckFilterTest
04:19:01.948 [main] WARN com.aimedical.modules.commonmodule.auth.security.PasswordChangeCheckFilter -- Password change required for userId=1, blocking request: GET /api/auth/me
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in com.aimedical.modules.commonmodule.auth.security.PasswordChangeCheckFilterTest
[INFO] Running com.aimedical.modules.commonmodule.auth.security.RestAccessDeniedHandlerTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.093 s -- in com.aimedical.modules.commonmodule.auth.security.RestAccessDeniedHandlerTest
[INFO] Running com.aimedical.modules.commonmodule.auth.security.RestAuthenticationEntryPointTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.047 s -- in com.aimedical.modules.commonmodule.auth.security.RestAuthenticationEntryPointTest
[INFO] Running com.aimedical.modules.commonmodule.auth.security.SecurityConfigPhase1CoexistenceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.aimedical.modules.commonmodule.auth.security.SecurityConfigPhase1CoexistenceTest
[INFO] Running com.aimedical.modules.commonmodule.auth.security.SecurityConfigPhase1Test
04:19:02.470 [main] INFO org.springframework.security.web.DefaultSecurityFilterChain -- Will secure any request with [org.springframework.security.web.session.DisableEncodeUrlFilter@7fd7085d, org.springframework.security.web.header.HeaderWriterFilter@69cc3370, org.springframework.web.filter.CorsFilter@9120cb5, com.aimedical.modules.commonmodule.auth.security.GlobalRateLimitFilter@18419ab7, com.aimedical.modules.commonmodule.auth.security.JwtAuthenticationFilter@5f93ec02, com.aimedical.modules.commonmodule.auth.security.GlobalRateLimitFilter@18419ab7, com.aimedical.modules.commonmodule.auth.security.JwtAuthenticationFilter@5f93ec02, com.aimedical.modules.commonmodule.auth.security.PasswordChangeCheckFilter@57cc0333, com.aimedical.modules.commonmodule.auth.security.PasswordChangeCheckFilter@57cc0333, org.springframework.security.web.session.SessionManagementFilter@4e22dadb, org.springframework.security.web.access.ExceptionTranslationFilter@5aa2758a, org.springframework.security.web.access.intercept.AuthorizationFilter@1a916120]
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.365 s -- in com.aimedical.modules.commonmodule.auth.security.SecurityConfigPhase1Test
[INFO] Running com.aimedical.modules.commonmodule.auth.UserFacadeImplTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.039 s -- in com.aimedical.modules.commonmodule.auth.UserFacadeImplTest
[INFO] Running com.aimedical.modules.commonmodule.CommonModulePlaceholderTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in com.aimedical.modules.commonmodule.CommonModulePlaceholderTest
[INFO] Running com.aimedical.modules.commonmodule.controller.AuthControllerTest
[INFO] Running com.aimedical.modules.commonmodule.controller.AuthControllerTest$ChangePasswordTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.088 s -- in com.aimedical.modules.commonmodule.controller.AuthControllerTest$ChangePasswordTests
[INFO] Running com.aimedical.modules.commonmodule.controller.AuthControllerTest$UpdateMeTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.modules.commonmodule.controller.AuthControllerTest$UpdateMeTests
[INFO] Running com.aimedical.modules.commonmodule.controller.AuthControllerTest$MeTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.aimedical.modules.commonmodule.controller.AuthControllerTest$MeTests
[INFO] Running com.aimedical.modules.commonmodule.controller.AuthControllerTest$RefreshTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.aimedical.modules.commonmodule.controller.AuthControllerTest$RefreshTests
[INFO] Running com.aimedical.modules.commonmodule.controller.AuthControllerTest$LogoutTests
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in com.aimedical.modules.commonmodule.controller.AuthControllerTest$LogoutTests
[INFO] Running com.aimedical.modules.commonmodule.controller.AuthControllerTest$LoginTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.commonmodule.controller.AuthControllerTest$LoginTests
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.133 s -- in com.aimedical.modules.commonmodule.controller.AuthControllerTest
[INFO] Running com.aimedical.modules.commonmodule.controller.MenuControllerTest
[INFO] Running com.aimedical.modules.commonmodule.controller.MenuControllerTest$DeleteMenuTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.072 s -- in com.aimedical.modules.commonmodule.controller.MenuControllerTest$DeleteMenuTests
[INFO] Running com.aimedical.modules.commonmodule.controller.MenuControllerTest$PathIdConsistencyTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.commonmodule.controller.MenuControllerTest$PathIdConsistencyTests
[INFO] Running com.aimedical.modules.commonmodule.controller.MenuControllerTest$UpdateMenuTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in com.aimedical.modules.commonmodule.controller.MenuControllerTest$UpdateMenuTests
[INFO] Running com.aimedical.modules.commonmodule.controller.MenuControllerTest$CreateMenuTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.commonmodule.controller.MenuControllerTest$CreateMenuTests
[INFO] Running com.aimedical.modules.commonmodule.controller.MenuControllerTest$GetMenuTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.commonmodule.controller.MenuControllerTest$GetMenuTests
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.096 s -- in com.aimedical.modules.commonmodule.controller.MenuControllerTest
[INFO] Running com.aimedical.modules.commonmodule.dto.request.LoginRequestTest
04:19:02.931 [main] INFO org.hibernate.validator.internal.util.Version -- HV000001: Hibernate Validator 8.0.1.Final
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.650 s -- in com.aimedical.modules.commonmodule.dto.request.LoginRequestTest
[INFO] Running com.aimedical.modules.commonmodule.dto.request.MenuCreateRequestTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.017 s -- in com.aimedical.modules.commonmodule.dto.request.MenuCreateRequestTest
[INFO] Running com.aimedical.modules.commonmodule.dto.request.MenuUpdateRequestTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in com.aimedical.modules.commonmodule.dto.request.MenuUpdateRequestTest
[INFO] Running com.aimedical.modules.commonmodule.dto.request.PasswordChangeRequestTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.015 s -- in com.aimedical.modules.commonmodule.dto.request.PasswordChangeRequestTest
[INFO] Running com.aimedical.modules.commonmodule.dto.request.ProfileUpdateRequestTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.044 s -- in com.aimedical.modules.commonmodule.dto.request.ProfileUpdateRequestTest
[INFO] Running com.aimedical.modules.commonmodule.dto.request.RefreshTokenRequestTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.commonmodule.dto.request.RefreshTokenRequestTest
[INFO] Running com.aimedical.modules.commonmodule.dto.response.LoginResponseTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.commonmodule.dto.response.LoginResponseTest
[INFO] Running com.aimedical.modules.commonmodule.dto.response.MenuResponseTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.modules.commonmodule.dto.response.MenuResponseTest
[INFO] Running com.aimedical.modules.commonmodule.dto.response.TokenRefreshResponseTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.commonmodule.dto.response.TokenRefreshResponseTest
[INFO] Running com.aimedical.modules.commonmodule.dto.response.UserInfoResponseTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in com.aimedical.modules.commonmodule.dto.response.UserInfoResponseTest
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtConfigTest
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtConfigTest$ValidateTests
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.commonmodule.jwt.JwtConfigTest$ValidateTests
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtConfigTest$GetterSetterTests
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.commonmodule.jwt.JwtConfigTest$GetterSetterTests
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtConfigTest$DefaultValueTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in com.aimedical.modules.commonmodule.jwt.JwtConfigTest$DefaultValueTests
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s -- in com.aimedical.modules.commonmodule.jwt.JwtConfigTest
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtUtilTest
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtUtilTest$InitTests
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s -- in com.aimedical.modules.commonmodule.jwt.JwtUtilTest$InitTests
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtUtilTest$GetExpirationTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in com.aimedical.modules.commonmodule.jwt.JwtUtilTest$GetExpirationTests
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtUtilTest$ExtractTokenTests
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.commonmodule.jwt.JwtUtilTest$ExtractTokenTests
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtUtilTest$GetRoleTests
04:19:03.572 [main] WARN com.aimedical.modules.commonmodule.jwt.JwtUtil -- JWT���Ƹ�ʽ����: Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 1
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in com.aimedical.modules.commonmodule.jwt.JwtUtilTest$GetRoleTests
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtUtilTest$GetUserIdTests
04:19:03.578 [main] WARN com.aimedical.modules.commonmodule.jwt.JwtUtil -- JWT���Ƹ�ʽ����: Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 1
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.modules.commonmodule.jwt.JwtUtilTest$GetUserIdTests
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtUtilTest$ValidateTokenAndGetClaimsTests
04:19:03.581 [main] WARN com.aimedical.modules.commonmodule.jwt.JwtUtil -- JWT���Ƹ�ʽ����: Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 1
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.commonmodule.jwt.JwtUtilTest$ValidateTokenAndGetClaimsTests
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtUtilTest$ValidateTokenTests
04:19:03.588 [main] WARN com.aimedical.modules.commonmodule.jwt.JwtUtil -- JWT���Ƹ�ʽ����: Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 1
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.commonmodule.jwt.JwtUtilTest$ValidateTokenTests
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtUtilTest$ParseTokenTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s -- in com.aimedical.modules.commonmodule.jwt.JwtUtilTest$ParseTokenTests
[INFO] Running com.aimedical.modules.commonmodule.jwt.JwtUtilTest$GenerateTokenTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.commonmodule.jwt.JwtUtilTest$GenerateTokenTests
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.052 s -- in com.aimedical.modules.commonmodule.jwt.JwtUtilTest
[INFO] Running com.aimedical.modules.commonmodule.permission.PermissionFunctionTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.commonmodule.permission.PermissionFunctionTest
[INFO] Running com.aimedical.modules.commonmodule.permission.PostTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.modules.commonmodule.permission.PostTest
[INFO] Running com.aimedical.modules.commonmodule.permission.RoleTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in com.aimedical.modules.commonmodule.permission.RoleTest
[INFO] Running com.aimedical.modules.commonmodule.permission.UserRepositoryTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

2026-07-03T04:19:04.480+08:00  INFO 18848 --- [           main] c.a.m.c.permission.UserRepositoryTest    : Starting UserRepositoryTest using Java 21.0.11 with PID 18848 (started by laoE in C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\common-module\common-module-impl)
2026-07-03T04:19:04.482+08:00  INFO 18848 --- [           main] c.a.m.c.permission.UserRepositoryTest    : No active profile set, falling back to 1 default profile: "default"
2026-07-03T04:19:04.938+08:00  INFO 18848 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-03T04:19:05.003+08:00  INFO 18848 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 55 ms. Found 4 JPA repository interfaces.
2026-07-03T04:19:05.068+08:00  INFO 18848 --- [           main] beddedDataSourceBeanFactoryPostProcessor : Replacing 'dataSource' DataSource bean with embedded version
2026-07-03T04:19:05.274+08:00  INFO 18848 --- [           main] o.s.j.d.e.EmbeddedDatabaseFactory        : Starting embedded database: url='jdbc:h2:mem:d85c2474-2a0a-4944-961d-e1ccdcfb937b;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', username='sa'
2026-07-03T04:19:05.731+08:00  INFO 18848 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-03T04:19:05.825+08:00  INFO 18848 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.4.4.Final
2026-07-03T04:19:05.864+08:00  INFO 18848 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-03T04:19:05.998+08:00  INFO 18848 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-03T04:19:07.202+08:00  INFO 18848 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: drop table if exists post_function cascade 
Hibernate: drop table if exists sys_function cascade 
Hibernate: drop table if exists sys_post cascade 
Hibernate: drop table if exists sys_role cascade 
Hibernate: drop table if exists sys_user cascade 
Hibernate: drop table if exists user_post cascade 
Hibernate: drop table if exists user_role cascade 
Hibernate: create table post_function (function_id bigint not null, post_id bigint not null, primary key (function_id, post_id))
Hibernate: create table sys_function (deleted boolean not null, enabled boolean not null, sort_order integer not null, visible boolean not null, created_at timestamp(6), id bigint generated by default as identity, parent_id bigint, updated_at timestamp(6), version bigint, type varchar(20), code varchar(255) not null unique, component varchar(255), description varchar(255), icon varchar(255), name varchar(255) not null, path varchar(255), primary key (id))
Hibernate: create table sys_post (deleted boolean not null, enabled boolean not null, sort integer, created_at timestamp(6), id bigint generated by default as identity, role_id bigint, updated_at timestamp(6), version bigint, remark varchar(500), code varchar(255) not null unique, description varchar(255), name varchar(255), primary key (id))
Hibernate: create table sys_role (deleted boolean not null, enabled boolean not null, sort integer not null, created_at timestamp(6), id bigint generated by default as identity, updated_at timestamp(6), version bigint, remark varchar(500), code varchar(255) not null unique, description varchar(255), name varchar(255), primary key (id))
Hibernate: create table sys_user (age integer, deleted boolean not null, enabled boolean not null, password_change_required boolean not null, token_version integer not null, created_at timestamp(6), id bigint generated by default as identity, updated_at timestamp(6), version bigint, gender varchar(10), user_type varchar(20) not null check (user_type in ('DOCTOR','PATIENT','ADMIN')), remark varchar(500), email varchar(255), nickname varchar(255) not null, password varchar(255) not null, phone varchar(255), username varchar(255) not null unique, primary key (id))
Hibernate: create table user_post (post_id bigint not null, user_id bigint not null, primary key (post_id, user_id))
Hibernate: create table user_role (role_id bigint not null, user_id bigint not null, primary key (role_id, user_id))
Hibernate: alter table if exists post_function add constraint FKh56snoidh814t7tmnsvgkyp6c foreign key (function_id) references sys_function
Hibernate: alter table if exists post_function add constraint FKbv50wilq40pjojsdm6sg6g2xg foreign key (post_id) references sys_post
Hibernate: alter table if exists sys_function add constraint FKmp2cmbi9l9c1c7618t2o0v2xb foreign key (parent_id) references sys_function
Hibernate: alter table if exists sys_post add constraint FKjfpb3no7elnlin0vwqbx940gu foreign key (role_id) references sys_role
Hibernate: alter table if exists user_post add constraint FK1qq5m5bsjagqw0s8m1cyb1rmj foreign key (post_id) references sys_post
Hibernate: alter table if exists user_post add constraint FKafwurpfqy3g4a4k0xnse3l8vy foreign key (user_id) references sys_user
Hibernate: alter table if exists user_role add constraint FKdec2ggmqwgdhhb59jw7o488wx foreign key (role_id) references sys_role
Hibernate: alter table if exists user_role add constraint FKsrs64lo4ci4xyu3da9clbiv8r foreign key (user_id) references sys_user
2026-07-03T04:19:07.311+08:00  INFO 18848 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-03T04:19:07.762+08:00  INFO 18848 --- [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-03T04:19:08.237+08:00  INFO 18848 --- [           main] c.a.m.c.permission.UserRepositoryTest    : Started UserRepositoryTest in 4.329 seconds (process running for 22.563)
Hibernate: select u1_0.id,u1_0.age,u1_0.created_at,u1_0.deleted,u1_0.email,u1_0.enabled,u1_0.gender,u1_0.nickname,u1_0.password,u1_0.password_change_required,u1_0.phone,u1_0.remark,u1_0.token_version,u1_0.updated_at,u1_0.user_type,u1_0.username,u1_0.version from sys_user u1_0 where u1_0.username=?
Hibernate: insert into sys_user (age,created_at,deleted,email,enabled,gender,nickname,password,password_change_required,phone,remark,token_version,updated_at,user_type,username,version,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: insert into sys_user (age,created_at,deleted,email,enabled,gender,nickname,password,password_change_required,phone,remark,token_version,updated_at,user_type,username,version,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,default)
2026-07-03T04:19:08.507+08:00  WARN 18848 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 23502, SQLState: 23502
2026-07-03T04:19:08.507+08:00 ERROR 18848 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : NULL not allowed for column "PASSWORD"; SQL statement:
insert into sys_user (age,created_at,deleted,email,enabled,gender,nickname,password,password_change_required,phone,remark,token_version,updated_at,user_type,username,version,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,default) [23502-224]
Hibernate: insert into sys_user (age,created_at,deleted,email,enabled,gender,nickname,password,password_change_required,phone,remark,token_version,updated_at,user_type,username,version,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: select u1_0.id,u1_0.age,u1_0.created_at,u1_0.deleted,u1_0.email,u1_0.enabled,u1_0.gender,u1_0.nickname,u1_0.password,u1_0.password_change_required,u1_0.phone,u1_0.remark,u1_0.token_version,u1_0.updated_at,u1_0.user_type,u1_0.username,u1_0.version from sys_user u1_0 where u1_0.username=?
Hibernate: select u1_0.id,u1_0.age,u1_0.created_at,u1_0.deleted,u1_0.email,u1_0.enabled,u1_0.gender,u1_0.nickname,u1_0.password,u1_0.password_change_required,u1_0.phone,p1_0.user_id,p1_1.id,p1_1.code,p1_1.created_at,p1_1.deleted,p1_1.description,p1_1.enabled,f1_0.post_id,f1_1.id,f1_1.code,f1_1.component,f1_1.created_at,f1_1.deleted,f1_1.description,f1_1.enabled,f1_1.icon,f1_1.name,f1_1.parent_id,f1_1.path,f1_1.sort_order,f1_1.type,f1_1.updated_at,f1_1.version,f1_1.visible,p1_1.name,p1_1.remark,p1_1.role_id,p1_1.sort,p1_1.updated_at,p1_1.version,u1_0.remark,r2_0.user_id,r2_1.id,r2_1.code,r2_1.created_at,r2_1.deleted,r2_1.description,r2_1.enabled,r2_1.name,r2_1.remark,r2_1.sort,r2_1.updated_at,r2_1.version,u1_0.token_version,u1_0.updated_at,u1_0.user_type,u1_0.username,u1_0.version from sys_user u1_0 left join user_post p1_0 on u1_0.id=p1_0.user_id left join sys_post p1_1 on p1_1.id=p1_0.post_id left join post_function f1_0 on p1_1.id=f1_0.post_id left join sys_function f1_1 on f1_1.id=f1_0.function_id left join user_role r2_0 on u1_0.id=r2_0.user_id left join sys_role r2_1 on r2_1.id=r2_0.role_id where u1_0.id=?
Hibernate: insert into sys_user (age,created_at,deleted,email,enabled,gender,nickname,password,password_change_required,phone,remark,token_version,updated_at,user_type,username,version,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: select u1_0.id,u1_0.age,u1_0.created_at,u1_0.deleted,u1_0.email,u1_0.enabled,u1_0.gender,u1_0.nickname,u1_0.password,u1_0.password_change_required,u1_0.phone,p1_0.user_id,p1_1.id,p1_1.code,p1_1.created_at,p1_1.deleted,p1_1.description,p1_1.enabled,f1_0.post_id,f1_1.id,f1_1.code,f1_1.component,f1_1.created_at,f1_1.deleted,f1_1.description,f1_1.enabled,f1_1.icon,f1_1.name,f1_1.parent_id,f1_1.path,f1_1.sort_order,f1_1.type,f1_1.updated_at,f1_1.version,f1_1.visible,p1_1.name,p1_1.remark,p1_1.role_id,p1_1.sort,p1_1.updated_at,p1_1.version,u1_0.remark,r2_0.user_id,r2_1.id,r2_1.code,r2_1.created_at,r2_1.deleted,r2_1.description,r2_1.enabled,r2_1.name,r2_1.remark,r2_1.sort,r2_1.updated_at,r2_1.version,u1_0.token_version,u1_0.updated_at,u1_0.user_type,u1_0.username,u1_0.version from sys_user u1_0 left join user_post p1_0 on u1_0.id=p1_0.user_id left join sys_post p1_1 on p1_1.id=p1_0.post_id left join post_function f1_0 on p1_1.id=f1_0.post_id left join sys_function f1_1 on f1_1.id=f1_0.function_id left join user_role r2_0 on u1_0.id=r2_0.user_id left join sys_role r2_1 on r2_1.id=r2_0.role_id where u1_0.id=?
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.984 s -- in com.aimedical.modules.commonmodule.permission.UserRepositoryTest
[INFO] Running com.aimedical.modules.commonmodule.permission.UserTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.commonmodule.permission.UserTest
[INFO] Running com.aimedical.modules.commonmodule.service.AuthServiceTest
2026-07-03T04:19:09.023+08:00  INFO 18848 --- [           main] c.a.m.c.service.impl.AuthServiceImpl     : �û��ǳ��ɹ�
2026-07-03T04:19:09.037+08:00  INFO 18848 --- [           main] c.a.m.c.service.impl.AuthServiceImpl     : �û��ǳ��ɹ�
2026-07-03T04:19:09.046+08:00  INFO 18848 --- [           main] c.a.m.c.service.impl.AuthServiceImpl     : �û��ǳ��ɹ�
2026-07-03T04:19:09.049+08:00  INFO 18848 --- [           main] c.a.m.c.service.impl.AuthServiceImpl     : �û��ǳ��ɹ�
2026-07-03T04:19:09.051+08:00  INFO 18848 --- [           main] c.a.m.c.service.impl.AuthServiceImpl     : �û������޸ĳɹ���userId: 1
2026-07-03T04:19:09.055+08:00  INFO 18848 --- [           main] c.a.m.c.service.impl.AuthServiceImpl     : �û��ǳ��ɹ�
[INFO] Tests run: 36, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.453 s -- in com.aimedical.modules.commonmodule.service.AuthServiceTest
[INFO] Running com.aimedical.modules.commonmodule.service.MenuServiceTest
[INFO] Running com.aimedical.modules.commonmodule.service.MenuServiceTest$GetMenuByIdTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.065 s -- in com.aimedical.modules.commonmodule.service.MenuServiceTest$GetMenuByIdTests
[INFO] Running com.aimedical.modules.commonmodule.service.MenuServiceTest$DeleteMenuTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.commonmodule.service.MenuServiceTest$DeleteMenuTests
[INFO] Running com.aimedical.modules.commonmodule.service.MenuServiceTest$UpdateMenuTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.commonmodule.service.MenuServiceTest$UpdateMenuTests
[INFO] Running com.aimedical.modules.commonmodule.service.MenuServiceTest$CreateMenuTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.commonmodule.service.MenuServiceTest$CreateMenuTests
[INFO] Running com.aimedical.modules.commonmodule.service.MenuServiceTest$GetAllMenusTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in com.aimedical.modules.commonmodule.service.MenuServiceTest$GetAllMenusTests
[INFO] Running com.aimedical.modules.commonmodule.service.MenuServiceTest$GetUserMenuTreeTests
[WARNING] Tests run: 8, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 0.013 s -- in com.aimedical.modules.commonmodule.service.MenuServiceTest$GetUserMenuTreeTests
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.117 s -- in com.aimedical.modules.commonmodule.service.MenuServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[WARNING] Tests run: 399, Failures: 0, Errors: 0, Skipped: 1
[INFO] 
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ common-module-impl ---
[INFO] Loading execution data file C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\common-module\common-module-impl\target\jacoco.exec
[INFO] Analyzed bundle 'common-module-impl' with 45 classes
[INFO] 
[INFO] --------------------------< com.aimedical:ai >--------------------------
[INFO] Building AI Module Aggregator 0.0.1-SNAPSHOT                      [6/18]
[INFO]   from modules\ai\pom.xml
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ ai ---
[INFO] Skipping JaCoCo execution because property jacoco.skip is set.
[INFO] argLine set to empty
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ ai ---
[INFO] Skipping JaCoCo execution because property jacoco.skip is set.
[INFO] 
[INFO] ------------------------< com.aimedical:ai-api >------------------------
[INFO] Building ai-api 0.0.1-SNAPSHOT                                    [7/18]
[INFO]   from modules\ai\ai-api\pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ ai-api ---
[INFO] argLine set to -javaagent:C:\\Users\\laoE\\.m2\\repository\\org\\jacoco\\org.jacoco.agent\\0.8.12\\org.jacoco.agent-0.8.12-runtime.jar=destfile=C:\\Develop\\Software\\AIMedicalSys\\AIMedical\\backend\\modules\\ai\\ai-api\\target\\jacoco.exec
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ ai-api ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-api\src\main\resources
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-api\src\main\resources
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ ai-api ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ ai-api ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-api\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ ai-api ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- surefire:3.1.2:test (default-test) @ ai-api ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.aimedical.modules.ai.api.AiResultFactoryTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.092 s -- in com.aimedical.modules.ai.api.AiResultFactoryTest
[INFO] Running com.aimedical.modules.ai.api.AiResultTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in com.aimedical.modules.ai.api.AiResultTest
[INFO] Running com.aimedical.modules.ai.api.AiServiceTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.044 s -- in com.aimedical.modules.ai.api.AiServiceTest
[INFO] Running com.aimedical.modules.ai.api.degradation.DegradationContextTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.017 s -- in com.aimedical.modules.ai.api.degradation.DegradationContextTest
[INFO] Running com.aimedical.modules.ai.api.degradation.DegradationReasonTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in com.aimedical.modules.ai.api.degradation.DegradationReasonTest
[INFO] Running com.aimedical.modules.ai.api.degradation.DegradationStrategyTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s -- in com.aimedical.modules.ai.api.degradation.DegradationStrategyTest
[INFO] Running com.aimedical.modules.ai.api.dto.base.CallContextTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.470 s -- in com.aimedical.modules.ai.api.dto.base.CallContextTest
[INFO] Running com.aimedical.modules.ai.api.dto.base.Phase4BusinessExceptionTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.aimedical.modules.ai.api.dto.base.Phase4BusinessExceptionTest
[INFO] Running com.aimedical.modules.ai.api.dto.base.Phase4ServiceMetaCapableTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.api.dto.base.Phase4ServiceMetaCapableTest
[INFO] Running com.aimedical.modules.ai.api.dto.base.Phase4ServiceMetaTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.013 s -- in com.aimedical.modules.ai.api.dto.base.Phase4ServiceMetaTest
[INFO] Running com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequestTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequestTest
[INFO] Running com.aimedical.modules.ai.api.dto.discussion.DiscussionTranscriptTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.api.dto.discussion.DiscussionTranscriptTest
[INFO] Running com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordDtoTest
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s -- in com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordDtoTest
[INFO] Running com.aimedical.modules.ai.api.dto.prescription.PrescriptionDtoTest
[INFO] Tests run: 33, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.022 s -- in com.aimedical.modules.ai.api.dto.prescription.PrescriptionDtoTest
[INFO] Running com.aimedical.modules.ai.api.dto.triage.TriageDtoTest
[INFO] Tests run: 38, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.020 s -- in com.aimedical.modules.ai.api.dto.triage.TriageDtoTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 200, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ ai-api ---
[INFO] Loading execution data file C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-api\target\jacoco.exec
[INFO] Analyzed bundle 'ai-api' with 51 classes
[INFO] 
[INFO] -----------------------< com.aimedical:ai-impl >------------------------
[INFO] Building ai-impl 0.0.1-SNAPSHOT                                   [8/18]
[INFO]   from modules\ai\ai-impl\pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ ai-impl ---
[INFO] argLine set to -javaagent:C:\\Users\\laoE\\.m2\\repository\\org\\jacoco\\org.jacoco.agent\\0.8.12\\org.jacoco.agent-0.8.12-runtime.jar=destfile=C:\\Develop\\Software\\AIMedicalSys\\AIMedical\\backend\\modules\\ai\\ai-impl\\target\\jacoco.exec
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ ai-impl ---
[INFO] Copying 0 resource from src\main\resources to target\classes
[INFO] Copying 1 resource from src\main\resources to target\classes
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ ai-impl ---
[INFO] Changes detected - recompiling the module! :source
[INFO] Compiling 89 source files with javac [debug release 17] to target\classes
[INFO] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutor.java: C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\src\main\java\com\aimedical\modules\ai\impl\orchestrator\AbstractCapabilityExecutor.javaʹ����δ�����򲻰�ȫ�Ĳ�����
[INFO] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutor.java: �й���ϸ��Ϣ, ��ʹ�� -Xlint:unchecked ���±��롣
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ ai-impl ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ ai-impl ---
[INFO] Changes detected - recompiling the module! :dependency
[INFO] Compiling 65 source files with javac [debug release 17] to target\test-classes
[INFO] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutorTest.java: ĳЩ�����ļ�ʹ����δ�����򲻰�ȫ�Ĳ�����
[INFO] /C:/Develop/Software/AIMedicalSys/AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutorTest.java: �й���ϸ��Ϣ, ��ʹ�� -Xlint:unchecked ���±��롣
[INFO] 
[INFO] --- surefire:3.1.2:test (default-test) @ ai-impl ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.aimedical.modules.ai.impl.client.AuthTypeTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.063 s -- in com.aimedical.modules.ai.impl.client.AuthTypeTest
[INFO] Running com.aimedical.modules.ai.impl.client.ChatToolDefinitionTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.325 s -- in com.aimedical.modules.ai.impl.client.ChatToolDefinitionTest
[INFO] Running com.aimedical.modules.ai.impl.client.ClientTypeTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.ai.impl.client.ClientTypeTest
[INFO] Running com.aimedical.modules.ai.impl.client.CredentialProviderTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.modules.ai.impl.client.CredentialProviderTest
[INFO] Running com.aimedical.modules.ai.impl.client.DefaultCredentialProviderTest
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.495 s -- in com.aimedical.modules.ai.impl.client.DefaultCredentialProviderTest
[INFO] Running com.aimedical.modules.ai.impl.client.DelegatingLlmChatServiceTest
04:19:21.256 [main] WARN com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- ClientType=SPRING_AI û�ж�Ӧ�� LlmChatService ʵ�֣�����������ע��
04:19:21.266 [main] WARN com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=SPRING_AI ��ʵ�֣�endpointId=null�������˵� HTTP_API�������齡�����˵�״̬
04:19:21.268 [main] WARN com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=SPRING_AI ��ʵ�֣�endpointId=test-ep-id�������˵� HTTP_API�������齡�����˵�״̬
04:19:21.268 [main] WARN com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=null ��ʵ�֣�endpointId=null�������˵� HTTP_API�������齡�����˵�״̬
04:19:21.269 [main] WARN com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=SPRING_AI ��ʵ�֣�endpointId=test-ep-id�������˵� HTTP_API�������齡�����˵�״̬
04:19:21.270 [main] WARN com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=SPRING_AI ��ʵ�֣�endpointId=null�������˵� HTTP_API�������齡�����˵�״̬
04:19:21.271 [main] WARN com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=null ��ʵ�֣�endpointId=null�������˵� HTTP_API�������齡�����˵�״̬
04:19:21.275 [main] WARN com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=null ��ʵ�֣�endpointId=null�������˵� HTTP_API�������齡�����˵�״̬
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.029 s -- in com.aimedical.modules.ai.impl.client.DelegatingLlmChatServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.EndpointRateLimiterTest
WARNING: A Java agent has been loaded dynamically (C:\Users\laoE\.m2\repository\net\bytebuddy\byte-buddy-agent\1.14.13\byte-buddy-agent-1.14.13.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.253 s -- in com.aimedical.modules.ai.impl.client.EndpointRateLimiterTest
[INFO] Running com.aimedical.modules.ai.impl.client.exception.AiAbilityInputInvalidExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.ai.impl.client.exception.AiAbilityInputInvalidExceptionTest
[INFO] Running com.aimedical.modules.ai.impl.client.exception.CredentialUnavailableExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.modules.ai.impl.client.exception.CredentialUnavailableExceptionTest
[INFO] Running com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureExceptionTest
[INFO] Running com.aimedical.modules.ai.impl.client.exception.StructuredOutputNotSupportedExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.ai.impl.client.exception.StructuredOutputNotSupportedExceptionTest
[INFO] Running com.aimedical.modules.ai.impl.client.HttpApiLlmChatServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.786 s -- in com.aimedical.modules.ai.impl.client.HttpApiLlmChatServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.HttpApiLlmChatStreamServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.185 s -- in com.aimedical.modules.ai.impl.client.HttpApiLlmChatStreamServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatMessageRoleTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s -- in com.aimedical.modules.ai.impl.client.LlmChatMessageRoleTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatMessageTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.047 s -- in com.aimedical.modules.ai.impl.client.LlmChatMessageTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatOptionsTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.033 s -- in com.aimedical.modules.ai.impl.client.LlmChatOptionsTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatRequestTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.035 s -- in com.aimedical.modules.ai.impl.client.LlmChatRequestTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatResponseTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s -- in com.aimedical.modules.ai.impl.client.LlmChatResponseTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.ai.impl.client.LlmChatServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatStreamServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.ai.impl.client.LlmChatStreamServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.SpringAiLlmChatServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.impl.client.SpringAiLlmChatServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.SpringAiLlmChatStreamServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.impl.client.SpringAiLlmChatStreamServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.StructuredChatResultTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in com.aimedical.modules.ai.impl.client.StructuredChatResultTest
[INFO] Running com.aimedical.modules.ai.impl.config.AiPlatformConfigTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.418 s -- in com.aimedical.modules.ai.impl.config.AiPlatformConfigTest
[INFO] Running com.aimedical.modules.ai.impl.config.AiPlatformEnvironmentPostProcessorTest
04:19:24.188 [main] WARN com.aimedical.modules.ai.impl.config.AiPlatformEnvironmentPostProcessor -- ai.platform.enabled=false �� ai.mock.enabled δ���ã��Զ����� ai.mock.enabled=true
04:19:24.190 [main] WARN com.aimedical.modules.ai.impl.config.AiPlatformEnvironmentPostProcessor -- ai.platform.enabled=true �� ai.mock.enabled δ���ã��Զ����� ai.mock.enabled=false
04:19:24.192 [main] WARN com.aimedical.modules.ai.impl.config.AiPlatformEnvironmentPostProcessor -- ai.platform.enabled=true �� ai.mock.enabled δ���ã��Զ����� ai.mock.enabled=false
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.044 s -- in com.aimedical.modules.ai.impl.config.AiPlatformEnvironmentPostProcessorTest
[INFO] Running com.aimedical.modules.ai.impl.config.Phase4ServiceFacadeConfigTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.050 s -- in com.aimedical.modules.ai.impl.config.Phase4ServiceFacadeConfigTest
[INFO] Running com.aimedical.modules.ai.impl.degradation.CircuitBreakerDegradationStrategyTest
[INFO] Tests run: 24, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.972 s -- in com.aimedical.modules.ai.impl.degradation.CircuitBreakerDegradationStrategyTest
[INFO] Running com.aimedical.modules.ai.impl.degradation.NoOpDegradationStrategyTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.modules.ai.impl.degradation.NoOpDegradationStrategyTest
[INFO] Running com.aimedical.modules.ai.impl.degradation.TimeoutDegradationStrategyTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.impl.degradation.TimeoutDegradationStrategyTest
[INFO] Running com.aimedical.modules.ai.impl.experiment.ExperimentAssignmentTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in com.aimedical.modules.ai.impl.experiment.ExperimentAssignmentTest
[INFO] Running com.aimedical.modules.ai.impl.experiment.HashBucketExperimentManagerTest
04:19:25.386 [main] INFO com.aimedical.modules.ai.impl.experiment.HashBucketExperimentManager -- warmup completed: 1 capability groups cached
04:19:25.391 [main] WARN com.aimedical.modules.ai.impl.experiment.HashBucketExperimentManager -- warmup failed: java.lang.RuntimeException: DB error
04:19:25.420 [main] WARN com.aimedical.modules.ai.impl.experiment.HashBucketExperimentManager -- assign failed for capabilityId=cap1: java.lang.RuntimeException: DB connection failed
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.187 s -- in com.aimedical.modules.ai.impl.experiment.HashBucketExperimentManagerTest
[INFO] Running com.aimedical.modules.ai.impl.fallback.FallbackAiServiceTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.066 s -- in com.aimedical.modules.ai.impl.fallback.FallbackAiServiceTest
[INFO] Running com.aimedical.modules.ai.impl.fallback.PrescriptionLocalRuleFallbackTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 s -- in com.aimedical.modules.ai.impl.fallback.PrescriptionLocalRuleFallbackTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.AiCallLogEntityTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in com.aimedical.modules.ai.impl.metrics.AiCallLogEntityTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.AiCallLogRepositoryTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

2026-07-03T04:19:26.290+08:00  INFO 13416 --- [           main] c.a.m.a.i.m.AiCallLogRepositoryTest      : Starting AiCallLogRepositoryTest using Java 21.0.11 with PID 13416 (started by laoE in C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl)
2026-07-03T04:19:26.293+08:00  INFO 13416 --- [           main] c.a.m.a.i.m.AiCallLogRepositoryTest      : No active profile set, falling back to 1 default profile: "default"
2026-07-03T04:19:26.749+08:00  INFO 13416 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-03T04:19:26.873+08:00  INFO 13416 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 112 ms. Found 2 JPA repository interfaces.
2026-07-03T04:19:26.943+08:00  INFO 13416 --- [           main] beddedDataSourceBeanFactoryPostProcessor : Replacing 'dataSource' DataSource bean with embedded version
2026-07-03T04:19:27.122+08:00  INFO 13416 --- [           main] o.s.j.d.e.EmbeddedDatabaseFactory        : Starting embedded database: url='jdbc:h2:mem:f7066f08-371c-41da-a73d-d538ae22bb65;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', username='sa'
2026-07-03T04:19:27.578+08:00  INFO 13416 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-03T04:19:27.663+08:00  INFO 13416 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.4.4.Final
2026-07-03T04:19:27.706+08:00  INFO 13416 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-03T04:19:27.881+08:00  INFO 13416 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-03T04:19:28.833+08:00  INFO 13416 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: drop table if exists ai_call_log cascade 
Hibernate: drop table if exists ai_call_log_stats cascade 
Hibernate: create table ai_call_log (completion_tokens integer, degraded boolean, prompt_tokens integer, prompt_version integer, retry_count integer, total_tokens integer, call_time DATETIME(3) not null, elapsed_ms bigint, id bigint generated by default as identity, caller_role varchar(20), caller_id varchar(50), capability_id varchar(50), capability_name varchar(50), department_id varchar(50), error_code varchar(50), model_id varchar(50), patient_id varchar(50), session_id varchar(50), user_id varchar(50), visit_id varchar(50), error_message varchar(500), degradation_reason varchar(255), input_summary TEXT, output_summary TEXT, primary key (id))
Hibernate: create table ai_call_log_stats (avg_elapsed_ms float(53), p50elapsed_ms float(53), p95elapsed_ms float(53), p99elapsed_ms float(53), stat_month varchar(7), degraded_count bigint, failure_count bigint, id bigint generated by default as identity, success_count bigint, total_calls bigint, capability_id varchar(50), primary key (id))
Hibernate: create index idx_call_time on ai_call_log (call_time)
Hibernate: create index idx_capability_call_time on ai_call_log (capability_id, call_time desc)
Hibernate: create index idx_degraded_call_time on ai_call_log (degraded, call_time desc)
Hibernate: create index idx_stats_capability_month on ai_call_log_stats (capability_id, stat_month desc)
2026-07-03T04:19:28.880+08:00  INFO 13416 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-03T04:19:29.297+08:00  INFO 13416 --- [           main] c.a.m.a.i.m.AiCallLogRepositoryTest      : Started AiCallLogRepositoryTest in 3.41 seconds (process running for 11.066)
Hibernate: select count(acle1_0.id) from ai_call_log acle1_0 where acle1_0.call_time<?
Hibernate: insert into ai_call_log (call_time,caller_id,caller_role,capability_id,capability_name,completion_tokens,degradation_reason,degraded,department_id,elapsed_ms,error_code,error_message,input_summary,model_id,output_summary,patient_id,prompt_tokens,prompt_version,retry_count,session_id,total_tokens,user_id,visit_id,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,default)
Hibernate: select count(acle1_0.id) from ai_call_log acle1_0 where acle1_0.call_time<?
Hibernate: select count(acle1_0.id) from ai_call_log acle1_0 where acle1_0.call_time<?
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.025 s -- in com.aimedical.modules.ai.impl.metrics.AiCallLogRepositoryTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.AiCallLogStatsRepositoryTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

2026-07-03T04:19:29.606+08:00  INFO 13416 --- [           main] c.a.m.a.i.m.AiCallLogStatsRepositoryTest : Starting AiCallLogStatsRepositoryTest using Java 21.0.11 with PID 13416 (started by laoE in C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl)
2026-07-03T04:19:29.606+08:00  INFO 13416 --- [           main] c.a.m.a.i.m.AiCallLogStatsRepositoryTest : No active profile set, falling back to 1 default profile: "default"
2026-07-03T04:19:29.674+08:00  INFO 13416 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-03T04:19:29.681+08:00  INFO 13416 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 6 ms. Found 2 JPA repository interfaces.
2026-07-03T04:19:29.689+08:00  INFO 13416 --- [           main] beddedDataSourceBeanFactoryPostProcessor : Replacing 'dataSource' DataSource bean with embedded version
2026-07-03T04:19:29.708+08:00  INFO 13416 --- [           main] o.s.j.d.e.EmbeddedDatabaseFactory        : Starting embedded database: url='jdbc:h2:mem:6f5e4ee8-c385-4db6-a03b-6daeb6c65754;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', username='sa'
2026-07-03T04:19:29.732+08:00  INFO 13416 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-03T04:19:29.734+08:00  INFO 13416 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-03T04:19:29.738+08:00  INFO 13416 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-03T04:19:29.803+08:00  INFO 13416 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: drop table if exists ai_call_log cascade 
Hibernate: drop table if exists ai_call_log_stats cascade 
Hibernate: create table ai_call_log (completion_tokens integer, degraded boolean, prompt_tokens integer, prompt_version integer, retry_count integer, total_tokens integer, call_time DATETIME(3) not null, elapsed_ms bigint, id bigint generated by default as identity, caller_role varchar(20), caller_id varchar(50), capability_id varchar(50), capability_name varchar(50), department_id varchar(50), error_code varchar(50), model_id varchar(50), patient_id varchar(50), session_id varchar(50), user_id varchar(50), visit_id varchar(50), error_message varchar(500), degradation_reason varchar(255), input_summary TEXT, output_summary TEXT, primary key (id))
Hibernate: create table ai_call_log_stats (avg_elapsed_ms float(53), p50elapsed_ms float(53), p95elapsed_ms float(53), p99elapsed_ms float(53), stat_month varchar(7), degraded_count bigint, failure_count bigint, id bigint generated by default as identity, success_count bigint, total_calls bigint, capability_id varchar(50), primary key (id))
Hibernate: create index idx_call_time on ai_call_log (call_time)
Hibernate: create index idx_capability_call_time on ai_call_log (capability_id, call_time desc)
Hibernate: create index idx_degraded_call_time on ai_call_log (degraded, call_time desc)
Hibernate: create index idx_stats_capability_month on ai_call_log_stats (capability_id, stat_month desc)
2026-07-03T04:19:29.808+08:00  INFO 13416 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-03T04:19:29.853+08:00  INFO 13416 --- [           main] c.a.m.a.i.m.AiCallLogStatsRepositoryTest : Started AiCallLogStatsRepositoryTest in 0.266 seconds (process running for 11.622)
Hibernate: insert into ai_call_log_stats (avg_elapsed_ms,capability_id,degraded_count,failure_count,p50elapsed_ms,p95elapsed_ms,p99elapsed_ms,stat_month,success_count,total_calls,id) values (?,?,?,?,?,?,?,?,?,?,default)
Hibernate: select acls1_0.id,acls1_0.avg_elapsed_ms,acls1_0.capability_id,acls1_0.degraded_count,acls1_0.failure_count,acls1_0.p50elapsed_ms,acls1_0.p95elapsed_ms,acls1_0.p99elapsed_ms,acls1_0.stat_month,acls1_0.success_count,acls1_0.total_calls from ai_call_log_stats acls1_0 where acls1_0.capability_id=? and acls1_0.stat_month between ? and ?
Hibernate: select acls1_0.id,acls1_0.avg_elapsed_ms,acls1_0.capability_id,acls1_0.degraded_count,acls1_0.failure_count,acls1_0.p50elapsed_ms,acls1_0.p95elapsed_ms,acls1_0.p99elapsed_ms,acls1_0.stat_month,acls1_0.success_count,acls1_0.total_calls from ai_call_log_stats acls1_0 where acls1_0.capability_id=? and acls1_0.stat_month between ? and ?
Hibernate: insert into ai_call_log_stats (avg_elapsed_ms,capability_id,degraded_count,failure_count,p50elapsed_ms,p95elapsed_ms,p99elapsed_ms,stat_month,success_count,total_calls,id) values (?,?,?,?,?,?,?,?,?,?,default)
Hibernate: insert into ai_call_log_stats (avg_elapsed_ms,capability_id,degraded_count,failure_count,p50elapsed_ms,p95elapsed_ms,p99elapsed_ms,stat_month,success_count,total_calls,id) values (?,?,?,?,?,?,?,?,?,?,default)
Hibernate: select acls1_0.id,acls1_0.avg_elapsed_ms,acls1_0.capability_id,acls1_0.degraded_count,acls1_0.failure_count,acls1_0.p50elapsed_ms,acls1_0.p95elapsed_ms,acls1_0.p99elapsed_ms,acls1_0.stat_month,acls1_0.success_count,acls1_0.total_calls from ai_call_log_stats acls1_0 where acls1_0.capability_id=? and acls1_0.stat_month between ? and ?
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.345 s -- in com.aimedical.modules.ai.impl.metrics.AiCallLogStatsRepositoryTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.AiCallLogStatsTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 s -- in com.aimedical.modules.ai.impl.metrics.AiCallLogStatsTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.AiCallRecordTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.impl.metrics.AiCallRecordTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.EndpointHealthStateTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.ai.impl.metrics.EndpointHealthStateTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.LoggingMetricsCollectorTest
2026-07-03T04:19:30.017+08:00  WARN 13416 --- [           main] c.a.m.a.i.m.LoggingMetricsCollector      : Failed to persist AiCallLogEntity

java.lang.RuntimeException: DB error
	at com.aimedical.modules.ai.impl.metrics.LoggingMetricsCollector.record(LoggingMetricsCollector.java:51) ~[classes/:na]
	at com.aimedical.modules.ai.impl.metrics.LoggingMetricsCollectorTest.lambda$shouldHandleRepositoryExceptionGracefully$0(LoggingMetricsCollectorTest.java:97) ~[test-classes/:na]
	at org.junit.jupiter.api.AssertDoesNotThrow.assertDoesNotThrow(AssertDoesNotThrow.java:49) ~[junit-jupiter-api-5.10.2.jar:5.10.2]
	at org.junit.jupiter.api.AssertDoesNotThrow.assertDoesNotThrow(AssertDoesNotThrow.java:36) ~[junit-jupiter-api-5.10.2.jar:5.10.2]
	at org.junit.jupiter.api.Assertions.assertDoesNotThrow(Assertions.java:3168) ~[junit-jupiter-api-5.10.2.jar:5.10.2]
	at com.aimedical.modules.ai.impl.metrics.LoggingMetricsCollectorTest.shouldHandleRepositoryExceptionGracefully(LoggingMetricsCollectorTest.java:97) ~[test-classes/:na]
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

2026-07-03T04:19:30.027+08:00  WARN 13416 --- [           main] c.a.m.a.i.m.LoggingMetricsCollector      : AiCallRecord is null, skipping persistence
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.097 s -- in com.aimedical.modules.ai.impl.metrics.LoggingMetricsCollectorTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManagerTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManagerTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStoreTest
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.026 s -- in com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStoreTest
[INFO] Running com.aimedical.modules.ai.impl.mock.MockAdminControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.017 s -- in com.aimedical.modules.ai.impl.mock.MockAdminControllerTest
[INFO] Running com.aimedical.modules.ai.impl.mock.MockAiServiceTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s -- in com.aimedical.modules.ai.impl.mock.MockAiServiceTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutorTest
2026-07-03T04:19:30.272+08:00  WARN 13416 --- [           main] c.a.m.a.i.o.AbstractCapabilityExecutor   : �����Կ���ʧ��: capabilityId=TEST, inputType=class java.lang.Object, ���˵�ԭʼ request
2026-07-03T04:19:30.310+08:00 ERROR 13416 --- [ool-37-thread-1] c.a.m.a.i.o.AbstractCapabilityExecutor   : CapabilityExecutor �����쳣: capabilityId=TEST, cause=com.aimedical.modules.diagnosis.TestPhase4Exception: test phase4 exception
2026-07-03T04:19:30.402+08:00 ERROR 13416 --- [ool-54-thread-1] c.a.m.a.i.o.AbstractCapabilityExecutor   : CapabilityExecutor �����쳣: capabilityId=TEST, cause=java.lang.RuntimeException: unknown
[INFO] Tests run: 59, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.537 s -- in com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest
2026-07-03T04:19:30.647+08:00 ERROR 13416 --- [           main] c.a.m.a.i.orchestrator.AiOrchestrator    : ִ�� capability ʱ�����쳣: capabilityId=TRIAGE

java.lang.IllegalStateException: test error
	at com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest.shouldRecordFailureOnSyncException(AiOrchestratorTest.java:121) ~[test-classes/:na]
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

2026-07-03T04:19:30.663+08:00  WARN 13416 --- [           main] c.a.m.a.i.orchestrator.AiOrchestrator    : �ظ� capabilityId ������: capabilityId=TRIAGE, oldExecutor=com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest$3, newExecutor=com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest$4
2026-07-03T04:19:30.665+08:00 ERROR 13416 --- [           main] c.a.m.a.i.orchestrator.AiOrchestrator    : ִ�� capability ʱ�����쳣: capabilityId=TRIAGE

java.lang.IllegalStateException: test error
	at com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest.shouldReturnFailureOnExecutorSyncException(AiOrchestratorTest.java:112) ~[test-classes/:na]
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

2026-07-03T04:19:30.678+08:00 ERROR 13416 --- [           main] c.a.m.a.i.orchestrator.AiOrchestrator    : ִ�� capability ʱ�����쳣: capabilityId=TRIAGE

java.lang.IllegalStateException: test error
	at com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest.shouldRecordAiCallRecordOnSyncException(AiOrchestratorTest.java:137) ~[test-classes/:na]
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

[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.049 s -- in com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.DiscussionConclusionCapabilityExecutorTest
2026-07-03T04:19:30.721+08:00  WARN 13416 --- [           main] i.DiscussionConclusionCapabilityExecutor : transcript ѹ��ʧ�ܣ����˽ض�: java.util.concurrent.ExecutionException: java.lang.RuntimeException: Compression failed: compress_error
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.061 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.DiscussionConclusionCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.TriageCapabilityExecutorTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.TriageCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.parser.JsonStructuredOutputParserTest
2026-07-03T04:19:30.767+08:00  WARN 13416 --- [           main] c.a.m.a.i.p.JsonStructuredOutputParser   : Failed to parse JSON content to String

com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot deserialize value of type `java.lang.String` from Array value (token `JsonToken.START_ARRAY`)
 at [Source: (String)"[1,2,3]"; line: 1, column: 1]
	at com.fasterxml.jackson.databind.exc.MismatchedInputException.from(MismatchedInputException.java:59) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.DeserializationContext.reportInputMismatch(DeserializationContext.java:1752) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.DeserializationContext.handleUnexpectedToken(DeserializationContext.java:1526) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.deser.std.StdDeserializer._deserializeFromArray(StdDeserializer.java:222) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.deser.std.StringDeserializer.deserialize(StringDeserializer.java:46) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.deser.std.StringDeserializer.deserialize(StringDeserializer.java:11) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.readRootValue(DefaultDeserializationContext.java:323) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4825) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3772) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3740) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.aimedical.modules.ai.impl.parser.JsonStructuredOutputParser.parse(JsonStructuredOutputParser.java:27) ~[classes/:na]
	at com.aimedical.modules.ai.impl.parser.JsonStructuredOutputParserTest.lambda$shouldThrowExceptionForTypeMismatch$4(JsonStructuredOutputParserTest.java:62) ~[test-classes/:na]
	at org.junit.jupiter.api.AssertThrows.assertThrows(AssertThrows.java:53) ~[junit-jupiter-api-5.10.2.jar:5.10.2]
	at org.junit.jupiter.api.AssertThrows.assertThrows(AssertThrows.java:35) ~[junit-jupiter-api-5.10.2.jar:5.10.2]
	at org.junit.jupiter.api.Assertions.assertThrows(Assertions.java:3115) ~[junit-jupiter-api-5.10.2.jar:5.10.2]
	at com.aimedical.modules.ai.impl.parser.JsonStructuredOutputParserTest.shouldThrowExceptionForTypeMismatch(JsonStructuredOutputParserTest.java:61) ~[test-classes/:na]
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

2026-07-03T04:19:30.771+08:00  WARN 13416 --- [           main] c.a.m.a.i.p.JsonStructuredOutputParser   : Failed to parse JSON content to String

com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot deserialize value of type `java.lang.String` from Object value (token `JsonToken.START_OBJECT`)
 at [Source: (String)"{invalid}"; line: 1, column: 1]
	at com.fasterxml.jackson.databind.exc.MismatchedInputException.from(MismatchedInputException.java:59) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.DeserializationContext.reportInputMismatch(DeserializationContext.java:1752) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.DeserializationContext.handleUnexpectedToken(DeserializationContext.java:1526) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.DeserializationContext.handleUnexpectedToken(DeserializationContext.java:1431) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.DeserializationContext.extractScalarFromObject(DeserializationContext.java:943) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.deser.std.StdDeserializer._parseString(StdDeserializer.java:1424) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.deser.std.StringDeserializer.deserialize(StringDeserializer.java:48) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.deser.std.StringDeserializer.deserialize(StringDeserializer.java:11) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.readRootValue(DefaultDeserializationContext.java:323) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4825) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3772) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3740) ~[jackson-databind-2.15.4.jar:2.15.4]
	at com.aimedical.modules.ai.impl.parser.JsonStructuredOutputParser.parse(JsonStructuredOutputParser.java:27) ~[classes/:na]
	at com.aimedical.modules.ai.impl.parser.JsonStructuredOutputParserTest.lambda$shouldThrowExceptionForInvalidJson$3(JsonStructuredOutputParserTest.java:55) ~[test-classes/:na]
	at org.junit.jupiter.api.AssertThrows.assertThrows(AssertThrows.java:53) ~[junit-jupiter-api-5.10.2.jar:5.10.2]
	at org.junit.jupiter.api.AssertThrows.assertThrows(AssertThrows.java:35) ~[junit-jupiter-api-5.10.2.jar:5.10.2]
	at org.junit.jupiter.api.Assertions.assertThrows(Assertions.java:3115) ~[junit-jupiter-api-5.10.2.jar:5.10.2]
	at com.aimedical.modules.ai.impl.parser.JsonStructuredOutputParserTest.shouldThrowExceptionForInvalidJson(JsonStructuredOutputParserTest.java:54) ~[test-classes/:na]
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

[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.015 s -- in com.aimedical.modules.ai.impl.parser.JsonStructuredOutputParserTest
[INFO] Running com.aimedical.modules.ai.impl.parser.StructuredOutputParserTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.066 s -- in com.aimedical.modules.ai.impl.parser.StructuredOutputParserTest
[INFO] Running com.aimedical.modules.ai.impl.pom.AiImplPomCleanDependencyTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.049 s -- in com.aimedical.modules.ai.impl.pom.AiImplPomCleanDependencyTest
[INFO] Running com.aimedical.modules.ai.impl.router.AiRouterPropertiesTest
2026-07-03T04:19:30.897+08:00  WARN 13416 --- [           main] c.a.m.ai.impl.router.AiRouterProperties  : Invalid clientType value: 'BAD_CLIENT', falling back to HTTP_API
2026-07-03T04:19:30.897+08:00  WARN 13416 --- [           main] c.a.m.ai.impl.router.AiRouterProperties  : Invalid authType value: 'BAD_AUTH', falling back to NONE
2026-07-03T04:19:30.898+08:00  WARN 13416 --- [           main] c.a.m.ai.impl.router.AiRouterProperties  : Invalid clientType value: 'INVALID_CLIENT', falling back to HTTP_API
2026-07-03T04:19:30.905+08:00  WARN 13416 --- [           main] c.a.m.ai.impl.router.AiRouterProperties  : Invalid authType value: 'INVALID_AUTH', falling back to NONE
[ERROR] Tests run: 8, Failures: 2, Errors: 1, Skipped: 0, Time elapsed: 0.013 s <<< FAILURE! -- in com.aimedical.modules.ai.impl.router.AiRouterPropertiesTest
[ERROR] com.aimedical.modules.ai.impl.router.AiRouterPropertiesTest.shouldLogWarningForInvalidClientType -- Time elapsed: 0.005 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <true> but was: <false>
	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:151)
	at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
	at org.junit.jupiter.api.AssertTrue.failNotTrue(AssertTrue.java:63)
	at org.junit.jupiter.api.AssertTrue.assertTrue(AssertTrue.java:36)
	at org.junit.jupiter.api.AssertTrue.assertTrue(AssertTrue.java:31)
	at org.junit.jupiter.api.Assertions.assertTrue(Assertions.java:183)
	at com.aimedical.modules.ai.impl.router.AiRouterPropertiesTest.shouldLogWarningForInvalidClientType(AiRouterPropertiesTest.java:73)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[ERROR] com.aimedical.modules.ai.impl.router.AiRouterPropertiesTest.shouldLogWarningForInvalidAuthType -- Time elapsed: 0.001 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <true> but was: <false>
	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:151)
	at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
	at org.junit.jupiter.api.AssertTrue.failNotTrue(AssertTrue.java:63)
	at org.junit.jupiter.api.AssertTrue.assertTrue(AssertTrue.java:36)
	at org.junit.jupiter.api.AssertTrue.assertTrue(AssertTrue.java:31)
	at org.junit.jupiter.api.Assertions.assertTrue(Assertions.java:183)
	at com.aimedical.modules.ai.impl.router.AiRouterPropertiesTest.shouldLogWarningForInvalidAuthType(AiRouterPropertiesTest.java:95)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[ERROR] com.aimedical.modules.ai.impl.router.AiRouterPropertiesTest.shouldHandleNullConfigGracefully -- Time elapsed: 0.001 s <<< ERROR!
java.lang.NullPointerException
	at java.base/java.util.Objects.requireNonNull(Objects.java:233)
	at java.base/java.util.ImmutableCollections$List12.<init>(ImmutableCollections.java:563)
	at java.base/java.util.List.of(List.java:937)
	at com.aimedical.modules.ai.impl.router.AiRouterPropertiesTest.shouldHandleNullConfigGracefully(AiRouterPropertiesTest.java:160)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[INFO] Running com.aimedical.modules.ai.impl.router.DefaultModelRouterTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.ai.impl.router.DefaultModelRouterTest
[INFO] Running com.aimedical.modules.ai.impl.router.ModelRouteTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in com.aimedical.modules.ai.impl.router.ModelRouteTest
[INFO] Running com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManagerTest
2026-07-03T04:19:30.998+08:00  INFO 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : warmup completed: 2 active templates cached
2026-07-03T04:19:30.999+08:00  INFO 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : warmup completed: 2 active templates cached
2026-07-03T04:19:31.000+08:00  INFO 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : warmup completed: 1 active templates cached
2026-07-03T04:19:31.001+08:00  INFO 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : warmup completed: 2 active templates cached
2026-07-03T04:19:31.004+08:00  INFO 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : warmup completed: 1 active templates cached
2026-07-03T04:19:31.012+08:00  INFO 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : warmup completed: 1 active templates cached
2026-07-03T04:19:31.013+08:00  WARN 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : exact version 2 found but status=DEPRECATED for capabilityId=diag, fallback to ACTIVE
2026-07-03T04:19:31.015+08:00  WARN 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : render failed for capabilityId=diag: java.lang.RuntimeException: DB error
2026-07-03T04:19:31.018+08:00  INFO 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : warmup completed: 1 active templates cached
2026-07-03T04:19:31.019+08:00  INFO 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : warmup completed: 1 active templates cached
2026-07-03T04:19:31.019+08:00  WARN 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : variable 'department' is null, placeholder '{{department}}' retained
2026-07-03T04:19:31.020+08:00  INFO 13416 --- [           main] .a.m.a.i.t.DatabasePromptTemplateManager : warmup completed: 1 active templates cached
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.098 s -- in com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManagerTest
[INFO] Running com.aimedical.modules.ai.impl.template.PromptTemplateTest
2026-07-03T04:19:31.025+08:00  INFO 13416 --- [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [com.aimedical.modules.ai.impl.template.PromptTemplateTest]: PromptTemplateTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2026-07-03T04:19:31.049+08:00  INFO 13416 --- [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration com.aimedical.modules.ai.impl.template.PromptTemplateTestConfig for test class com.aimedical.modules.ai.impl.template.PromptTemplateTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

2026-07-03T04:19:31.067+08:00  INFO 13416 --- [           main] c.a.m.a.i.template.PromptTemplateTest    : Starting PromptTemplateTest using Java 21.0.11 with PID 13416 (started by laoE in C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl)
2026-07-03T04:19:31.067+08:00  INFO 13416 --- [           main] c.a.m.a.i.template.PromptTemplateTest    : No active profile set, falling back to 1 default profile: "default"
2026-07-03T04:19:31.119+08:00  INFO 13416 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-03T04:19:31.124+08:00  INFO 13416 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 5 ms. Found 1 JPA repository interface.
2026-07-03T04:19:31.133+08:00  INFO 13416 --- [           main] beddedDataSourceBeanFactoryPostProcessor : Replacing 'dataSource' DataSource bean with embedded version
2026-07-03T04:19:31.145+08:00  INFO 13416 --- [           main] o.s.j.d.e.EmbeddedDatabaseFactory        : Starting embedded database: url='jdbc:h2:mem:6ba8ec45-66da-456b-bdc7-d3824fde31ba;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', username='sa'
2026-07-03T04:19:31.160+08:00  INFO 13416 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-03T04:19:31.162+08:00  INFO 13416 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-03T04:19:31.165+08:00  INFO 13416 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-03T04:19:31.203+08:00  INFO 13416 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: drop table if exists ai_prompt_template cascade 
Hibernate: create table ai_prompt_template (version integer not null, id bigint generated by default as identity, status varchar(20) not null check (status in ('DRAFT','ACTIVE','DEPRECATED')), capability_id varchar(50) not null, department_id varchar(50), content TEXT not null, primary key (id), unique (capability_id, department_id, version))
2026-07-03T04:19:31.212+08:00  INFO 13416 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-03T04:19:31.244+08:00  INFO 13416 --- [           main] c.a.m.a.i.template.PromptTemplateTest    : Started PromptTemplateTest in 0.192 seconds (process running for 13.013)
Hibernate: insert into ai_prompt_template (capability_id,content,department_id,status,version,id) values (?,?,?,?,?,default)
Hibernate: insert into ai_prompt_template (capability_id,content,department_id,status,version,id) values (?,?,?,?,?,default)
2026-07-03T04:19:31.275+08:00  WARN 13416 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 23505, SQLState: 23505
2026-07-03T04:19:31.275+08:00 ERROR 13416 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Unique index or primary key violation: "PUBLIC.CONSTRAINT_INDEX_8 ON PUBLIC.AI_PROMPT_TEMPLATE(CAPABILITY_ID NULLS FIRST, DEPARTMENT_ID NULLS FIRST, VERSION NULLS FIRST) VALUES ( /* key:1 */ 'diag', 'dept1', 1)"; SQL statement:
insert into ai_prompt_template (capability_id,content,department_id,status,version,id) values (?,?,?,?,?,default) [23505-224]
Hibernate: insert into ai_prompt_template (capability_id,content,department_id,status,version,id) values (?,?,?,?,?,default)
Hibernate: insert into ai_prompt_template (capability_id,content,department_id,status,version,id) values (?,?,?,?,?,default)
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.294 s -- in com.aimedical.modules.ai.impl.template.PromptTemplateTest
[INFO] Running com.aimedical.modules.ai.impl.thinadapter.AnalysisReportForInspectionCapabilityExecutorTest
2026-07-03T04:19:31.378+08:00  WARN 13416 --- [           main] sisReportForInspectionCapabilityExecutor : ThinAdapter ί�г�ʱ: capabilityId=ANALYSIS_REPORT_INSPECTION, thinAdapterTimeout=50ms
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.062 s -- in com.aimedical.modules.ai.impl.thinadapter.AnalysisReportForInspectionCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.thinadapter.AnalysisReportForLabTestCapabilityExecutorTest
2026-07-03T04:19:31.440+08:00  WARN 13416 --- [           main] alysisReportForLabTestCapabilityExecutor : ThinAdapter ί�г�ʱ: capabilityId=ANALYSIS_REPORT_LABTEST, thinAdapterTimeout=50ms
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.063 s -- in com.aimedical.modules.ai.impl.thinadapter.AnalysisReportForLabTestCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.thinadapter.DiagnosisCapabilityExecutorTest
2026-07-03T04:19:31.503+08:00  WARN 13416 --- [           main] c.a.m.a.i.t.DiagnosisCapabilityExecutor  : ThinAdapter ί�г�ʱ: capabilityId=DIAGNOSIS, thinAdapterTimeout=50ms
2026-07-03T04:19:31.565+08:00  WARN 13416 --- [           main] c.a.m.a.i.t.DiagnosisCapabilityExecutor  : ThinAdapter ί�г�ʱ: capabilityId=DIAGNOSIS, thinAdapterTimeout=50ms
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.126 s -- in com.aimedical.modules.ai.impl.thinadapter.DiagnosisCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.thinadapter.ImageAnalysisCapabilityExecutorTest
2026-07-03T04:19:31.627+08:00  WARN 13416 --- [           main] .m.a.i.t.ImageAnalysisCapabilityExecutor : ThinAdapter ί�г�ʱ: capabilityId=IMAGE_ANALYSIS, thinAdapterTimeout=50ms
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.060 s -- in com.aimedical.modules.ai.impl.thinadapter.ImageAnalysisCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.thinadapter.RecommendExaminationCapabilityExecutorTest
2026-07-03T04:19:31.690+08:00  WARN 13416 --- [           main] t.RecommendExaminationCapabilityExecutor : ThinAdapter ί�г�ʱ: capabilityId=RECOMMEND_EXAM, thinAdapterTimeout=50ms
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.064 s -- in com.aimedical.modules.ai.impl.thinadapter.RecommendExaminationCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.thinadapter.RecommendExecutionOrderCapabilityExecutorTest
2026-07-03T04:19:31.752+08:00  WARN 13416 --- [           main] ecommendExecutionOrderCapabilityExecutor : ThinAdapter ί�г�ʱ: capabilityId=RECOMMEND_EXEC_ORDER, thinAdapterTimeout=50ms
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.060 s -- in com.aimedical.modules.ai.impl.thinadapter.RecommendExecutionOrderCapabilityExecutorTest
[INFO] 
[INFO] Results:
[INFO] 
[ERROR] Failures: 
[ERROR]   AiRouterPropertiesTest.shouldLogWarningForInvalidAuthType:95 expected: <true> but was: <false>
[ERROR]   AiRouterPropertiesTest.shouldLogWarningForInvalidClientType:73 expected: <true> but was: <false>
[ERROR] Errors: 
[ERROR]   AiRouterPropertiesTest.shouldHandleNullConfigGracefully:160 ? NullPointer
[INFO] 
[ERROR] Tests run: 572, Failures: 2, Errors: 1, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for aimedical-sys 0.0.1-SNAPSHOT:
[INFO] 
[INFO] aimedical-sys ...................................... SUCCESS [  0.219 s]
[INFO] common ............................................. SUCCESS [  7.876 s]
[INFO] Common Module Aggregator ........................... SUCCESS [  0.007 s]
[INFO] common-module-api .................................. SUCCESS [  1.493 s]
[INFO] common-module-impl ................................. SUCCESS [ 24.293 s]
[INFO] AI Module Aggregator ............................... SUCCESS [  0.006 s]
[INFO] ai-api ............................................. SUCCESS [  2.273 s]
[INFO] ai-impl ............................................ FAILURE [ 19.993 s]
[INFO] patient ............................................ SKIPPED
[INFO] doctor ............................................. SKIPPED
[INFO] admin .............................................. SKIPPED
[INFO] registration ....................................... SKIPPED
[INFO] medical-order ...................................... SKIPPED
[INFO] consultation ....................................... SKIPPED
[INFO] prescription ....................................... SKIPPED
[INFO] medical-record ..................................... SKIPPED
[INFO] application ........................................ SKIPPED
[INFO] integration ........................................ SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  56.614 s
[INFO] Finished at: 2026-07-03T04:19:31+08:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.1.2:test (default-test) on project ai-impl: There are test failures.
[ERROR] 
[ERROR] Please refer to C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\target\surefire-reports for the individual test results.
[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.
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
