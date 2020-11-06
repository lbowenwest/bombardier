package tweakyllama.bombardier.base.module;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import tweakyllama.bombardier.base.config.ConfigBuilder;
import tweakyllama.bombardier.bomb.Bombs;
import tweakyllama.bombardier.fuse.Fuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class ModuleLoader {

    private final Map<Class<? extends Module>, Module> foundModules = new HashMap<>();

    public void start() {
        findModules();
        dispatch(Module::construct);
        buildConfig();
    }

    private void buildConfig() {
        ConfigBuilder builder = new ConfigBuilder(new ArrayList<>(foundModules.values()));
        builder.makeSpec();
        builder.refreshConfig();
    }

    @OnlyIn(Dist.CLIENT)
    public void clientStart() {
        dispatch(Module::constructClient);
    }

    private void findModules() {
        // hardcode for now
        foundModules.put(Bombs.class, new Bombs());
        foundModules.put(Fuse.class, new Fuse());
    }


    public void setup(FMLCommonSetupEvent event) {
        dispatch(module -> module.setup(event));

    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        dispatch(module -> module.clientSetup(event));
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        dispatch(module -> module.loadComplete(event));
    }

    public void gatherData(GatherDataEvent event) {
        dispatch(module -> module.gatherData(event));
    }

    @OnlyIn(Dist.CLIENT)
    public void modelRegistry(ModelRegistryEvent event) {
        dispatch(module -> module.modelRegistry(event));
    }

    @OnlyIn(Dist.CLIENT)
    public void textureStitch(TextureStitchEvent.Pre event) {
        dispatch(module -> module.textureStitch(event));
    }

    @OnlyIn(Dist.CLIENT)
    public void postTextureStitch(TextureStitchEvent.Post event) {
        dispatch(module -> module.postTextureStitch(event));
    }

    private void dispatch(Consumer<Module> run) {
        foundModules.values().forEach(run);
    }


    public boolean isModuleEnabled(Class<? extends Module> moduleClass) {
        Module module = getModuleInstance(moduleClass);
        return module != null && module.enabled;
    }

    public Module getModuleInstance(Class<? extends Module> moduleClass) {
        return foundModules.get(moduleClass);
    }

}
