package tweakyllama.bombardier.bomb;

import net.minecraft.item.Item;
import tweakyllama.bombardier.base.module.Module;
import tweakyllama.bombardier.bomb.item.BombItem;

public class Bombs extends Module {

    public static Item bombItem;

    @Override
    public void construct() {
        bombItem = new BombItem();
    }
}
