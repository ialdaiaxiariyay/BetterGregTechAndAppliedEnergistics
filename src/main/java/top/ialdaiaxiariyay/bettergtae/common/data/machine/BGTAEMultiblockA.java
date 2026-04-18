package top.ialdaiaxiariyay.bettergtae.common.data.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;

import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.electric.LargeMolecularAssembler;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.dataHatchPredicate;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_GRATE;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;
import static top.ialdaiaxiariyay.bettergtae.api.registrate.BGTAERegistrate.REGISTRATE;

public class BGTAEMultiblockA {

    public static void init() {}

    public static final MultiblockMachineDefinition ADVANCED_ASSEMBLY_LINE = REGISTRATE
            .multiblock("advanced_assembly_line", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.ASSEMBLY_LINE_RECIPES)
            .alwaysTryModifyRecipe(true)
            .recipeModifiers(OC_PERFECT_SUBTICK, PARALLEL_HATCH)
            .appearanceBlock(CASING_STEEL_SOLID)
            .tooltips(Component.translatable("bettergtae.machine.advanced_assembly_line.0"))
            .tooltips(Component.translatable("bettergtae.machine.advanced_assembly_line.1"))
            .pattern(definition -> FactoryBlockPattern.start(BACK, UP, RIGHT)
                    .aisle("       ", " AAAAAA", " BCCCCC", " AAAAAA", "       ")
                    .aisle(" AAAAAA", "BADDDDD", "BACCCCC", "BABBBBB", " AAAAAA")
                    .aisle(" BGEEEE", "BAAAAAA", "~FFFFFF", "BAAAAAA", " BGEEEE")
                    .aisle(" AAAAAA", "BABBBBB", "BACCCCC", "BADDDDD", " AAAAAA")
                    .aisle("       ", " AAAAAA", " BCCCCC", " AAAAAA", "       ")
                    .where("~", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("A", Predicates.blocks(CASING_STEEL_SOLID.get()))
                    .where("B", Predicates.blocks(CASING_GRATE.get()))
                    .where("D", Predicates.blocks(CASING_ASSEMBLY_CONTROL.get()))
                    .where("C", Predicates.blocks(CASING_LAMINATED_GLASS.get()))
                    .where("E", Predicates.blocks(CASING_STEEL_SOLID.get())
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH)))
                    .where("F", Predicates.blocks(CASING_ASSEMBLY_LINE.get()))
                    .where('G', dataHatchPredicate(blocks(CASING_STEEL_SOLID.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/assembly_line"))
            .register();

    public static final MultiblockMachineDefinition LARGE_MOLECULAR_ASSEMBLER = REGISTRATE
            .multiblock("large_molecular_assembler", LargeMolecularAssembler::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .alwaysTryModifyRecipe(true)
            .recipeModifiers(OC_NON_PERFECT, PARALLEL_HATCH)
            .appearanceBlock(CASING_ASSEMBLY_CONTROL)
            .tooltips(Component.translatable("bettergtae.machine.large_molecular_assembler.0"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("               ", "               ", "               ", "       A       ",
                            "      BBB      ", "   AAABBBAAA   ", "      BBB      ", "       A       ",
                            "               ", "               ", "               ")
                    .aisle("               ", "               ", "               ", "               ",
                            "       A       ", "  A   AAA   A  ", "       A       ", "               ",
                            "               ", "               ", "               ")
                    .aisle("               ", "               ", "               ", "               ",
                            "               ", " A     A     A ", "               ", "               ",
                            "               ", "               ", "               ")
                    .aisle("               ", "               ", "               ", "     CCDCC     ",
                            "     CCDCC     ", "A    DDDDD    A", "     CCDCC     ", "     CCDCC     ",
                            "               ", "               ", "               ")
                    .aisle("               ", "               ", "     CCDCC     ", "    C     C    ",
                            "    C     C    ", "A   D  E  D   A", "    C     C    ", "    C     C    ",
                            "     CCDCC     ", "               ", "               ")
                    .aisle("               ", "     CCDCC     ", "    C     C    ", "   C       C   ",
                            "   C   E   C   ", "A  D  EEE  D  A", "   C   E   C   ", "   C       C   ",
                            "    C     C    ", "     CCDCC     ", "               ")
                    .aisle("      DDD      ", "     CDDDC     ", "    C     C    ", "   C   E   C   ",
                            "B  C  EEE  C  B", "BA D EEEEE D AB", "B  C  EEE  C  B", "   C   E   C   ",
                            "    C     C    ", "     CDDDC     ", "      DDD      ")
                    .aisle("      DDD      ", "     DDDDD     ", "    D  E  D    ", "A  D  EEE  D  A",
                            "BA D EEEEE D AB", "BAADEEEEEEEDAAB", "BA D EEEEE D AB", "A  D  EEE  D  A",
                            "    D  E  D    ", "     DDDDD     ", "      DDD      ")
                    .aisle("      DDD      ", "     CDDDC     ", "    C     C    ", "   C   E   C   ",
                            "B  C  EEE  C  B", "BA D EEEEE D AB", "B  C  EEE  C  B", "   C   E   C   ",
                            "    C     C    ", "     CDDDC     ", "      DDD      ")
                    .aisle("               ", "     CCDCC     ", "    C     C    ", "   C       C   ",
                            "   C   E   C   ", "A  D  EEE  D  A", "   C   E   C   ", "   C       C   ",
                            "    C     C    ", "     CCDCC     ", "               ")
                    .aisle("               ", "               ", "     CCDCC     ", "    C     C    ",
                            "    C     C    ", "A   D  E  D   A", "    C     C    ", "    C     C    ",
                            "     CCDCC     ", "               ", "               ")
                    .aisle("               ", "               ", "               ", "     CCDCC     ",
                            "     CCDCC     ", "A    DDDDD    A", "     CCDCC     ", "     CCDCC     ",
                            "               ", "               ", "               ")
                    .aisle("               ", "               ", "               ", "               ",
                            "               ", " A     A     A ", "               ", "               ",
                            "               ", "               ", "               ")
                    .aisle("               ", "               ", "               ", "               ",
                            "       A       ", "  A   AAA   A  ", "       A       ", "               ",
                            "               ", "               ", "               ")
                    .aisle("               ", "               ", "               ", "       A       ",
                            "      BBB      ", "   AAAB~BAAA   ", "      BBB      ", "       A       ",
                            "               ", "               ", "               ")
                    .where("~", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("C", Predicates.blocks(CASING_LAMINATED_GLASS.get()))
                    .where("D", Predicates.blocks(CASING_GRATE.get()))
                    .where("E", Predicates.blocks(CASING_ASSEMBLY_LINE.get()))
                    .where("A", Predicates.blocks(CASING_ASSEMBLY_CONTROL.get()))
                    .where("B", Predicates.blocks(CASING_ASSEMBLY_CONTROL.get())
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.blocks(BGTAEMachines.CRAFTING_PATTERN_HATCH.get()))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/mechanic/machine_casing_assembly_control"),
                    GTCEu.id("block/multiblock/assembly_line"))
            .register();
}
