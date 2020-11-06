package tweakyllama.bombardier.base.proxy;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tweakyllama.bombardier.base.handler.RegistryHandler;
import tweakyllama.bombardier.base.module.ModuleLoader;

public class CommonProxy {

    protected ModuleLoader loader = new ModuleLoader();

    public void start() {

        loader.start();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(RegistryHandler.class);
        registerListeners(bus);
    }

    public void registerListeners(IEventBus bus) {
        bus.addListener(this::setup);
        bus.addListener(this::loadComplete);
        bus.addListener(this::configChanged);
        bus.addListener(this::gatherData);
    }


    public void setup(FMLCommonSetupEvent event) {
        loader.setup(event);
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        loader.loadComplete(event);
    }

    public void gatherData(GatherDataEvent event) {
        loader.gatherData(event);
    }

    public void configChanged(ModConfigEvent event) {
        // TODO
    }
}
