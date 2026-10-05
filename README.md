# GTNH-Dumper
 Dump data for GTNH wiki

 Special thanks : https://github.com/GTNewHorizons/ExampleMod1.7.10

## 按 Mod 导出

在游戏内打开 **NEI 选项 → 工具 → 数据导出 → GTNH Dumper**，在 **Mod 筛选 / Mod filter**
输入已加载 Mod 的 ID（如 `gregtech`）或完整显示名称，然后点击对应导出按钮。
不区分大小写，忽略首尾空格；清空输入即可恢复全量导出。设置使用 NEI 的全局/世界配置并自动保存。
未知或重名的 Mod 会提示错误；重名时请使用 ID。

筛选结果保存到 `dumps/filtered/<modid>/`，全量导出仍使用 `dumps/`。各个 Mod 的结果互不混合。

- 物品、图标和矿辞成员：按物品注册所属 Mod 筛选，保留所有 metadata/NBT 变体。
- 流体：按 Forge 记录的流体注册 Mod 筛选，包括没有对应方块的流体。
- GT 材料：按导出数据中的材料来源字段筛选；GT 结构按控制器物品的注册 Mod 筛选。
  附属 Mod 共用 `gregtech` 注册物品时，这些物品和结构仍归 `gregtech`。
- 配方：按 **原始配方来源** 筛选，目前使用 `GTRecipe.owners` 的第一条记录。
  后续修改者、配方产物、机器和 NEI 分类的 Mod 都不作为原始来源。
  匹配后保留完整配方，包括其他 Mod 的原料和产物，并导出 `sourceMod` 便于核对。

GT 配方需要在加载配方前开启 `config/GregTech/Client.cfg` 的 `NEIRecipeOwner=true`，修改后重启游戏。
运行时打开该选项无法补回此前丢失的来源记录。缺少来源的 GT 配方、普通 NEI 配方以及 EOH/EEC
等未保存来源的自定义配方不会被猜测归属；筛选时跳过并在 `recipes_filter_report.json` 中列出原因，
游戏内也会显示提示。清空筛选仍可全量导出这些配方。

研究、任务、任务线、TiC 材料、矿脉及维度等没有可靠逐项 Mod 归属的数据，在开启筛选时会提示并停止该项导出。
