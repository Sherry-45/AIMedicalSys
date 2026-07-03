# 验证报告（v4）

## 结果
PASSED

## 统计
- 通过：80
- 失败：0

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
[INFO] Copying 0 resource from src\main\resources to target\classes
[INFO] Copying 1 resource from src\main\resources to target\classes
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ ai-impl ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ ai-impl ---
[INFO] skip non existing resourceDirectory C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ ai-impl ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- surefire:3.1.2:test (default-test) @ ai-impl ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutorTest
WARNING: A Java agent has been loaded dynamically (C:\Users\laoE\.m2\repository\net\bytebuddy\byte-buddy-agent\1.14.13\byte-buddy-agent-1.14.13.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
00:51:11.823 [main] WARN com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutor -- �����Կ���ʧ��: capabilityId=TEST, inputType=class java.lang.Object, ���˵�ԭʼ request
00:51:11.856 [pool-34-thread-1] ERROR com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutor -- CapabilityExecutor �����쳣: capabilityId=TEST, cause=com.aimedical.modules.diagnosis.TestPhase4Exception: test phase4 exception
00:51:11.913 [pool-51-thread-1] ERROR com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutor -- CapabilityExecutor �����쳣: capabilityId=TEST, cause=java.lang.RuntimeException: unknown
[INFO] Tests run: 56, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.237 s -- in com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.DiscussionConclusionCapabilityExecutorTest
00:51:12.208 [main] WARN com.aimedical.modules.ai.impl.orchestrator.impl.DiscussionConclusionCapabilityExecutor -- transcript ѹ��ʧ�ܣ����˽ض�: java.util.concurrent.ExecutionException: java.lang.RuntimeException: Compression failed: compress_error
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.175 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.DiscussionConclusionCapabilityExecutorTest
[INFO] Running com.aimedical.modules.ai.impl.orchestrator.impl.TriageCapabilityExecutorTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.042 s -- in com.aimedical.modules.ai.impl.orchestrator.impl.TriageCapabilityExecutorTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 80, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- jacoco:0.8.12:report (jacoco-report) @ ai-impl ---
[INFO] Loading execution data file C:\Develop\Software\AIMedicalSys\AIMedical\backend\modules\ai\ai-impl\target\jacoco.exec
[INFO] Analyzed bundle 'ai-impl' with 95 classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  5.563 s
[INFO] Finished at: 2026-07-03T00:51:13+08:00
[INFO] ------------------------------------------------------------------------
