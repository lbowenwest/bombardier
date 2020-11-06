package tweakyllama.bombardier.base.handler;

import com.google.common.collect.ArrayListMultimap;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import tweakyllama.bombardier.Bombardier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class RegistryHandler {

    private static final Map<ResourceLocation, ItemGroup> groups = new LinkedHashMap<>();
    private static final ArrayListMultimap<Class<?>, Supplier<IForgeRegistryEntry<?>>> defers = ArrayListMultimap.create();

    @SubscribeEvent
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onRegistryEvent(RegistryEvent.Register<?> event) {
        IForgeRegistry registry = event.getRegistry();
        Class<?> type = registry.getRegistrySuperType();

        if (defers.containsKey(type)) {
            Collection<Supplier<IForgeRegistryEntry<?>>> entries = defers.get(type);
            for (Supplier<IForgeRegistryEntry<?>> supplier : entries) {
                IForgeRegistryEntry<?> entry = supplier.get();
                registry.register(entry);
                Bombardier.LOGGER.debug("registering " + entry.getRegistryName() + " to " + registry.getRegistryName());
            }
            defers.removeAll(type);
        }
    }

    public static void registerBlock(Block block, String name) {
        registerBlock(block, name, true);
    }

    public static void registerBlock(Block block, String name, boolean hasBlockItem) {
        register(block, name);

        if (hasBlockItem) {
            defers.put(Item.class, () -> createItemBlock(block));
        }
    }

    public static void registerItem(Item item, String name) {
        register(item, name);
    }

    public static <T extends IForgeRegistryEntry<T>> void register(IForgeRegistryEntry<T> obj) {
        if (obj == null)
            throw new IllegalArgumentException("cannot register null object");
        if (obj.getRegistryName() == null)
            throw new IllegalArgumentException("cannot register object without registry name");

        defers.put(obj.getRegistryType(), () -> obj);
    }

    public static <T extends IForgeRegistryEntry<T>> void register(IForgeRegistryEntry<T> obj, String name) {
        if (obj == null)
            throw new IllegalArgumentException("cannot register null object");

        obj.setRegistryName(GameData.checkPrefix(name, false));
        defers.put(obj.getRegistryType(), () -> obj);
    }

    private static Item createItemBlock(Block block) {
        Item.Properties props = new Item.Properties();
        ResourceLocation registryName = block.getRegistryName();

        if (registryName == null)
            throw new IllegalArgumentException("cannot create item for block with no registry name");

        ItemGroup group = groups.get(registryName);
        if (group != null)
            props = props.group(group);

        BlockItem blockItem = new BlockItem(block, props);
        return blockItem.setRegistryName(registryName);
    }
}
