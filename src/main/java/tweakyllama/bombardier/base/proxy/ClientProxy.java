package tweakyllama.bombardier.base.proxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void start() {
        super.start();
        loader.clientStart();
    }

    @Override
    public void registerListeners(IEventBus bus) {
        super.registerListeners(bus);

        bus.addListener(this::clientSetup);
        bus.addListener(this::modelRegistry);
        bus.addListener(this::textureStitch);
        bus.addListener(this::postTextureStitch);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        loader.clientSetup(event);
    }

    public void modelRegistry(ModelRegistryEvent event) {
        loader.modelRegistry(event);
    }

    public void textureStitch(TextureStitchEvent.Pre event) {
        loader.textureStitch(event);
    }

    public void postTextureStitch(TextureStitchEvent.Post event) {
        loader.postTextureStitch(event);
    }
}
