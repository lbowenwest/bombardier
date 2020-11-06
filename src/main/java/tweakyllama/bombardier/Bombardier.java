package tweakyllama.bombardier;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tweakyllama.bombardier.base.proxy.ClientProxy;
import tweakyllama.bombardier.base.proxy.CommonProxy;

@Mod(Bombardier.MOD_ID)
public class Bombardier {

    public static final String MOD_ID = "bombardier";
    public static final Logger LOGGER = LogManager.getLogger();

    public static CommonProxy proxy;

    public Bombardier() {
        proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        proxy.start();
    }

}
