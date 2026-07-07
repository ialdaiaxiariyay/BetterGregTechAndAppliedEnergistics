package top.ialdaiaxiariyay.bettergtae.data.datagen.lang.initlang;

import top.ialdaiaxiariyay.bettergtae.data.datagen.lang.UnifiedLanguageProvider;

public class TipsLang {

    public static void init(UnifiedLanguageProvider provider) {
        provider.add("bettergtae.gui.circuit.configurator.tooltip", "Configure circuit values for each Internal Slot",
                "配置每个内部槽位的电路值");
        provider.add("bettergtae.gui.circuit.slot_tooltip", "Slot %d: click to change circuit configuration",
                "槽位 %d：点击更改电路配置");
        provider.add("bettergtae.gui.circuit.current_value", "Current: %d", "当前值：%d");
        provider.add("bettergtae.extend_me_pattern_buffer.tips.0",
                "Increase the pattern slots to 81, with independent programming circuits for each slot that do not interfere with each other.",
                "增加样板槽位至81格，可单独设置每一个槽位的编程电路，并且互不影响");
        provider.add("bettergtae.machine.me_stocking_dual_input_hatch.tooltip.0",
                "Item and Fluid Input for Multiblocks", "为多方块结构输入物品和流体");
        provider.add("bettergtae.machine.me_stocking_dual_input_hatch.tooltip.1",
                "Retrieves items and fluids directly from the ME network", "直接从ME网络抽取物品和流体");
        provider.add("bettergtae.machine.me_stocking_dual_input_hatch.tooltip.2",
                "Keeps 16 item and fluid types in stock", "可标记16种物品和流体");
        provider.add("bettergtae.machine.me_stocking_dual_input_hatch.tooltip.3",
                "Auto-Pull from ME mode will automatically stock the first 16 items and fluids in the ME system, updated every 5 seconds.",
                "ME自动拉取模式将自动标记ME网络中的前16种物品和流体，每5秒更新一次。");
        provider.add("bettergtae.machine.me.dual_stocking.data_stick.name", "Me Stocking Dual Input Hatch Data Stick",
                "ME库存输入总成配置数据");
        provider.add("bettergtae.crafting_unit_tips.0",
                "Can be simultaneously used for the fabrication of memory and parallel processing units",
                "可同时用于合成成存储器与并行处理单元");
        provider.add("bettergtae.machine.advanced_assembly_line.0",
                "A more advanced assembly line that does not require sequential assembly", "更加先进的装配线，不需要顺序组装");
        provider.add("bettergtae.machine.advanced_assembly_line.1", "Supports the ME Pattern Buffer and Parallel Hatch",
                "支持样板总成与并行仓");
        provider.add("bettergtae.machine.large_molecular_assembler.0",
                "GT-Style Molecular Assembly Chamber: Larger, Better, More Powerful", "GT式的分子装配室，更大，更好，更强");
        provider.add("bettergtae.multiblock.pattern.error.tier", "Block Tier Error!", "方块等级错误！");
        provider.add("bettergtae.block.tier", "Block Tier: %s", "方块等级：%s");
    }
}
