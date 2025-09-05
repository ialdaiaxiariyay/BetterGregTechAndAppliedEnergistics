package top.ialdaiaxiariyay.bettergtae.common.item;

import top.ialdaiaxiariyay.bettergtae.BetterGTAE;
import top.ialdaiaxiariyay.bettergtae.data.StorageManager;
import top.ialdaiaxiariyay.bettergtae.utils.NumberUtil;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.core.AELog;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

public class InfinityCellInventory implements StorageCell {

    private final ISaveProvider container;
    private final AEKeyType keyType;
    @Getter
    private double storedItemCount;
    private Object2ObjectOpenHashMap<AEKey, BigInteger> storedMap;
    private final ItemStack stack;
    private boolean isPersisted = true;

    public InfinityCellInventory(AEKeyType keyType, ItemStack stack, ISaveProvider saveProvider) {
        this.stack = stack;
        this.container = saveProvider;
        this.keyType = keyType;
        this.storedMap = null;
        initData();
    }

    private InfinityCellDataStorage getDiskStorage() {
        if (getDiskUUID() != null)
            return getStorageInstance().getOrCreateDisk(getDiskUUID());
        else
            return InfinityCellDataStorage.EMPTY;
    }

    private void initData() {
        if (hasDiskUUID()) {
            this.storedItemCount = getDiskStorage().totalAmount;
        } else {
            this.storedItemCount = 0;
            getCellItems();
        }
    }

    @Override
    public CellState getStatus() {
        if (this.getStoredItemCount() == 0) {
            return CellState.EMPTY;
        }
        if (this.getFreeBytes() > 0) {
            return CellState.NOT_EMPTY;
        }
        return CellState.FULL;
    }

    @Override
    public double getIdleDrain() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void persist() {
        if (this.isPersisted) {
            return;
        }

        if (storedItemCount == 0) {
            if (hasDiskUUID()) {
                getStorageInstance().removeDisk(getDiskUUID());
                if (stack.getTag() != null) {
                    stack.getTag().remove("diskuuid");
                    stack.getTag().remove("count");
                }
                initData();
            }
            return;
        }
        ListTag keys = new ListTag();
        ListTag amount = new ListTag();
        BigInteger count = BigInteger.ZERO;

        for (var entry : storedMap.object2ObjectEntrySet()) {
            BigInteger a = entry.getValue();
            if (a.compareTo(BigInteger.ZERO) > 0) {
                count = count.add(a);
                keys.add(entry.getKey().toTagGeneric());
                amount.add(StringTag.valueOf(a.toString()));
            }
        }

        if (keys.isEmpty()) {
            getStorageInstance().updateDisk(getDiskUUID(), new InfinityCellDataStorage());
        } else {
            getStorageInstance().modifyDisk(getDiskUUID(), keys, amount, count.doubleValue());
        }

        this.storedItemCount = count.doubleValue();
        stack.getOrCreateTag().putDouble("count", this.storedItemCount);

        this.isPersisted = true;
    }

    @Override
    public Component getDescription() {
        return null;
    }

    public static InfinityCellInventory createInventory(ItemStack stack, ISaveProvider saveProvider) {
        Objects.requireNonNull(stack, "Cannot create cell inventory for null itemstack");

        if (!(stack.getItem() instanceof InfinityCell cellType)) {
            return null;
        }

        return new InfinityCellInventory(cellType.getKeyType(), stack, saveProvider);
    }

    public boolean hasDiskUUID() {
        return stack.hasTag() && stack.getOrCreateTag().contains("diskuuid");
    }

    public static boolean hasDiskUUID(ItemStack disk) {
        if (disk.getItem() instanceof InfinityCell) {
            return disk.hasTag() && disk.getOrCreateTag().contains("diskuuid");
        }
        return false;
    }

    public UUID getDiskUUID() {
        if (hasDiskUUID())
            return stack.getOrCreateTag().getUUID("diskuuid");
        else
            return null;
    }

    private boolean isStorageCell(AEItemKey key) {
        InfinityCell type = getStorageCell(key);
        return type != null;
    }

    private static InfinityCell getStorageCell(AEItemKey itemKey) {
        if (itemKey.getItem() instanceof InfinityCell infinityCell) {
            return infinityCell;
        }

        return null;
    }

