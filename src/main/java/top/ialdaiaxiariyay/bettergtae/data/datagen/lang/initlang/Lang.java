package top.ialdaiaxiariyay.bettergtae.data.datagen.lang.initlang;

import top.ialdaiaxiariyay.bettergtae.data.datagen.lang.UnifiedLanguageProvider;

public class Lang {

    public static void init(UnifiedLanguageProvider provider) {
        provider.add("config.jade.plugin_bettergtae.extend_me_pattern_buffer", "[BetterGTAE] Extend Pattern Buffer Info", "扩展样板总成信息");
        provider.add("config.jade.plugin_bettergtae.extend_me_pattern_buffer_proxy", "[BetterGTAE] Extend Pattern Buffer Proxy Info", "扩展样板总成镜像信息");
        provider.add("itemGroup.bettergtae.machine", "BetterGTAE | Machine", "BetterGTAE | 机器");
        provider.add("itemGroup.bettergtae.block", "BetterGTAE | Block", "BetterGTAE | 方块");
        provider.add("itemGroup.bettergtae.item", "BetterGTAE | Item", "BetterGTAE | 物品");
    }
}
