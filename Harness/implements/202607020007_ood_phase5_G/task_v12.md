# 任务指令（v12）

## 动作
RETRY

## 任务描述
修复 `AiImplPomCleanDependencyTest.totalDependenciesCountShouldBeSeven` 测试断言，使其与当前 `ai-impl/pom.xml` 实际依赖数一致。

### 具体修改
- **文件**：`ai-impl/src/test/java/.../pom/AiImplPomCleanDependencyTest.java:63`
- **修改**：`assertEquals(7, count.intValue())` → `assertEquals(8, count.intValue())`

### 原因
Task 9（v11）向 `ai-impl/pom.xml` 添加了 `io.projectreactor:reactor-core` 编译期依赖，使总依赖数从 7 增至 8。该测试硬编码的预期值未同步更新。

## 选择理由
最小修复，仅修改 1 行断言数值，无需变更生产代码或其他测试。

## 任务上下文

### 当前依赖清单（ai-impl/pom.xml）
1. `com.aimedical:ai-api` (compile)
2. `org.springframework.boot:spring-boot-starter` (compile)
3. `org.springframework.boot:spring-boot-starter-web` (compile)
4. `org.springframework.security:spring-security-core` (compile)
5. `io.projectreactor:reactor-core` (compile) — 本次新增
6. `com.github.ben-manes.caffeine:caffeine` (compile)
7. `com.google.guava:guava` (compile)
8. `org.springframework.boot:spring-boot-starter-test` (test)

共 8 个 `<dependency>` 元素。

### 已有代码上下文
```java
// AiImplPomCleanDependencyTest.java:60-64
@Test
void totalDependenciesCountShouldBeSeven() throws Exception {
    Double count = (Double) xpath.evaluate("count(/project/dependencies/dependency)", doc, XPathConstants.NUMBER);
    assertEquals(7, count.intValue());
}
```

## RETRY 说明
- **失败摘要**：verify_v11 中 pom 依赖计数测试断言硬编码值未随新增依赖更新
- **修正方向**：将 `assertEquals(7, ...)` → `assertEquals(8, ...)`
- **涉及文件**：仅 `AiImplPomCleanDependencyTest.java`（1 行修改）
- **无需重新跑完整测试**：修改后仅需验证 `AiImplPomCleanDependencyTest` 全部通过即可
