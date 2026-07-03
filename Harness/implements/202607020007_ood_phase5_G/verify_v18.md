# 验证报告（v18）

## 结果
PASSED

## 统计
- 通过：425
- 失败：0
- 跳过：0

## 测试执行日志
[INFO] Scanning for projects...
[INFO] 
[INFO] -----------------------< com.aimedical:ai-impl >------------------------
[INFO] Building ai-impl 0.0.1-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- jacoco:0.8.12:prepare-agent (jacoco-prepare-agent) @ ai-impl ---
[INFO] argLine set to -javaagent:C:\\Users\\laoE\\.m2\\repository\\org\\jacoco\\org.jacoco.agent\\0.8.12\\org.jacoco.agent-0.8.12-runtime.jar=destfile=C:\\Develop\\Software\\AIMedicalSys\\AIMedical\\backend\\modules\\ai\\ai-impl\\target\\jacoco.exec
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ ai-impl ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\src\main\resources
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\src\main\resources
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ ai-impl ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ ai-impl ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ ai-impl ---
[INFO] Changes detected - recompiling the module! :source
[INFO] Compiling 52 source files with javac [debug release 17] to target\test-classes
[INFO] 
[INFO] --- surefire:3.1.2:test (default-test) @ ai-impl ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.aimedical.modules.ai.impl.client.AuthTypeTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.129 s -- in com.aimedical.modules.ai.impl.client.AuthTypeTest
[INFO] Running com.aimedical.modules.ai.impl.client.ChatToolDefinitionTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.528 s -- in com.aimedical.modules.ai.impl.client.ChatToolDefinitionTest
[INFO] Running com.aimedical.modules.ai.impl.client.ClientTypeTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.ai.impl.client.ClientTypeTest
[INFO] Running com.aimedical.modules.ai.impl.client.CredentialProviderTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.ai.impl.client.CredentialProviderTest
[INFO] Running com.aimedical.modules.ai.impl.client.DefaultCredentialProviderTest
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.515 s -- in com.aimedical.modules.ai.impl.client.DefaultCredentialProviderTest
[INFO] Running com.aimedical.modules.ai.impl.client.DelegatingLlmChatServiceTest
12:09:15.292 [main] ERROR com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=SPRING_AI ��ʵ�֣����˵� HTTP_API
12:09:15.301 [main] ERROR com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=null ��ʵ�֣����˵� HTTP_API
12:09:15.303 [main] ERROR com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=SPRING_AI ��ʵ�֣����˵� HTTP_API
12:09:15.304 [main] ERROR com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=null ��ʵ�֣����˵� HTTP_API
12:09:15.317 [main] ERROR com.aimedical.modules.ai.impl.client.DelegatingLlmChatService -- δ�ҵ� ClientType=null ��ʵ�֣����˵� HTTP_API
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.047 s -- in com.aimedical.modules.ai.impl.client.DelegatingLlmChatServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.EndpointRateLimiterTest
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
WARNING: A Java agent has been loaded dynamically (C:\Users\laoE\.m2\repository\net\bytebuddy\byte-buddy-agent\1.14.13\byte-buddy-agent-1.14.13.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.582 s -- in com.aimedical.modules.ai.impl.client.EndpointRateLimiterTest
[INFO] Running com.aimedical.modules.ai.impl.client.exception.AiAbilityInputInvalidExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in com.aimedical.modules.ai.impl.client.exception.AiAbilityInputInvalidExceptionTest
[INFO] Running com.aimedical.modules.ai.impl.client.exception.CredentialUnavailableExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in com.aimedical.modules.ai.impl.client.exception.CredentialUnavailableExceptionTest
[INFO] Running com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureExceptionTest
[INFO] Running com.aimedical.modules.ai.impl.client.exception.StructuredOutputNotSupportedExceptionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.ai.impl.client.exception.StructuredOutputNotSupportedExceptionTest
[INFO] Running com.aimedical.modules.ai.impl.client.HttpApiLlmChatServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.388 s -- in com.aimedical.modules.ai.impl.client.HttpApiLlmChatServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.HttpApiLlmChatStreamServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.304 s -- in com.aimedical.modules.ai.impl.client.HttpApiLlmChatStreamServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatMessageRoleTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in com.aimedical.modules.ai.impl.client.LlmChatMessageRoleTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatMessageTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.035 s -- in com.aimedical.modules.ai.impl.client.LlmChatMessageTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatOptionsTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.043 s -- in com.aimedical.modules.ai.impl.client.LlmChatOptionsTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatRequestTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.041 s -- in com.aimedical.modules.ai.impl.client.LlmChatRequestTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatResponseTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s -- in com.aimedical.modules.ai.impl.client.LlmChatResponseTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s -- in com.aimedical.modules.ai.impl.client.LlmChatServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.LlmChatStreamServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.impl.client.LlmChatStreamServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.SpringAiLlmChatServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in com.aimedical.modules.ai.impl.client.SpringAiLlmChatServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.SpringAiLlmChatStreamServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in com.aimedical.modules.ai.impl.client.SpringAiLlmChatStreamServiceTest
[INFO] Running com.aimedical.modules.ai.impl.client.StructuredChatResultTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.023 s -- in com.aimedical.modules.ai.impl.client.StructuredChatResultTest
[INFO] Running com.aimedical.modules.ai.impl.degradation.CircuitBreakerDegradationStrategyTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.458 s -- in com.aimedical.modules.ai.impl.degradation.CircuitBreakerDegradationStrategyTest
[INFO] Running com.aimedical.modules.ai.impl.degradation.NoOpDegradationStrategyTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.009 s -- in com.aimedical.modules.ai.impl.degradation.NoOpDegradationStrategyTest
[INFO] Running com.aimedical.modules.ai.impl.degradation.TimeoutDegradationStrategyTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.010 s -- in com.aimedical.modules.ai.impl.degradation.TimeoutDegradationStrategyTest
[INFO] Running com.aimedical.modules.ai.impl.experiment.ExperimentAssignmentTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.022 s -- in com.aimedical.modules.ai.impl.experiment.ExperimentAssignmentTest
[INFO] Running com.aimedical.modules.ai.impl.experiment.HashBucketExperimentManagerTest
12:09:19.650 [main] INFO com.aimedical.modules.ai.impl.experiment.HashBucketExperimentManager -- warmup completed: 1 capability groups cached
12:09:19.680 [main] WARN com.aimedical.modules.ai.impl.experiment.HashBucketExperimentManager -- warmup failed: java.lang.RuntimeException: DB error
12:09:19.737 [main] WARN com.aimedical.modules.ai.impl.experiment.HashBucketExperimentManager -- assign failed for capabilityId=cap1: java.lang.RuntimeException: DB connection failed
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.355 s -- in com.aimedical.modules.ai.impl.experiment.HashBucketExperimentManagerTest
[INFO] Running com.aimedical.modules.ai.impl.fallback.FallbackAiServiceTest
12:09:19.825 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.825 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.834 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.834 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.872 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.873 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.873 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.873 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.873 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.875 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.876 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.887 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.891 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.891 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.892 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.892 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.892 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.892 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.892 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.893 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.893 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.895 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.895 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.903 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.904 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.908 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.908 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.912 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.916 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.916 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.916 [main] ERROR com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
12:09:19.916 [main] WARN com.aimedical.modules.ai.impl.fallback.FallbackAiService -- No available AiService delegate
[INFO] Tests run: 36, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.170 s -- in com.aimedical.modules.ai.impl.fallback.FallbackAiServiceTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.AiCallRecordTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.impl.metrics.AiCallRecordTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManagerTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManagerTest
[INFO] Running com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStoreTest
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.043 s -- in com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStoreTest
[INFO] Running com.aimedical.modules.ai.impl.mock.MockAdminControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.041 s -- in com.aimedical.modules.ai.impl.mock.MockAdminControllerTest
[INFO] Running com.aimedical.modules.ai.impl.mock.MockAiServiceTest
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.025 s -- in com.aimedical.modules.ai.impl.mock.MockAiServiceTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutorTest
12:09:20.275 [main] WARN com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutor -- �����Կ���ʧ��: capabilityId=TEST, inputType=class java.lang.Object, ���˵�ԭʼ request
12:09:20.432 [pool-41-thread-1] ERROR com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutor -- CapabilityExecutor �����쳣: capabilityId=TEST, cause=java.lang.RuntimeException: unknown
[INFO] Tests run: 48, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.643 s -- in com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest
12:09:20.703 [main] ERROR com.aimedical.modules.ai.impl.orchestrator.AiOrchestrator -- ִ�� capability ʱ�����쳣: capabilityId=TRIAGE
java.lang.IllegalStateException: test error
	at com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest.shouldRecordFailureOnSyncException(AiOrchestratorTest.java:123)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:728)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$7(TestMethodTestDescriptor.java:218)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:214)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:139)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:69)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35)
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57)
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103)
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85)
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47)
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56)
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184)
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148)
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122)
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385)
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162)
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507)
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495)
12:09:20.732 [main] WARN com.aimedical.modules.ai.impl.orchestrator.AiOrchestrator -- �ظ� capabilityId ������: capabilityId=TRIAGE, oldExecutor=com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest$2, newExecutor=com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest$3
12:09:20.735 [main] ERROR com.aimedical.modules.ai.impl.orchestrator.AiOrchestrator -- ִ�� capability ʱ�����쳣: capabilityId=TRIAGE
java.lang.IllegalStateException: test error
	at com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest.shouldReturnFailureOnExecutorSyncException(AiOrchestratorTest.java:114)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:728)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$7(TestMethodTestDescriptor.java:218)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:214)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:139)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:69)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35)
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:198)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:169)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:93)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:58)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:141)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:57)
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103)
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85)
	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47)
	at org.apache.maven.surefire.junitplatform.LazyLauncher.execute(LazyLauncher.java:56)
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.execute(JUnitPlatformProvider.java:184)
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invokeAllTests(JUnitPlatformProvider.java:148)
	at org.apache.maven.surefire.junitplatform.JUnitPlatformProvider.invoke(JUnitPlatformProvider.java:122)
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:385)
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:162)
	at org.apache.maven.surefire.booter.ForkedBooter.run(ForkedBooter.java:507)
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:495)
12:09:20.739 [main] WARN com.aimedical.modules.ai.impl.orchestrator.AiOrchestrator -- δע��������ʶ: capabilityId=TRIAGE
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.053 s -- in com.aimedical.modules.ai.impl.orchestrator.AiOrchestratorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.AnalysisReportForInspectionCapabilityExecutorTest
12:09:20.833 [main] WARN com.aimedical.modules.ai.impl.orchestrator.impl.AnalysisReportForInspectionCapabilityExecutor -- ThinAdapter ί�г�ʱ: capabilityId=ANALYSIS_REPORT_INSPECTION, thinAdapterTimeout=50ms
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.119 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.AnalysisReportForInspectionCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.AnalysisReportForLabTestCapabilityExecutorTest
12:09:20.942 [main] WARN com.aimedical.modules.ai.impl.orchestrator.impl.AnalysisReportForLabTestCapabilityExecutor -- ThinAdapter ί�г�ʱ: capabilityId=ANALYSIS_REPORT_LABTEST, thinAdapterTimeout=50ms
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.105 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.AnalysisReportForLabTestCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.DiagnosisCapabilityExecutorTest
12:09:21.051 [main] WARN com.aimedical.modules.ai.impl.orchestrator.impl.DiagnosisCapabilityExecutor -- ThinAdapter ί�г�ʱ: capabilityId=DIAGNOSIS, thinAdapterTimeout=50ms
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.109 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.DiagnosisCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.DiscussionConclusionCapabilityExecutorTest
12:09:21.132 [main] WARN com.aimedical.modules.ai.impl.orchestrator.impl.DiscussionConclusionCapabilityExecutor -- transcript ѹ��ʧ�ܣ����˽ض�: java.util.concurrent.ExecutionException: java.lang.RuntimeException: Compression failed: compress_error
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.081 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.DiscussionConclusionCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.ImageAnalysisCapabilityExecutorTest
12:09:21.239 [main] WARN com.aimedical.modules.ai.impl.orchestrator.impl.ImageAnalysisCapabilityExecutor -- ThinAdapter ί�г�ʱ: capabilityId=IMAGE_ANALYSIS, thinAdapterTimeout=50ms
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.107 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.ImageAnalysisCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.RecommendExaminationCapabilityExecutorTest
12:09:21.348 [main] WARN com.aimedical.modules.ai.impl.orchestrator.impl.RecommendExaminationCapabilityExecutor -- ThinAdapter ί�г�ʱ: capabilityId=RECOMMEND_EXAM, thinAdapterTimeout=50ms
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.104 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.RecommendExaminationCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.RecommendExecutionOrderCapabilityExecutorTest
12:09:21.459 [main] WARN com.aimedical.modules.ai.impl.orchestrator.impl.RecommendExecutionOrderCapabilityExecutor -- ThinAdapter ί�г�ʱ: capabilityId=RECOMMEND_EXEC_ORDER, thinAdapterTimeout=50ms
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.111 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.RecommendExecutionOrderCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.TriageCapabilityExecutorTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.021 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.TriageCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.parser.StructuredOutputParserTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.069 s -- in com.aimedical.modules.ai.impl.parser.StructuredOutputParserTest
[INFO] Running com.aimedical.modules.ai.impl.pom.AiImplPomCleanDependencyTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.113 s -- in com.aimedical.modules.ai.impl.pom.AiImplPomCleanDependencyTest
[INFO] Running com.aimedical.modules.ai.impl.router.DefaultModelRouterTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in com.aimedical.modules.ai.impl.router.DefaultModelRouterTest
[INFO] Running com.aimedical.modules.ai.impl.router.ModelRouteTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.aimedical.modules.ai.impl.router.ModelRouteTest
[INFO] Running com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManagerTest
12:09:21.788 [main] WARN com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- invalid promptVersion 'not-a-number' for capabilityId=diag, fallback to ACTIVE
12:09:21.790 [main] INFO com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- warmup completed: 2 active templates cached
12:09:21.792 [main] INFO com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- warmup completed: 2 active templates cached
12:09:21.795 [main] INFO com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- warmup completed: 2 active templates cached
12:09:21.799 [main] INFO com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- warmup completed: 1 active templates cached
12:09:21.809 [main] WARN com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- exact version 2 found but status=DEPRECATED for capabilityId=diag, fallback to ACTIVE
12:09:21.814 [main] WARN com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- render failed for capabilityId=diag: java.lang.RuntimeException: DB error
12:09:21.819 [main] INFO com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- warmup completed: 1 active templates cached
12:09:21.820 [main] INFO com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- warmup completed: 1 active templates cached
12:09:21.821 [main] WARN com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- variable 'department' is null, placeholder '{{department}}' retained
12:09:21.822 [main] INFO com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManager -- warmup completed: 1 active templates cached
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.114 s -- in com.aimedical.modules.ai.impl.template.DatabasePromptTemplateManagerTest
[INFO] Running com.aimedical.modules.ai.impl.template.PromptTemplateTest
12:09:22.121 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [com.aimedical.modules.ai.impl.template.PromptTemplateTest]: PromptTemplateTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
12:09:22.623 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration com.aimedical.modules.ai.impl.template.PromptTemplateTestConfig for test class com.aimedical.modules.ai.impl.template.PromptTemplateTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

