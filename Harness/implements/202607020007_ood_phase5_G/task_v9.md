# 任务指令（v9）

## 动作
RETRY

## 任务描述
修复 Task 6（7 项底座能力 CapabilityExecutor）测试代码中的 8 处 test-compile 错误。生产代码无需修改。

## 选择理由
verify_v8.md 报告 FAILED — 所有 8 个错误为同一类型：`StructuredOutputParser` 的泛型方法 `<T> T parse(String, Class<T>)` 在 lambda 表达式中无法进行类型推断。生产代码 main-compile 全部通过，仅 test-compile 失败。修复测试代码即可。

## 失败原因摘要（来自 verify_v8.md）
- **错误类型**：`StructuredOutputParser` 为泛型方法接口 `<T> T parse(String, Class<T>)`，Java 编译器无法从 lambda 表达式推断 `<T>`
- **错误位置**：
  1. `AbstractCapabilityExecutorTest.java` — 7 处（lines 597, 641, 885, 934, 978, 1024, 1070）
  2. `DiscussionConclusionCapabilityExecutorTest.java` — 1 处（line 310）
- **影响模块**：ai-impl test-compile（8 个编译错误），导致 ai-impl 全部测试未运行

## 修正方向
将全部 8 处 `StructuredOutputParser` 的 lambda 表达式替换为匿名内部类，使编译器能解析 `<T>`：

### 3 种具体修正模式：

**模式 A**（返回字符串字面量，6 处：AbstractCapabilityExecutorTest.java lines 597, 885, 934, 978, 1024, 1070）：
```java
// 原代码（lambda，编译失败）：
StructuredOutputParser mockParser = (rawContent, targetClass) -> "parsed";  // 或 "parsedFromChat"

// 修正为（匿名内部类）：
StructuredOutputParser mockParser = new StructuredOutputParser() {
    @Override
    @SuppressWarnings("unchecked")
    public <T> T parse(String rawContent, Class<T> targetClass) {
        return (T) "parsed";  // 或 "parsedFromChat"
    }
};
```

**模式 B**（抛异常，1 处：AbstractCapabilityExecutorTest.java line 641）：
```java
// 原代码：
StructuredOutputParser failingParser = (rawContent, targetClass) -> {
    throw new RuntimeException("parse error");
};

// 修正为：
StructuredOutputParser failingParser = new StructuredOutputParser() {
    @Override
    public <T> T parse(String rawContent, Class<T> targetClass) {
        throw new RuntimeException("parse error");
    }
};
```

**模式 C**（返回复杂类型，1 处：DiscussionConclusionCapabilityExecutorTest.java line 310）：
```java
// 原代码：
StructuredOutputParser mockParser = (rawContent, targetClass) -> new DiscussionConclusionResponse();

// 修正为：
StructuredOutputParser mockParser = new StructuredOutputParser() {
    @Override
    @SuppressWarnings("unchecked")
    public <T> T parse(String rawContent, Class<T> targetClass) {
        return (T) new DiscussionConclusionResponse();
    }
};
```

## 涉及文件（仅修改测试代码）
1. `modules/ai/ai-impl/src/test/java/.../orchestrator/AbstractCapabilityExecutorTest.java` — 7 处
2. `modules/ai/ai-impl/src/test/java/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` — 1 处

## 验证方式
```bash
mvn test -pl modules/ai/ai-impl -am -DskipTests=false 2>&1 | tail -20
```
预期：test-compile 通过，ai-impl 模块测试全部运行，无 FAILURE。
