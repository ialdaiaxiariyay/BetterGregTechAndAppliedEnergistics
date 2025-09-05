package top.ialdaiaxiariyay.bettergtae.common.data;

import top.ialdaiaxiariyay.bettergtae.common.item.InfinityCell;
import top.ialdaiaxiariyay.bettergtae.common.item.StructureWriteBehavior;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import appeng.api.stacks.AEKeyType;
import com.tterrag.registrate.util.entry.ItemEntry;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static top.ialdaiaxiariyay.bettergtae.api.registrate.BGTAERegistrate.REGISTRATE;

public class BGTAEItems {

    public static void init() {}

    static {
        REGISTRATE.creativeModeTab(() -> BGTAECreativeModeTabs.ITEM);
    }

    public static ItemEntry<ComponentItem> STRUCTURE_TOOLS = REGISTRATE.item("structure_tools", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(StructureWriteBehavior.INSTANCE))
            .register();

    public static final ItemEntry<InfinityCell> ITEM_INFINITY_CELL = REGISTRATE.item("item_infinity_cell", p -> new InfinityCell(AEKeyType.items())).register();
    public static final ItemEntry<InfinityCell> FLUID_INFINITY_CELL = REGISTRATE.item("fluid_infinity_cell", p -> new InfinityCell(AEKeyType.fluids())).register();
}