2026-07-02T12:09:23.315+08:00  INFO 29104 --- [           main] c.a.m.a.i.template.PromptTemplateTest    : Starting PromptTemplateTest using Java 21.0.11 with PID 29104 (started by laoE in C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl)
2026-07-02T12:09:23.317+08:00  INFO 29104 --- [           main] c.a.m.a.i.template.PromptTemplateTest    : No active profile set, falling back to 1 default profile: "default"
2026-07-02T12:09:23.809+08:00  INFO 29104 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-02T12:09:23.949+08:00  INFO 29104 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 127 ms. Found 1 JPA repository interface.
2026-07-02T12:09:24.031+08:00  INFO 29104 --- [           main] beddedDataSourceBeanFactoryPostProcessor : Replacing 'dataSource' DataSource bean with embedded version
2026-07-02T12:09:24.229+08:00  INFO 29104 --- [           main] o.s.j.d.e.EmbeddedDatabaseFactory        : Starting embedded database: url='jdbc:h2:mem:a24948ce-32eb-4a1c-8cc2-346c51e66d65;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', username='sa'
2026-07-02T12:09:24.783+08:00  INFO 29104 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-02T12:09:24.891+08:00  INFO 29104 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.4.4.Final
2026-07-02T12:09:24.945+08:00  INFO 29104 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-02T12:09:25.145+08:00  INFO 29104 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-02T12:09:26.248+08:00  INFO 29104 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Hibernate: drop table if exists ai_prompt_template cascade 
Hibernate: create table ai_prompt_template (version integer not null, id bigint generated by default as identity, status varchar(20) not null check (status in ('DRAFT','ACTIVE','DEPRECATED')), capability_id varchar(50) not null, department_id varchar(50), content TEXT not null, primary key (id), unique (capability_id, department_id, version))
2026-07-02T12:09:26.308+08:00  INFO 29104 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-02T12:09:26.811+08:00  INFO 29104 --- [           main] c.a.m.a.i.template.PromptTemplateTest    : Started PromptTemplateTest in 4.028 seconds (process running for 15.63)
Hibernate: insert into ai_prompt_template (capability_id,content,department_id,status,version,id) values (?,?,?,?,?,default)
Hibernate: insert into ai_prompt_template (capability_id,content,department_id,status,version,id) values (?,?,?,?,?,default)
2026-07-02T12:09:27.003+08:00  WARN 29104 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 23505, SQLState: 23505
2026-07-02T12:09:27.004+08:00 ERROR 29104 --- [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Unique index or primary key violation: "PUBLIC.CONSTRAINT_INDEX_8 ON PUBLIC.AI_PROMPT_TEMPLATE(CAPABILITY_ID NULLS FIRST, DEPARTMENT_ID NULLS FIRST, VERSION NULLS FIRST) VALUES ( /* key:1 */ 'diag', 'dept1', 1)"; SQL statement:
insert into ai_prompt_template (capability_id,content,department_id,status,version,id) values (?,?,?,?,?,default) [23505-224]
Hibernate: insert into ai_prompt_template (capability_id,content,department_id,status,version,id) values (?,?,?,?,?,default)
Hibernate: insert into ai_prompt_template (capability_id,content,department_id,status,version,id) values (?,?,?,?,?,default)
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.210 s -- in com.aimedical.modules.ai.impl.template.PromptTemplateTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 425, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ ai-impl ---
[INFO] Loading execution data file C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\target\jacoco.exec
[INFO] Analyzed bundle 'ai-impl' with 71 classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  24.556 s
[INFO] Finished at: 2026-07-02T12:09:27+08:00
[INFO] ------------------------------------------------------------------------
