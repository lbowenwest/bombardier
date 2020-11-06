package tweakyllama.bombardier.bomb.item;

import net.minecraft.item.Item;
import tweakyllama.bombardier.base.handler.RegistryHandler;

public class BombItem extends Item {

    public BombItem() {
        this(new Properties().maxStackSize(16));
    }

    public BombItem(Properties properties) {
        super(properties);
        RegistryHandler.registerItem(this, "tweakyllama/bombardier/bomb");
    }
}
