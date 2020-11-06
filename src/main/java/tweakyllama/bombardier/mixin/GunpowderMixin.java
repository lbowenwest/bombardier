package tweakyllama.bombardier.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tweakyllama.bombardier.fuse.Fuse;

@Mixin(Item.class)
public class GunpowderMixin {

    @Inject(method = "onItemUse", at = @At("HEAD"), cancellable = true)
    public void onUseHook(ItemUseContext context, CallbackInfoReturnable<ActionResultType> info) {
        if (!context.getWorld().isRemote) {
            if (context.getItem().getItem().getRegistryName().equals(new ResourceLocation("gunpowder"))) {
                info.setReturnValue(Fuse.placeFuseBlock(context));
            }
        }
    }
}
