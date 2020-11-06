package tweakyllama.bombardier.base.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import tweakyllama.bombardier.Bombardier;
import tweakyllama.bombardier.base.module.Module;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigBuilder {

    private ArrayList<Module> modules;
    private final List<Runnable> refreshAllConfig = new ArrayList<>();

    public ConfigBuilder(ArrayList<Module> modules) {
        this.modules = modules;
    }

    public void makeSpec() {
        ForgeConfigSpec.Builder forgeBuilder = new ForgeConfigSpec.Builder();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, forgeBuilder.configure(builder -> build(builder, modules)).getRight());
    }


    private Void build(ForgeConfigSpec.Builder builder, ArrayList<Module> modules) {
        modules.forEach(module -> {
            if (!module.description.isEmpty()) {
                builder.comment(module.description);
            }
            builder.push(module.getName());

            List<Field> fields = new ArrayList<>(Arrays.asList(module.getClass().getDeclaredFields()));
            fields.forEach(field -> {
                Config annotation = field.getDeclaredAnnotation(Config.class);
                if (annotation == null)
                    return;

                field.setAccessible(true);
                String name = annotation.name();
                String description = annotation.description();

                if (name.isEmpty())
                    name = field.getName();

                if (!description.isEmpty())
                    builder.comment(description);

                try {
                    ForgeConfigSpec.ConfigValue<?> val;
                    Object defaultVal = field.get(null);

                    if (defaultVal instanceof List) {
                        val = builder.defineList(name, (List<?>) defaultVal, o -> true);
                    } else {
                        val = builder.define(name, defaultVal);
                    }
                    refreshAllConfig.add(() -> {
                       try {
                           field.set(null, val.get());
                       } catch (IllegalAccessException e) {
                           Bombardier.LOGGER.error("Could not set config value for " + module.getName());
                           throw new RuntimeException(e);
                       }
                    });
                } catch (ReflectiveOperationException e) {
                    Bombardier.LOGGER.error("Failed to get config for" + module.getName());
                }

            });
            builder.pop();
        });

        return null;
    }

    public void refreshConfig() {
        refreshAllConfig.forEach(Runnable::run);
    }
}
