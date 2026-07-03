# 设计审查报告（v7 r1）

## 审查结果
REJECTED

## 发现

### **[严重] ChatToolDefinition.strict 默认值在 Jackson 反序列化时无法保持**

设计声明 `strict` 字段默认值为 `true`（detail_v7.md:211）。由于字段为 `final` 且使用无参构造器 + 字段反射注入的反序列化方式，当 JSON 输入不包含 `strict` 字段时，Jackson 不会通过反射设置该字段，Java 的 `boolean` 类型默认值为 `false`，导致反序列化后 `strict` 为 `false` 而非声明的 `true`。设计未就此边界场景提供任何处理方案。

**期望的修正方向**：方案一：将 `strict` 改为非 final 字段并初始化 `private boolean strict = true;`，Jackson 通过 Setter 或字段注入，缺失时保留初始值；方案二：无参构造器中显式初始化 `this.strict = true;`（需配合 Jackson 的 `@JsonProperty(required = false)`）；方案三：明确约定 `strict` 在序列化/反序列化中始终显式出现，无默认值。

### **[一般] @SuppressWarnings("unchecked") 标注位置不当**

设计在 `LlmChatService` 接口的 `structuredChat()` 方法声明上标注 `@SuppressWarnings("unchecked")`（detail_v7.md:239）。接口方法声明没有方法体，不会产生任何 unchecked 警告，该注解在此处无实际效果。"unchecked" 警告出现在实现类中进行泛型转型时，而非接口声明处。将该注解放在接口上不仅无效，而且会给后续实现者错误的指引。

**期望的修正方向**：移除接口方法上的 `@SuppressWarnings("unchecked")`，改为在实现该接口的类中按需添加；或在设计说明中注明"该注解应在实现类中添加，接口声明处无需标注"。

### **[一般] 无参构造器可见性与项目已有约定不一致**

设计对含 `final` 字段的类（`LlmChatMessage`、`LlmChatRequest`、`LlmChatResponse`、`LlmChatUsage`、`StructuredChatResult`、`ChatToolDefinition`）建议使用 `private` 或 `protected` 无参构造器（detail_v7.md:79,125,149,171,193,216）。项目现有所有 DTO 类（如 `TriageRequest`、`TriageResponse`、`RecommendedDoctor` 等）均使用 `public` 无参构造器。使用 `private`/`protected` 虽然技术上可行（Jackson 可调用非 public 构造器），但与项目约定不一致，可能给其他开发者带来困惑，且某些测试/构造框架可能无法访问非 public 构造器。

**期望的修正方向**：将无参构造器统一改为 `public`，与项目现有 DTO 风格保持一致。

### **[轻微] LlmChatUsage 内嵌静态类误标 `final` 修饰符**

设计将 `LlmChatUsage` 描述为 `public static final` 内嵌类（detail_v7.md:161）。类级 `final` 修饰符禁止继承，对 DTO 语义上无害，但项目现有代码无此用法，且 task 定义仅要求"内嵌静态类"。该修饰符不会影响 Jackson 序列化，但属于不必要的风格不一致。

**期望的修正方向**：移除 `final` 类修饰符，保持 `public static class` 形式，与项目无 `final` class 风格一致。

### **[轻微] LlmChatOptions 全参构造器参数顺序未与字段声明顺序对齐**

字段声明顺序（detail_v7.md:93-100）为 `modelId`、`temperature`、`maxTokens`、`stopSequences`、`topP`、`frequencyPenalty`、`presencePenalty`，全参构造器参数顺序（detail_v7.md:104）与之匹配。此处无缺陷，仅观察到字段列表与构造器参数顺序对齐良好，无需处理。