    private static boolean isCellEmpty(InfinityCellInventory inv) {
        if (inv != null) {
            return inv.getAvailableStacks().isEmpty();
        }
        return true;
    }

    protected Object2ObjectOpenHashMap<AEKey, BigInteger> getCellItems() {
        if (this.storedMap == null) {
            this.storedMap = new Object2ObjectOpenHashMap<>();
            this.loadCellItems();
        }
        return this.storedMap;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var it = this.getCellItems().object2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            out.add(entry.getKey(), NumberUtil.getLongValue(entry.getValue()));
        }
    }

    private void loadCellItems() {
        boolean corruptedTag = false;

        if (!stack.hasTag()) {
            return;
        }

        ListTag amounts = getDiskStorage().amounts;
        ListTag stackKeys = getDiskStorage().stackKeys;
        if (amounts.size() != stackKeys.size()) {
            AELog.warn("Loading storage cell with mismatched amounts/tags: %d != %d", amounts.size(), stackKeys.size());
        }

        for (int i = 0; i < amounts.size(); i++) {
            String amount = amounts.getString(i);
            AEKey key = AEKey.fromTagGeneric(stackKeys.getCompound(i));
            if (amount.isEmpty() || key == null) corruptedTag = true;
            else storedMap.put(key, new BigInteger(amount));
        }

        if (corruptedTag) {
            this.saveChanges();
        }
    }

    private StorageManager getStorageInstance() {
        return BetterGTAE.STORAGE_INSTANCE;
    }

    protected void saveChanges() {
        this.storedItemCount = 0;
        for (var it = Object2ObjectMaps.fastIterator(this.storedMap); it.hasNext();) {
            BigInteger storedAmount = it.next().getValue();
            if (this.storedItemCount < 0) {
                this.storedItemCount = Double.MAX_VALUE;
                break;
            }
            this.storedItemCount += storedAmount.doubleValue();
        }
        this.isPersisted = false;
        if (this.container != null) {
            this.container.saveChanges();
        } else {
            this.persist();
        }
    }

    public double getRemainingItemCount() {
        return this.getFreeBytes() > 0 ? this.getFreeBytes() : 0;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount == 0 || !keyType.contains(what)) {
            return 0;
        }

        if (what instanceof AEItemKey itemKey && this.isStorageCell(itemKey)) {
            InfinityCellInventory meInventory = createInventory(itemKey.toStack(), null);
            if (!isCellEmpty(meInventory)) {
                return 0;
            }
        }

        if (!hasDiskUUID()) {
            stack.getOrCreateTag().putUUID("diskuuid", UUID.randomUUID());
            getStorageInstance().getOrCreateDisk(getDiskUUID());
            loadCellItems();
        }

        double remainingItemCount = getRemainingItemCount();
        if (amount > remainingItemCount) {
            amount = (long) remainingItemCount;
        }

        if (mode == Actionable.MODULATE) {
            BigInteger finalAmount = BigInteger.valueOf(amount);
            getCellItems().compute(what, (k, v) -> v == null ? finalAmount : v.add(finalAmount));
            this.saveChanges();
        }

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        BigInteger currentAmount = getCellItems().get(what);
        if (currentAmount == null) {
            return 0L;
        } else if (currentAmount.signum() > 0) {
            BigInteger extractAmount = BigInteger.valueOf(amount);
            if (currentAmount.compareTo(extractAmount) < 1) {
                if (mode == Actionable.MODULATE) {
                    this.storedMap.remove(what);
                    this.saveChanges();
                }
                return currentAmount.longValue();
            } else {
                if (mode == Actionable.MODULATE) {
                    this.storedMap.put(what, currentAmount.subtract(extractAmount));
                    this.saveChanges();
                }
                return amount;
            }
        } else {
            return 0L;
        }
    }

    public double getFreeBytes() {
        return Double.MAX_VALUE - this.getStoredItemCount();
    }

    public double getNbtItemCount() {
        if (hasDiskUUID()) {
            if (stack.getTag() != null) {
                return stack.getTag().getDouble("count");
            }
        }
        return 0;
    }
}
