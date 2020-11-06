package tweakyllama.bombardier.base.module;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public abstract class Module {
    public boolean enabled = true;
    public boolean enabledByDefault = true;
    public String description = "";


    public String getName() {
        return this.getClass().getSimpleName();
    }

    public void construct() {

    }

    @OnlyIn(Dist.CLIENT)
    public void constructClient() {

    }

    public void setup(FMLCommonSetupEvent event) {

    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {

    }

    public void loadComplete(FMLLoadCompleteEvent event) {

    }

    public void gatherData(GatherDataEvent event) {

    }

    public void configChanged(ModConfig.ModConfigEvent event) {

    }

    @OnlyIn(Dist.CLIENT)
    public void clientConfigChanged(ModConfig.ModConfigEvent event) {

    }

    @OnlyIn(Dist.CLIENT)
    public void modelRegistry(ModelRegistryEvent event) {

    }

    @OnlyIn(Dist.CLIENT)
    public void textureStitch(TextureStitchEvent.Pre event) {

    }

    @OnlyIn(Dist.CLIENT)
    public void postTextureStitch(TextureStitchEvent.Post event) {

    }

}
