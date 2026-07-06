package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.trait.ExtendProxySlotRecipeHandler;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExtendMEPatternBufferProxyPartMachine extends TieredIOPartMachine
                                                   implements IMuiMachine, IDataStickInteractable {

    @Getter
    private final ExtendProxySlotRecipeHandler proxySlotRecipeHandler;

    @SaveField
    @Getter
    @SyncToClient
    private @Nullable BlockPos bufferPos;

    private @Nullable ExtendMEPatternBufferPartMachine buffer = null;
    private boolean bufferResolved = false;

    public ExtendMEPatternBufferProxyPartMachine(BlockEntityCreationInfo info) {
        super(info, GTValues.LuV, IO.IN);
        proxySlotRecipeHandler = new ExtendProxySlotRecipeHandler(this,
                ExtendMEPatternBufferPartMachine.MAX_PATTERN_COUNT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) this.setBuffer(bufferPos);
    }

    @Override
    public List<RecipeHandlerList> getRecipeHandlers() {
        return proxySlotRecipeHandler.getProxySlotHandlers();
    }

    public void setBuffer(@Nullable BlockPos pos) {
        bufferResolved = true;
        var level = getLevel();
        if (level == null || pos == null) {
            buffer = null;
        } else if (MetaMachine.getMachine(level, pos) instanceof ExtendMEPatternBufferPartMachine machine) {
            bufferPos = pos;
            buffer = machine;
            machine.addProxy(this);
            if (!isRemote()) proxySlotRecipeHandler.updateProxy(machine);
        } else {
            buffer = null;
        }
        syncDataHolder.markClientSyncFieldDirty("bufferPos");
    }

    @Nullable
    public ExtendMEPatternBufferPartMachine getBuffer() {
        if (!bufferResolved) setBuffer(bufferPos);
        return buffer;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return getBuffer() != null;
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        assert getBuffer() != null;
        return getBuffer().getPanelBuilder(data, syncManager, settings);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        assert getBuffer() != null;
        getBuffer().buildMainUI(mainWidget, guiData, syncManager, settings);
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        var buf = getBuffer();
        if (buf != null) {
            buf.removeProxy(this);
            proxySlotRecipeHandler.clearProxy();
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        if (dataStick.hasTag()) {
            assert dataStick.getTag() != null;
            if (dataStick.getTag().contains("pos", Tag.TAG_INT_ARRAY)) {
                var posArray = dataStick.getOrCreateTag().getIntArray("pos");
                var bufferPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
                setBuffer(bufferPos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
