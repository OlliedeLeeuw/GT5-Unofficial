package gregtech.common.tileentities.machines.multi.purification;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static gregtech.api.enums.GT_HatchElement.*;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_PROCESSING_ARRAY_GLOW;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.GregTech_API;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_StructureUtility;

public class GT_MetaTileEntity_PurificationUnitSifter
    extends GT_MetaTileEntity_PurificationUnitBase<GT_MetaTileEntity_PurificationUnitSifter> {

    private static final String STRUCTURE_PIECE_MAIN = "main";

    private int mCasingAmount;

    private static final IStructureDefinition<GT_MetaTileEntity_PurificationUnitSifter> STRUCTURE_DEFINITION = StructureDefinition
        .<GT_MetaTileEntity_PurificationUnitSifter>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            new String[][] { { "AAA", "A~A", "AAA" }, { "AAA", "A A", "AAA" }, { "AAA", "AAA", "AAA" } })
        .addElement(
            'A',
            ofChain(
                lazy(
                    t -> GT_StructureUtility.<GT_MetaTileEntity_PurificationUnitSifter>buildHatchAdder()
                        .atLeastList(t.getAllowedHatches())
                        .casingIndex(48)
                        .dot(1)
                        .build()),
                onElementPass(t -> t.mCasingAmount++, ofBlock(GregTech_API.sBlockCasings4, 0))))
        .build();

    public GT_MetaTileEntity_PurificationUnitSifter(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_PurificationUnitSifter(String aName) {
        super(aName);
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_PurificationUnitSifter> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        final GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType("Purification Unit")
            .addInfo(
                EnumChatFormatting.AQUA + ""
                    + EnumChatFormatting.BOLD
                    + "Water tier: "
                    + EnumChatFormatting.WHITE
                    + "1"
                    + EnumChatFormatting.RESET)
            .addSeparator()
            .beginStructureBlock(3, 3, 3, true)
            .addController("Front center")
            .toolTipFinisher("Gregtech");
        return tt;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, 1, 1, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_PurificationUnitSifter(this.mName);
    }

    private List<IHatchElement<? super GT_MetaTileEntity_PurificationUnitSifter>> getAllowedHatches() {
        return ImmutableList.of(InputBus);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {
        if (side == facing) {
            if (active) return new ITexture[] { Textures.BlockIcons.casingTexturePages[0][48], TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { Textures.BlockIcons.casingTexturePages[0][48], TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { Textures.BlockIcons.casingTexturePages[0][48] };
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        boolean result = checkPiece(STRUCTURE_PIECE_MAIN, 1, 1, 0);
        return result && super.checkMachine(aBaseMetaTileEntity, aStack);
    }
}