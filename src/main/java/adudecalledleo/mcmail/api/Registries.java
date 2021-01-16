package adudecalledleo.mcmail.api;

import adudecalledleo.mcmail.api.meta.MetadataValueType;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import static adudecalledleo.mcmail.MCMail.MOD_ID;

public final class Registries {
    private Registries() { }

    public static final SimpleRegistry<MetadataValueType<?>> METADATA_VALUE_TYPE;
    public static final RegistryKey<Registry<MetadataValueType<?>>> METADATA_VALUE_TYPE_KEY;

    static {
        METADATA_VALUE_TYPE_KEY = RegistryKey.ofRegistry(new Identifier(MOD_ID, "metadata_key"));
        METADATA_VALUE_TYPE = new SimpleRegistry<>(METADATA_VALUE_TYPE_KEY, Lifecycle.stable());
    }
}
