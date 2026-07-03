# 设计审查报告（v5 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `handle()` 方法对 `executorMap` 取出的 `CapabilityExecutor<?, ?>` 直接调用 `executor.execute(request, capabilityId)` 无法通过 Java 编译。由于通配符捕获（wildcard capture）规则，编译器将 `?` 视为未知具体类型 `CAP#1`，而 `Object` 类型参数无法赋值给 `CAP#1`，产生编译错误。设计必须明确解决方式，例如：(a) 使用原始类型 `CapabilityExecutor`（配合 `@SuppressWarnings("rawtypes")`），(b) 编写通配符捕获辅助方法，或 (c) 对 map 取值做显式类型转换。

## 修改要求（仅 REJECTED 时）

1. **[严重]** `AiOrchestrator.handle()` 中 `executor.execute(request, capabilityId)` 的调用方式——当前 `executorMap` 类型为 `Map<String, CapabilityExecutor<?, ?>>`，从中 `get()` 得到的 `CapabilityExecutor<?, ?>` 无法直接以 `Object` 参数调用 `execute(T, String)`。设计需选择并写明具体实现方案：原始类型回退、捕获辅助方法、或显式转型；同时更新对应的 `@SuppressWarnings` 注解规格。
