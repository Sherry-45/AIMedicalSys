# 测试报告（v8 r1）

## 测试文件

| 文件 | 路径 | 状态 |
|------|------|------|
| AiCallRecordTest | `ai-impl/src/test/java/.../metrics/AiCallRecordTest.java` | 已修正 |

## 修正内容

| 审查意见 | 修改措施 |
|---------|---------|
| `shouldConstructWithAllFields()` 中 `assertEquals("v1", record.getPromptVersion())` 与 promptVersion 类型 String→Integer 不兼容 | 旧版构造器参数 `"v1"` 改为 `"2"`（可解析整数字符串）；断言改为 `assertEquals(Integer.valueOf(2), record.getPromptVersion())` |

## 编译状态

- AiCallRecordTest.java: 编译通过
