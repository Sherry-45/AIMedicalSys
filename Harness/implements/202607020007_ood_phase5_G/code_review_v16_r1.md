# 代码审查报告（v16 r1）

## 审查结果
**REJECTED**

## 发现

- **[一般]** `DefaultModelRouterTest.java:68-79` — `shouldBeThreadSafe` 方法中线程依次串行启动并立即 join（循环内 `t.start()` 后紧跟 `t.join()`），实际未产生任何并发访问，无法验证 `AtomicReference` 读路径在多线程同时调用时的线程安全性。期望：所有线程启动后再统一 join，构造真实的并发场景。

- **[轻微]** `DefaultModelRouterTest.java:56-65` — `shouldSelectRandomlyWhenAllWeightsZero` 使用 `List.of(routeZero, routeZero)` 传入同一对象引用两次，数组中两元素具有相同 `modelId=model-z`，导致无法区分选择的是哪一个元素，不能有效验证"等概率选择"行为。

## 修改要求

### [一般] DefaultModelRouterTest.shouldBeThreadSafe 并发性不足

**位置**：`DefaultModelRouterTest.java:68-79`

**问题**：当前实现中，50 个线程依次串行创建→启动→join，同一时间只有一个线程运行 `route()`，无法暴露 `AtomicReference` 在并发读写下的竞态条件。

**期望的修正方向**：改为先启动所有线程，再统一 join，示例如下：

```java
DefaultModelRouter router = createRouter(Map.of("CAP", List.of(routeA, routeB)));
Thread[] threads = new Thread[50];
for (int i = 0; i < threads.length; i++) {
    threads[i] = new Thread(() -> {
        for (int j = 0; j < 20; j++) {
            assertNotNull(router.route("CAP", null));
        }
    });
    threads[i].start();
}
for (Thread t : threads) {
    try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
}
```

### [轻微] DefaultModelRouterTest.shouldSelectRandomlyWhenAllWeightsZero 无法验证等概率

**位置**：`DefaultModelRouterTest.java:56-65`

**问题**：两个 route 持有相同 `modelId=model-z`，无论选择哪一个，断言均通过，无法判断是否实现了"等概率随机选择"。

**期望的修正方向**：使用两个不同的 route（不同 endpointId/modelId），统计 200 次以上选择中两个 route 均至少被选中若干次，验证选择不是固定指向单一路由。
