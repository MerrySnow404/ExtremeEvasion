package cc.sighs.extremeevasion.handler;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.HashSet;
import java.util.List;

public class KeySet<T> {
    private final ResourceKey<Registry<T>> registryKey;
    private final HashSet<ResourceKey<T>> resourceKeys = new HashSet<>();
    private final HashSet<TagKey<T>> tagKeys = new HashSet<>();
    private final HashSet<T> values = new HashSet<>();
    private Registry<T> registry;

    public KeySet(ResourceKey<Registry<T>> registryKey) {
        this.registryKey = registryKey;
        NeoForge.EVENT_BUS.addListener(this::onTagsUpdated);
    }

    public void updateKeys(List<? extends String> keys) {
        resourceKeys.clear();
        tagKeys.clear();
        for (var key : keys) {
            if (key == null || key.isBlank()) continue;

            var isTag = key.charAt(0) == '#';
            var location = ResourceLocation.tryParse(isTag ? key.substring(1) : key);
            if (location == null) continue;

            if (isTag) {
                tagKeys.add(TagKey.create(registryKey, location));
                continue;
            }

            resourceKeys.add(ResourceKey.create(registryKey, location));
        }
        if (registry == null) return;

        updateValues(registry);
    }

    public void updateValues(Registry<T> registry) {
        values.clear();
        for (var resourceKey : resourceKeys) {
            var value = registry.get(resourceKey.location());
            values.add(value);
        }
        for (var tagKey : tagKeys) {
            for (var value : registry.getTagOrEmpty(tagKey)) {
                values.add(value.value());
            }
        }
    }

    public boolean contains(T value) {
        return values.contains(value);
    }

    private void onTagsUpdated(TagsUpdatedEvent event) {
        var registryAccess = event.getRegistryAccess();
        var registry = registryAccess.registryOrThrow(registryKey);
        updateValues(registry);
        this.registry = registry;
    }
}
