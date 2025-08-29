package top.ialdaiaxiariyay.bettergtae.data.datagen.lang;

import net.minecraft.world.item.Item;
import top.ialdaiaxiariyay.bettergtae.BetterGTAE;
import top.ialdaiaxiariyay.bettergtae.data.datagen.lang.initlang.BlockLang;
import top.ialdaiaxiariyay.bettergtae.data.datagen.lang.initlang.ItemLang;
import top.ialdaiaxiariyay.bettergtae.data.datagen.lang.initlang.Lang;
import top.ialdaiaxiariyay.bettergtae.data.datagen.lang.initlang.TipsLang;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.HashMap;
import java.util.Map;

public class UnifiedLanguageProvider extends LanguageProvider {

    private final Map<String, Map<String, String>> translations = new HashMap<>();

    private final String locale;

    public UnifiedLanguageProvider(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), BetterGTAE.MOD_ID, locale);
        this.locale = locale;
        initializeTranslations();
    }

    @Override
    protected void addTranslations() {
        translations.forEach((key, langMap) -> {
            String translation = langMap.get(locale);
            if (translation != null) {
                add(key, translation);
            }
        });
    }

    private void initializeTranslations() {
        BlockLang.init(this);
        ItemLang.init(this);
        TipsLang.init(this);
        Lang.init(this);
    }

    public void add(String key, String en, String zh) {
        Map<String, String> langMap = new HashMap<>();
        langMap.put("en_us", en);
        langMap.put("zh_cn", zh);
        translations.put(key, langMap);
    }
}
