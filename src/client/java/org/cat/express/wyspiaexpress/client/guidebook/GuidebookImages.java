package org.cat.express.wyspiaexpress.client.guidebook;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/** Texture dimensions are cached; outside-assets metadata icons are registered and closed on reload. */
public final class GuidebookImages {
    public record Art(Identifier texture, int width, int height) {}
    private static final Identifier FALLBACK = Identifier.of("wyspiaexpress", "icon.png");
    private static final Map<Identifier, Art> CACHE = new HashMap<>();
    private static final Map<String, Art> MOD_ICONS = new HashMap<>();
    private static int generation = -1;

    private GuidebookImages() {}

    public static Art get(GuidebookEntry entry) {
        var client = MinecraftClient.getInstance();
        if (generation != GuidebookDefinitions.INSTANCE.generation()) {
            MOD_ICONS.values().stream().filter(art -> art.texture.getPath().startsWith("guidebook/generated/"))
                    .forEach(art -> client.getTextureManager().destroyTexture(art.texture));
            CACHE.clear();
            MOD_ICONS.clear();
            generation = GuidebookDefinitions.INSTANCE.generation();
        }
        Identifier illustration = entry.definition().image();
        Art art = illustration != null ? resource(illustration) : null;
        if (art != null) return art;
        String modId = entry.id().getNamespace();
        if (!MOD_ICONS.containsKey(modId)) MOD_ICONS.put(modId, modIcon(modId));
        art = MOD_ICONS.get(modId);
        if (art != null) return art;
        art = resource(FALLBACK);
        return art != null ? art : new Art(FALLBACK, 128, 128);
    }

    private static Art resource(Identifier id) {
        if (CACHE.containsKey(id)) return CACHE.get(id);
        Art art = null;
        try {
            var resource = MinecraftClient.getInstance().getResourceManager().getResource(id);
            if (resource.isPresent()) try (InputStream stream = resource.get().getInputStream(); NativeImage image = NativeImage.read(stream)) {
                art = new Art(id, image.getWidth(), image.getHeight());
            }
        } catch (Exception error) { WyspiaExpress.LOGGER.warn("Cannot read guidebook image {}: {}", id, error.getMessage()); }
        CACHE.put(id, art);
        return art;
    }

    private static Art modIcon(String modId) {
        var container = FabricLoader.getInstance().getModContainer(modId);
        if (container.isEmpty()) return null;
        var path = container.get().getMetadata().getIconPath(128);
        if (path.isEmpty()) return null;
        String icon = path.get();
        if (icon.startsWith("assets/")) {
            String[] pieces = icon.split("/", 3);
            if (pieces.length == 3) {
                Art art = resource(Identifier.of(pieces[1], pieces[2]));
                if (art != null) return art;
            }
        }
        var file = container.get().findPath(icon);
        if (file.isEmpty()) return null;
        try (InputStream stream = Files.newInputStream(file.get())) {
            NativeImage image = NativeImage.read(stream);
            Identifier texture = Identifier.of("wyspiaexpress", "guidebook/generated/" + modId);
            MinecraftClient.getInstance().getTextureManager().registerTexture(texture, new NativeImageBackedTexture(image));
            return new Art(texture, image.getWidth(), image.getHeight());
        } catch (Exception error) {
            WyspiaExpress.LOGGER.warn("Cannot load guidebook icon for {}: {}", modId, error.getMessage());
            return null;
        }
    }
}
