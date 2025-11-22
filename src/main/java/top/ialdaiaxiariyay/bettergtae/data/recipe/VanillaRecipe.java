package top.ialdaiaxiariyay.bettergtae.data.recipe;

import top.ialdaiaxiariyay.bettergtae.BetterGTAE;
import top.ialdaiaxiariyay.bettergtae.common.data.machine.BGTAEMachines;
import top.ialdaiaxiariyay.bettergtae.common.data.machine.BGTAEMultiblockA;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.block.Blocks;

import appeng.core.definitions.AEBlocks;

import java.util.function.Consumer;

public class VanillaRecipe {

    public static void init(Consumer<FinishedRecipe> consumer) {
        VanillaRecipeHelper.addShapedRecipe(consumer, true, BetterGTAE.id("cleanroom_sterile_maintenance_hatch"), BGTAEMachines.STERILE_CLEANING_MAINTENANCE_HATCH.asStack(),
                "ABA",
                "CDC",
                "EAE",
                'A', CustomTags.UHV_CIRCUITS,
                'B', GTMachines.CLEANING_MAINTENANCE_HATCH.asStack(),
                'C', GTItems.ROBOT_ARM_UV.asStack(),
                'D', GTBlocks.FILTER_CASING_STERILE.asStack(),
                'E', ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Europium));

        VanillaRecipeHelper.addShapedRecipe(consumer, true, BetterGTAE.id("large_molecular_assembler"), BGTAEMultiblockA.LARGE_MOLECULAR_ASSEMBLER.asStack(),
                "ABA",
                "CDC",
                "ABA",
                'A', GTBlocks.CASING_ASSEMBLY_LINE.asStack(),
                'B', GTItems.ROBOT_ARM_IV,
                'C', CustomTags.IV_CIRCUITS,
                'D', Blocks.CRAFTING_TABLE.asItem());

        VanillaRecipeHelper.addShapedRecipe(consumer, true, BetterGTAE.id("crafting_pattern_hatch"), BGTAEMachines.CRAFTING_PATTERN_HATCH.asStack(),
                "ABA",
                "BCB",
                "ABA",
                'A', AEBlocks.PATTERN_PROVIDER.asItem(),
                'B', CustomTags.IV_CIRCUITS,
                'C', GTMachines.HULL[GTValues.IV].asStack());
    }
}
