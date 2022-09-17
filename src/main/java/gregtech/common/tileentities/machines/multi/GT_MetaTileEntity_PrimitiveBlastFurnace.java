package gregtech.common.tileentities.machines.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.objects.XSTR.XSTR_INSTANCE;

import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.GT_Mod;
import gregtech.api.GregTech_API;
import gregtech.api.enums.ParticleFX;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.objects.GT_ItemStack;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.WorldSpawnedEventBuilder;
import gregtech.api.util.WorldSpawnedEventBuilder.ParticleEventBuilder;
import gregtech.common.GT_Pollution;
import gregtech.common.gui.GT_Container_PrimitiveBlastFurnace;
import gregtech.common.gui.GT_GUIContainer_PrimitiveBlastFurnace;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class GT_MetaTileEntity_PrimitiveBlastFurnace extends MetaTileEntity
        implements IAlignment, ISurvivalConstructable {
    public static final int INPUT_SLOTS = 3, OUTPUT_SLOTS = 3;
    private static final ClassValue<IStructureDefinition<GT_MetaTileEntity_PrimitiveBlastFurnace>>
            STRUCTURE_DEFINITION = new ClassValue<IStructureDefinition<GT_MetaTileEntity_PrimitiveBlastFurnace>>() {
                @Override
                protected IStructureDefinition<GT_MetaTileEntity_PrimitiveBlastFurnace> computeValue(Class<?> type) {
                    return IStructureDefinition.<GT_MetaTileEntity_PrimitiveBlastFurnace>builder()
                            .addShape("main", transpose(new String[][] {
                                {"ccc", "c-c", "ccc"},
                                {"ccc", "clc", "ccc"},
                                {"c~c", "clc", "ccc"},
                                {"ccc", "ccc", "ccc"},
                            }))
                            .addElement('c', lazy(t -> ofBlock(t.getCasingBlock(), t.getCasingMetaID())))
                            .addElement(
                                    'l',
                                    ofChain(
                                            isAir(),
                                            ofBlockAnyMeta(Blocks.lava, 1),
                                            ofBlockAnyMeta(Blocks.flowing_lava, 1)))
                            .build();
                }
            };

    public int mMaxProgresstime = 0;
    private volatile boolean mUpdated;
    public int mUpdate = 5;
    public int mProgresstime = 0;
    public boolean mMachine = false;

    public ItemStack[] mOutputItems = new ItemStack[OUTPUT_SLOTS];

    @Deprecated
    public ItemStack mOutputItem1;

    @Deprecated
    public ItemStack mOutputItem2;

    public GT_MetaTileEntity_PrimitiveBlastFurnace(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, INPUT_SLOTS + OUTPUT_SLOTS);
    }

    public GT_MetaTileEntity_PrimitiveBlastFurnace(String aName) {
        super(aName, INPUT_SLOTS + OUTPUT_SLOTS);
    }

    @Override
    public boolean isSteampowered() {
        return false;
    }

    @Override
    public boolean isElectric() {
        return false;
    }

    @Override
    public boolean isPneumatic() {
        return false;
    }

    @Override
    public boolean isEnetInput() {
        return false;
    }

    @Override
    public boolean isEnetOutput() {
        return false;
    }

    @Override
    public boolean isInputFacing(byte aSide) {
        return false;
    }

    @Override
    public boolean isOutputFacing(byte aSide) {
        return false;
    }

    @Override
    public boolean isTeleporterCompatible() {
        return false;
    }

    @Override
    public boolean isFacingValid(byte aFacing) {
        return aFacing > 1;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {
        return true;
    }

    @Override
    public int getProgresstime() {
        return this.mProgresstime;
    }

    @Override
    public int maxProgresstime() {
        return this.mMaxProgresstime;
    }

    @Override
    public int increaseProgress(int aProgress) {
        this.mProgresstime += aProgress;
        return this.mMaxProgresstime - this.mProgresstime;
    }

    @Override
    public boolean allowCoverOnSide(byte aSide, GT_ItemStack aCoverID) {
        return (GregTech_API.getCoverBehaviorNew(aCoverID.toStack()).isSimpleCover())
                && (super.allowCoverOnSide(aSide, aCoverID));
    }

    @Override
    public abstract MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity);

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setInteger("mProgresstime", this.mProgresstime);
        aNBT.setInteger("mMaxProgresstime", this.mMaxProgresstime);
        if (this.mOutputItems != null) {
            for (int i = 0; i < mOutputItems.length; i++) {
                if (this.mOutputItems[i] != null) {
                    NBTTagCompound tNBT = new NBTTagCompound();
                    this.mOutputItems[i].writeToNBT(tNBT);
                    aNBT.setTag("mOutputItem" + i, tNBT);
                }
            }
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        this.mUpdate = 5;
        this.mProgresstime = aNBT.getInteger("mProgresstime");
        this.mMaxProgresstime = aNBT.getInteger("mMaxProgresstime");
        this.mOutputItems = new ItemStack[OUTPUT_SLOTS];
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            this.mOutputItems[i] = GT_Utility.loadItem(aNBT, "mOutputItem" + i);
        }
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aBaseMetaTileEntity.isClientSide()) {
            return true;
        }
        aBaseMetaTileEntity.openGUI(aPlayer);
        return true;
    }

    @Override
    public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_Container_PrimitiveBlastFurnace(aPlayerInventory, aBaseMetaTileEntity);
    }

    @Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_GUIContainer_PrimitiveBlastFurnace(
                aPlayerInventory,
                aBaseMetaTileEntity,
                getName(),
                GT_Recipe.GT_Recipe_Map.sPrimitiveBlastRecipes.mNEIName);
    }

    @Override
    public ExtendedFacing getExtendedFacing() {
        return ExtendedFacing.of(
                ForgeDirection.getOrientation(getBaseMetaTileEntity().getFrontFacing()));
    }

    @Override
    public void setExtendedFacing(ExtendedFacing alignment) {
        getBaseMetaTileEntity().setFrontFacing((byte) alignment.getDirection().ordinal());
    }

    @Override
    public IAlignmentLimits getAlignmentLimits() {
        return (d, r, f) -> d.offsetY == 0 && r.isNotRotated() && f.isNotFlipped();
    }

    private boolean checkMachine() {
        return STRUCTURE_DEFINITION
                .get(this.getClass())
                .check(
                        this,
                        "main",
                        getBaseMetaTileEntity().getWorld(),
                        getExtendedFacing(),
                        getBaseMetaTileEntity().getXCoord(),
                        getBaseMetaTileEntity().getYCoord(),
                        getBaseMetaTileEntity().getZCoord(),
                        1,
                        2,
                        0,
                        !mMachine);
    }

    protected abstract Block getCasingBlock();

    protected abstract int getCasingMetaID();

    @Override
    public void onMachineBlockUpdate() {
        mUpdated = true;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        final int lavaX = aBaseMetaTileEntity.getOffsetX(aBaseMetaTileEntity.getBackFacing(), 1);
        final int lavaZ = aBaseMetaTileEntity.getOffsetZ(aBaseMetaTileEntity.getBackFacing(), 1);
        if ((aBaseMetaTileEntity.isClientSide()) && (aBaseMetaTileEntity.isActive())) {

            new WorldSpawnedEventBuilder.ParticleEventBuilder()
                    .setMotion(0D, 0.3D, 0D)
                    .setIdentifier(ParticleFX.LARGE_SMOKE)
                    .setPosition(
                            lavaX + XSTR_INSTANCE.nextFloat(),
                            aBaseMetaTileEntity.getOffsetY(aBaseMetaTileEntity.getBackFacing(), 1),
                            lavaZ + XSTR_INSTANCE.nextFloat())
                    .setWorld(getBaseMetaTileEntity().getWorld())
                    .run();
        }
        if (aBaseMetaTileEntity.isServerSide()) {
            if (mUpdated) {
                // duct tape fix for too many updates on an overloaded server, causing the structure check to not run
                if (mUpdate < 0) mUpdate = 5;
                mUpdated = false;
            }
            if (this.mUpdate-- == 0) {
                this.mMachine = checkMachine();
            }
            if (this.mMachine) {
                if (this.mMaxProgresstime > 0) {
                    if (++this.mProgresstime >= this.mMaxProgresstime) {
                        addOutputProducts();
                        this.mOutputItems = null;
                        this.mProgresstime = 0;
                        this.mMaxProgresstime = 0;
                        GT_Mod.achievements.issueAchievement(
                                aBaseMetaTileEntity
                                        .getWorld()
                                        .getPlayerEntityByName(aBaseMetaTileEntity.getOwnerName()),
                                "steel");
                    }
                } else if (aBaseMetaTileEntity.isAllowedToWork()) {
                    checkRecipe();
                }
            }
            if (this.mMaxProgresstime > 0 && (aTimer % 20L == 0L)) {
                GT_Pollution.addPollution(
                        this.getBaseMetaTileEntity(), GT_Mod.gregtechproxy.mPollutionPrimitveBlastFurnacePerSecond);
            }

            aBaseMetaTileEntity.setActive((this.mMaxProgresstime > 0) && (this.mMachine));
            final short lavaY = aBaseMetaTileEntity.getYCoord();
            if (aBaseMetaTileEntity.isActive()) {
                if (aBaseMetaTileEntity.getAir(lavaX, lavaY, lavaZ)) {
                    aBaseMetaTileEntity.getWorld().setBlock(lavaX, lavaY, lavaZ, Blocks.lava, 1, 2);
                    this.mUpdate = 1;
                }
                if (aBaseMetaTileEntity.getAir(lavaX, lavaY + 1, lavaZ)) {
                    aBaseMetaTileEntity.getWorld().setBlock(lavaX, lavaY + 1, lavaZ, Blocks.lava, 1, 2);
                    this.mUpdate = 1;
                }
            } else {
                Block lowerLava = aBaseMetaTileEntity.getBlock(lavaX, lavaY, lavaZ);
                Block upperLava = aBaseMetaTileEntity.getBlock(lavaX, lavaY + 1, lavaZ);
                if (lowerLava == Blocks.lava) {
                    aBaseMetaTileEntity.getWorld().setBlock(lavaX, lavaY, lavaZ, Blocks.air, 0, 2);
                    this.mUpdate = 1;
                }
                if (upperLava == Blocks.lava) {
                    aBaseMetaTileEntity.getWorld().setBlock(lavaX, lavaY + 1, lavaZ, Blocks.air, 0, 2);
                    this.mUpdate = 1;
                }
            }
        }
    }

    /**
     * Draws random flames and smoke particles in front of Primitive Blast
     * Furnace when active
     *
     * @param aBaseMetaTileEntity The entity that will handle the
     *                            {@link Block#randomDisplayTick}
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void onRandomDisplayTick(IGregTechTileEntity aBaseMetaTileEntity) {
        if (aBaseMetaTileEntity.isActive()) {

            final byte frontFacing = aBaseMetaTileEntity.getFrontFacing();

            final double oX = aBaseMetaTileEntity.getOffsetX(frontFacing, 1) + 0.5D;
            final double oY = aBaseMetaTileEntity.getOffsetY(frontFacing, 1);
            final double oZ = aBaseMetaTileEntity.getOffsetZ(frontFacing, 1) + 0.5D;
            final double offset = -0.48D;
            final double horizontal = XSTR_INSTANCE.nextFloat() * 8D / 16D - 4D / 16D;

            final double x, y, z;

            y = oY + XSTR_INSTANCE.nextFloat() * 10D / 16D + 5D / 16D;

            if (frontFacing == ForgeDirection.WEST.ordinal()) {
                x = oX - offset;
                z = oZ + horizontal;
            } else if (frontFacing == ForgeDirection.EAST.ordinal()) {
                x = oX + offset;
                z = oZ + horizontal;
            } else if (frontFacing == ForgeDirection.NORTH.ordinal()) {
                x = oX + horizontal;
                z = oZ - offset;
            } else // if (frontFacing == ForgeDirection.SOUTH.ordinal())
            {
                x = oX + horizontal;
                z = oZ + offset;
            }

            ParticleEventBuilder particleEventBuilder = (new ParticleEventBuilder())
                    .setMotion(0D, 0D, 0D)
                    .setPosition(x, y, z)
                    .setWorld(getBaseMetaTileEntity().getWorld());
            particleEventBuilder.setIdentifier(ParticleFX.SMOKE).run();
            particleEventBuilder.setIdentifier(ParticleFX.FLAME).run();
        }
    }

    @Override
    public Class<?> getType() {
        return GT_MetaTileEntity_Cleanroom.class;
    }

    private void addOutputProducts() {
        if (this.mOutputItems == null) {
            return;
        }
        int limit = Math.min(mOutputItems.length, OUTPUT_SLOTS);
        for (int i = 0; i < limit; i++) {
            int absi = INPUT_SLOTS + i;
            if (this.mInventory[absi] == null) {
                this.mInventory[absi] = GT_Utility.copyOrNull(this.mOutputItems[i]);
            } else if (GT_Utility.areStacksEqual(this.mInventory[absi], this.mOutputItems[i])) {
                this.mInventory[absi].stackSize = Math.min(
                        this.mInventory[absi].getMaxStackSize(),
                        this.mInventory[absi].stackSize + this.mOutputItems[i].stackSize);
            }
        }
    }

    private boolean spaceForOutput(ItemStack outputStack, int relativeOutputSlot) {
        int absoluteSlot = relativeOutputSlot + INPUT_SLOTS;
        if (this.mInventory[absoluteSlot] == null || outputStack == null) {
            return true;
        }
        return ((this.mInventory[absoluteSlot].stackSize + outputStack.stackSize
                        <= this.mInventory[absoluteSlot].getMaxStackSize())
                && (GT_Utility.areStacksEqual(this.mInventory[absoluteSlot], outputStack)));
    }

    private boolean checkRecipe() {
        if (!this.mMachine) {
            return false;
        }
        ItemStack[] inputs = new ItemStack[INPUT_SLOTS];
        System.arraycopy(mInventory, 0, inputs, 0, INPUT_SLOTS);
        GT_Recipe recipe = GT_Recipe.GT_Recipe_Map.sPrimitiveBlastRecipes.findRecipe(
                getBaseMetaTileEntity(), false, 0, null, inputs);
        if (recipe == null) {
            this.mOutputItems = null;
            return false;
        }
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            if (!spaceForOutput(recipe.getOutput(i), i)) {
                this.mOutputItems = null;
                return false;
            }
        }

        if (!recipe.isRecipeInputEqual(true, null, inputs)) {
            this.mOutputItems = null;
            return false;
        }
        for (int i = 0; i < INPUT_SLOTS; i++) {
            if (mInventory[i] != null && mInventory[i].stackSize == 0) {
                mInventory[i] = null;
            }
        }

        this.mMaxProgresstime = recipe.mDuration;
        this.mOutputItems = recipe.mOutputs;
        return true;
    }

    @Override
    public boolean isGivingInformation() {
        return false;
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
        return aIndex > INPUT_SLOTS;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
        return !GT_Utility.areStacksEqual(aStack, this.mInventory[0]);
    }

    @Override
    public byte getTileEntityBaseType() {
        return 0;
    }

    public abstract String getName();

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return STRUCTURE_DEFINITION
                .get(getClass())
                .survivalBuild(
                        this,
                        stackSize,
                        "main",
                        getBaseMetaTileEntity().getWorld(),
                        getExtendedFacing(),
                        getBaseMetaTileEntity().getXCoord(),
                        getBaseMetaTileEntity().getYCoord(),
                        getBaseMetaTileEntity().getZCoord(),
                        1,
                        2,
                        0,
                        elementBudget,
                        env,
                        false);
    }

    @Override
    public IStructureDefinition<?> getStructureDefinition() {
        return STRUCTURE_DEFINITION.get(getClass());
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        STRUCTURE_DEFINITION
                .get(getClass())
                .buildOrHints(
                        this,
                        stackSize,
                        "main",
                        getBaseMetaTileEntity().getWorld(),
                        getExtendedFacing(),
                        getBaseMetaTileEntity().getXCoord(),
                        getBaseMetaTileEntity().getYCoord(),
                        getBaseMetaTileEntity().getZCoord(),
                        1,
                        2,
                        0,
                        hintsOnly);
    }
}
