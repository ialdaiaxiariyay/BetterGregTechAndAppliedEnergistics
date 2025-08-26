package top.ialdaiaxiariyay.bettergtae.data.recipe;

import top.ialdaiaxiariyay.bettergtae.BetterGTAE;
import top.ialdaiaxiariyay.bettergtae.common.data.machine.BGTAEMachines;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class AssemblyLineRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(BetterGTAE.id("extend_me_pattern_buffer"))
                .inputItems(GTAEMachines.ME_PATTERN_BUFFER)
                .inputItems(GTItems.EMITTER_UV, 8)
                .inputItems(CustomTags.UV_CIRCUITS, 4)
                .inputItems(AEBlocks.PATTERN_PROVIDER.asItem(), 8)
                .inputItems(AEBlocks.INTERFACE.asItem(), 8)
                .inputItems(AEItems.SPEED_CARD.asItem(), 64)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputFluids(GTMaterials.SolderingAlloy, 1225)
                .inputFluids(GTMaterials.Lubricant, 1000)
                .outputItems(BGTAEMachines.EXTEND_ME_PATTERN_BUFFER)
                .stationResearch(b -> b
                        .researchStack(GTAEMachines.ME_PATTERN_BUFFER.asStack())
                        .dataStack(GTItems.TOOL_DATA_MODULE.asStack())
                        .CWUt(128)
                        .EUt(V[UV]))
                .duration(20 * 30)
                .EUt(V[UV], 16)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(BetterGTAE.id("extend_me_pattern_buffer_proxy"))
                .inputItems(GTAEMachines.ME_PATTERN_BUFFER_PROXY)
                .inputItems(GTItems.SENSOR_UV, 8)
                .inputItems(CustomTags.UV_CIRCUITS, 4)
                .inputItems(AEBlocks.QUANTUM_LINK.asItem())
                .inputItems(AEBlocks.QUANTUM_RING.asItem(), 8)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputFluids(GTMaterials.SolderingAlloy, 1225)
                .inputFluids(GTMaterials.Lubricant, 1000)
                .outputItems(BGTAEMachines.EXTEND_ME_PATTERN_BUFFER_PROXY)
                .stationResearch(b -> b
                        .researchStack(GTAEMachines.ME_PATTERN_BUFFER_PROXY.asStack())
                        .dataStack(GTItems.TOOL_DATA_MODULE.asStack())
                        .CWUt(128)
                        .EUt(V[UV]))
                .duration(20 * 30)
                .EUt(V[UV], 16)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(BetterGTAE.id("me_stocking_dual_input_hatch"))
                .inputItems(GTAEMachines.STOCKING_IMPORT_BUS_ME)
                .inputItems(GTAEMachines.STOCKING_IMPORT_HATCH_ME)
                .inputItems(AEBlocks.INTERFACE.asItem(),2)
                .inputItems(GTItems.CONVEYOR_MODULE_LuV)
                .inputItems(GTItems.ELECTRIC_PUMP_LuV)
                .inputItems(GTItems.SENSOR_UV, 2)
                .inputItems(AEItems.SPEED_CARD.asItem(), 4)
                .inputFluids(GTMaterials.SolderingAlloy, 1225)
                .outputItems(BGTAEMachines.ME_STOCKING_DUAL_INPUT_HATCH)
                .duration(20 * 30)
                .EUt(V[LuV], 16)
                .save(provider);
    }
}
