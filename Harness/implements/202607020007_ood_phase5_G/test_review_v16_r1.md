# 测试审查报告（v16 r1）

## 审查结果
APPROVED

## 发现
- **[轻微]** `router/DefaultModelRouterTest.java:51` — `shouldSelectByWeight` 中断言 `countA > samples * 0.5`（>500），但 80:20 权重比下期望值约为 800。注释声称 "+/-15% tolerance"，实际阈值 50% 远低于 65% 下限。断言太宽松，虽不影响正确性，但削弱了概率验证的说服力。建议将阈值改为 `countA > 650` 以匹配注释意图。

- **[轻微]** `router/ModelRouteTest.java:60-69` — `shouldImplementEqualsAndHashCode` 仅验证更改 `endpointId` 一个字段会导致不等。equals 涉及 8 个字段，但只测试了单一字段差异。建议补充用例分别验证 `clientType`、`authType`、`modelId`、`endpointUrl`、`weight`、`timeoutMs`、`parameters` 各字段不等时 `equals` 返回 false。

## 修改要求
无（无严重或一般问题）
