# R3 出库 函数级 UML 图

本目录存放 R3（出库全流程）的函数级 UML 图，使用 PlantUML 描述。可用 IntelliJ IDEA 的 PlantUML 插件或 `plantuml` CLI 渲染为 PNG/SVG。

| 文件 | 内容 |
|---|---|
| `01-状态机.puml` | 出库单状态机（DRAFT→…→SHIPPED/CANCELLED） |
| `02-类图.puml` | 出库域类/函数图（Controller、Service、实体、外部库存 Feign） |
| `03-时序-分配库存.puml` | `OutboundService.allocate` 分配库存并锁定库存 |
| `04-时序-波次生成.puml` | `WaveService.generate` 波次生成纳入订单并锁定库存 |
| `05-时序-拣货确认.puml` | `PickService.confirmPick` 拣货回写与订单完成流转 |
| `06-时序-打包与发货.puml` | `PackageService.pack/ship` 复核打包与装车扣库存 |

> 与 `../R3-后端技术设计文档.md` 配套，图中类名/方法签名与代码实现保持一致。
