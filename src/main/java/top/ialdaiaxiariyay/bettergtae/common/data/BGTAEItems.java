package top.ialdaiaxiariyay.bettergtae.common.data;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import top.ialdaiaxiariyay.bettergtae.common.item.StructureWriteBehavior;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static top.ialdaiaxiariyay.bettergtae.api.registrate.BGTAERegistrate.REGISTRATE;


public class BGTAEItems {

    public static void init(){}

    static {
        REGISTRATE.creativeModeTab(() -> BGTAECreativeModeTabs.ITEM);
    }

    public static ItemEntry<ComponentItem> STRUCTURE_TOOLS = REGISTRATE.item("structure_tools",ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(StructureWriteBehavior.INSTANCE))
            .register();
}
