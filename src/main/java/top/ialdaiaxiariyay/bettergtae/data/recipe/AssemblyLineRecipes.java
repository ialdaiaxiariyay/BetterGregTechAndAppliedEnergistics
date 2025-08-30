package top.ialdaiaxiariyay.bettergtae.data.recipe;

import top.ialdaiaxiariyay.bettergtae.BetterGTAE;
import top.ialdaiaxiariyay.bettergtae.common.data.BGTAEBlocks;
import top.ialdaiaxiariyay.bettergtae.common.data.machine.BGTAEMachines;
import top.ialdaiaxiariyay.bettergtae.common.data.machine.BGTAEMultiblockA;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
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
                .inputItems(AEBlocks.INTERFACE.asItem(), 2)
                .inputItems(GTItems.CONVEYOR_MODULE_LuV)
                .inputItems(GTItems.ELECTRIC_PUMP_LuV)
                .inputItems(GTItems.SENSOR_UV, 2)
                .inputItems(AEItems.SPEED_CARD.asItem(), 4)
                .inputFluids(GTMaterials.SolderingAlloy, 1225)
                .outputItems(BGTAEMachines.ME_STOCKING_DUAL_INPUT_HATCH)
                .duration(20 * 30)
                .EUt(V[LuV], 16)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(BetterGTAE.id("1m_computing_core"))
                .inputItems(AEBlocks.CRAFTING_UNIT.asItem())
                .inputItems(AEItems.CELL_COMPONENT_256K.asItem(), 4)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 4)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.UraniumTriplatinum, 16)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.UraniumTriplatinum, 16)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.UraniumTriplatinum, 16)
                .inputFluids(GTMaterials.SolderingAlloy, 1356)
                .inputFluids(GTMaterials.Helium, 2647)
                .outputItems(BGTAEBlocks.COMPUTING_CORE_1M)
                .duration(20 * 10)
                .EUt(V[EV], 16)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(BetterGTAE.id("4m_computing_core"))
                .inputItems(AEBlocks.CRAFTING_UNIT.asItem())
                .inputItems(AEItems.CELL_COMPONENT_256K.asItem(), 16)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 16)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.SamariumIronArsenicOxide, 16)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.SamariumIronArsenicOxide, 16)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.SamariumIronArsenicOxide, 16)
                .inputFluids(GTMaterials.SolderingAlloy, 1356)
                .inputFluids(GTMaterials.Helium, 2647)
                .outputItems(BGTAEBlocks.COMPUTING_CORE_4M)
                .duration(20 * 10)
                .EUt(V[IV], 16)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(BetterGTAE.id("16m_computing_core"))
                .inputItems(AEBlocks.CRAFTING_UNIT.asItem())
                .inputItems(AEItems.CELL_COMPONENT_256K.asItem(), 64)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 64)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.IndiumTinBariumTitaniumCuprate, 16)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.IndiumTinBariumTitaniumCuprate, 16)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.IndiumTinBariumTitaniumCuprate, 16)
                .inputFluids(GTMaterials.SolderingAlloy, 1356)
                .inputFluids(GTMaterials.Helium, 2647)
                .outputItems(BGTAEBlocks.COMPUTING_CORE_16M)
                .duration(20 * 10)
                .EUt(V[LuV], 16)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(BetterGTAE.id("64m_computing_core"))
                .inputItems(AEBlocks.CRAFTING_UNIT.asItem())
                .inputItems(AEItems.CELL_COMPONENT_256K.asItem(), 64)
                .inputItems(AEItems.CELL_COMPONENT_256K.asItem(), 64)
                .inputItems(AEItems.CELL_COMPONENT_256K.asItem(), 64)
                .inputItems(AEItems.CELL_COMPONENT_256K.asItem(), 64)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 64)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.UraniumRhodiumDinaquadide, 48)
                .inputFluids(GTMaterials.SolderingAlloy, 1356)
                .inputFluids(GTMaterials.Helium, 2647)
                .outputItems(BGTAEBlocks.COMPUTING_CORE_64M)
                .duration(20 * 10)
                .EUt(V[LuV], 16)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(BetterGTAE.id("256m_computing_core"))
                .inputItems(AEBlocks.CRAFTING_UNIT.asItem())
                .inputItems(BGTAEBlocks.COMPUTING_CORE_64M, 64)
                .inputItems(BGTAEBlocks.COMPUTING_CORE_64M, 64)
                .inputItems(BGTAEBlocks.COMPUTING_CORE_64M, 64)
                .inputItems(BGTAEBlocks.COMPUTING_CORE_64M, 64)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 64)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.UraniumRhodiumDinaquadide, 48)
                .inputFluids(GTMaterials.SolderingAlloy, 1356)
                .inputFluids(GTMaterials.Helium, 2647)
                .outputItems(BGTAEBlocks.COMPUTING_CORE_256M)
                .duration(20 * 10)
                .EUt(V[ZPM], 16)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(BetterGTAE.id("max_computing_core"))
                .inputItems(AEBlocks.CRAFTING_UNIT.asItem())
                .inputItems(BGTAEBlocks.COMPUTING_CORE_256M, 64)
                .inputItems(CustomTags.IV_CIRCUITS, 16)
                .inputItems(CustomTags.LuV_CIRCUITS, 16)
                .inputItems(CustomTags.ZPM_CIRCUITS, 16)
                .inputItems(CustomTags.UV_CIRCUITS, 16)
                .inputItems(CustomTags.UHV_CIRCUITS, 16)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 48)
                .inputFluids(GTMaterials.SolderingAlloy, 1356)
                .inputFluids(GTMaterials.Helium, 2647)
                .outputItems(BGTAEBlocks.COMPUTING_CORE_MAX)
                .stationResearch(b -> b
                        .researchStack(BGTAEBlocks.COMPUTING_CORE_256M.asStack())
                        .dataStack(GTItems.TOOL_DATA_MODULE.asStack())
                        .CWUt(128)
                        .EUt(V[UV]))
                .duration(20 * 40)
                .EUt(V[UV], 16)
                .save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(BetterGTAE.id("advanced_assembly_line"))
                .inputItems(GTBlocks.CASING_ASSEMBLY_CONTROL, 4)
                .inputItems(GTBlocks.CASING_ASSEMBLY_LINE, 4)
                .inputItems(GTBlocks.CASING_GRATE, 4)
                .inputItems(GTItems.ROBOT_ARM_UV, 2)
                .inputItems(CustomTags.UV_CIRCUITS, 2)
                .inputFluids(GTMaterials.SolderingAlloy, 1356)
                .inputFluids(GTMaterials.SodiumPotassium, 1460)
                .inputFluids(GTMaterials.Polytetrafluoroethylene, 1266)
                .outputItems(BGTAEMultiblockA.ADVANCED_ASSEMBLY_LINE)
                .stationResearch(b -> b
                        .researchStack(GTMultiMachines.ASSEMBLY_LINE.asStack())
                        .dataStack(GTItems.TOOL_DATA_MODULE.asStack())
                        .CWUt(128)
                        .EUt(V[UV]))
                .duration(20 * 50)
                .EUt(V[UV], 16)
                .save(provider);
    }
}
