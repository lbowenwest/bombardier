package tweakyllama.bombardier.fuse;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import tweakyllama.bombardier.Bombardier;
import tweakyllama.bombardier.base.config.Config;
import tweakyllama.bombardier.base.module.Module;
import tweakyllama.bombardier.fuse.block.FuseBlock;

public class Fuse extends Module {

    @Config(description = "Time in ticks that a fuse block burns for")
    public static int fuseBurnTime = 10;

    public static Block fuseBlock;

    @Override
    public void construct() {
        fuseBlock = new FuseBlock(AbstractBlock.Properties
                .create(Material.MISCELLANEOUS)
                .doesNotBlockMovement()
                .zeroHardnessAndResistance()
        );
    }

    @Override
    public void clientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(fuseBlock, RenderType.getCutout());
    }

    @Override
    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeClient()) {
            generator.addProvider(new BlockStateProvider(generator, Bombardier.MOD_ID, event.getExistingFileHelper()) {
                @Override
                protected void registerStatesAndModels() {
                    simpleBlock(fuseBlock);

                }
            });
        }
    }

    public static ActionResultType placeFuseBlock(ItemUseContext context) {
        return fuseBlock.asItem().onItemUse(context);
    }
}
