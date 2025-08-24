package top.ialdaiaxiariyay.bettergtae.data.datagen.lang.initlang;

import top.ialdaiaxiariyay.bettergtae.data.datagen.lang.UnifiedLanguageProvider;

public class TipsLang {

    public static void init(UnifiedLanguageProvider provider) {
        provider.add("bettergtae.gui.circuit.middle_click_tooltip", "Left-click to set and right-click to clear", "左键设置，右键清除");
        provider.add("bettergtae.gui.circuit_configurator_slot.title", "Set the programming circuit for each slot", "设置每个槽位的编程电路");
        provider.add("bettergtae.extend_me_pattern_buffer.tips.0", "Increase the pattern slots to 81, with independent programming circuits for each slot that do not interfere with each other.", "增加样板槽位至81格，可单独设置每一个槽位的编程电路，并且互不影响");
    }
}
