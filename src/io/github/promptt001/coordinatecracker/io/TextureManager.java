package io.github.promptt001.coordinatecracker.io;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import io.github.promptt001.coordinatecracker.data.EnumBlockType;

/**
 * Loads exact preview textures for the GUI.
 *
 * The default source is embedded Java ARGB data decoded from the vanilla block
 * texture PNGs used by the previous bundled build. That keeps the application
 * self-contained without storing or importing the original PNG files. A user can
 * still explicitly choose a resource pack/assets folder when they need a custom
 * or version-specific texture source.
 */
public final class TextureManager {
    private static final String PREF_KEY = "textureSource";
    private static final String PREF_NODE = "/io/github/promptt001/coordinatecracker";
    private static final String EMBEDDED_SOURCE_DESCRIPTION = "embedded vanilla ARGB texture data";

    private final Map<EnumBlockType, BufferedImage> textures = new HashMap<EnumBlockType, BufferedImage>();
    private File source;
    private String statusMessage = "Textures: exact vanilla textures not loaded";

    public void loadInitialSource() {
        String explicit = System.getProperty("coordinatecracker.assets");
        if(explicit != null && !explicit.trim().isEmpty() && loadFrom(new File(explicit.trim()), false)) return;

        String env = System.getenv("COORDINATECRACKER_ASSETS");
        if(env != null && !env.trim().isEmpty() && loadFrom(new File(env.trim()), false)) return;

        if(loadEmbeddedVanillaTextures()) return;

        String pref = Preferences.userRoot().node(PREF_NODE).get(PREF_KEY, "");
        if(pref != null && !pref.trim().isEmpty() && loadFrom(new File(pref.trim()), false)) return;

        markTexturesMissing();
    }

    public void loadUserSource(File chosen) {
        if(chosen == null) return;
        if(loadFrom(chosen, true)) {
            Preferences.userRoot().node(PREF_NODE).put(PREF_KEY, chosen.getAbsolutePath());
        }
    }

    public BufferedImage getTexture(EnumBlockType type) {
        return textures.get(type);
    }

    public boolean hasTexture(EnumBlockType type) {
        return textures.containsKey(type);
    }

    public int getLoadedTextureCount() {
        return textures.size();
    }

    public boolean hasAllTextures() {
        return textures.size() == EnumBlockType.values().length;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getSourceDescription() {
        return source == null ? statusMessage : source.getAbsolutePath();
    }

    private boolean loadEmbeddedVanillaTextures() {
        Map<EnumBlockType, BufferedImage> loaded = new HashMap<EnumBlockType, BufferedImage>();
        for(EnumBlockType type : EnumBlockType.values()) {
            BufferedImage image = EmbeddedVanillaTextures.get(textureFileName(type));
            if(image != null) loaded.put(type, normalizeTexture(image));
        }

        if(loaded.isEmpty()) return false;
        textures.clear();
        textures.putAll(loaded);
        source = null;
        statusMessage = buildLoadedStatus(loaded.size(), EnumBlockType.values().length, EMBEDDED_SOURCE_DESCRIPTION);
        return true;
    }

    private boolean loadFrom(File candidate, boolean userChosen) {
        File resolved = resolveCandidate(candidate);
        if(resolved == null || !resolved.exists()) {
            if(userChosen) statusMessage = "Textures: source not found — " + candidate.getAbsolutePath();
            return false;
        }

        Map<EnumBlockType, BufferedImage> loaded = new HashMap<EnumBlockType, BufferedImage>();
        for(EnumBlockType type : EnumBlockType.values()) {
            BufferedImage image = readTexture(resolved, textureFileName(type));
            if(image != null) loaded.put(type, normalizeTexture(image));
        }

        if(loaded.isEmpty()) {
            if(userChosen) statusMessage = "Textures: no exact block PNGs found in " + candidate.getName() + "; keeping current textures";
            return false;
        }

        textures.clear();
        textures.putAll(loaded);
        source = resolved;
        statusMessage = buildLoadedStatus(loaded.size(), EnumBlockType.values().length, resolved.getName());
        return true;
    }

    private void markTexturesMissing() {
        textures.clear();
        source = null;
        statusMessage = "Textures: exact texture data missing; choose a resource pack or assets folder";
    }

    private static String buildLoadedStatus(int loaded, int total, String sourceName) {
        if(loaded == total) {
            return "Textures: " + loaded + "/" + total + " exact previews loaded from " + sourceName;
        }
        return "Textures: " + loaded + "/" + total + " exact previews loaded from " + sourceName + "; missing entries show placeholders";
    }

    private static File resolveCandidate(File candidate) {
        if(candidate == null) return null;
        if(candidate.isFile()) return candidate;
        File appPack = new File(candidate, "coordinatecracker/textures/block");
        if(appPack.exists()) return candidate;
        File assetsPack = new File(candidate, "assets/coordinatecracker/textures/block");
        if(assetsPack.exists()) return candidate;
        File vanillaPack = new File(candidate, "assets/minecraft/textures/block");
        if(vanillaPack.exists()) return candidate;
        return candidate;
    }

    private static String textureFileName(EnumBlockType type) {
        EnumBlockType safeType = type == null ? EnumBlockType.DEEPSLATE : type;
        return safeType.getTextureFileName();
    }

    private static BufferedImage normalizeTexture(BufferedImage image) {
        if(image == null) return null;
        if(image.getType() == BufferedImage.TYPE_INT_ARGB) return image;
        BufferedImage normalized = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = normalized.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return normalized;
    }

    private static BufferedImage readTexture(File source, String fileName) {
        try {
            if(source.isDirectory()) {
                File[] paths = new File[] {
                    new File(source, "assets/coordinatecracker/textures/block/" + fileName),
                    new File(source, "coordinatecracker/textures/block/" + fileName),
                    new File(source, "assets/minecraft/textures/block/" + fileName),
                    new File(source, "minecraft/textures/block/" + fileName),
                    new File(source, "textures/block/" + fileName),
                    new File(source, fileName)
                };
                for(File path : paths) {
                    if(path.exists() && path.isFile()) return ImageIO.read(path);
                }
                return null;
            }

            try(ZipFile zip = new ZipFile(source)) {
                String[] entries = new String[] {
                    "assets/coordinatecracker/textures/block/" + fileName,
                    "coordinatecracker/textures/block/" + fileName,
                    "assets/minecraft/textures/block/" + fileName,
                    "minecraft/textures/block/" + fileName,
                    "textures/block/" + fileName,
                    fileName
                };
                for(String entryName : entries) {
                    ZipEntry entry = zip.getEntry(entryName);
                    if(entry != null) {
                        try(InputStream stream = zip.getInputStream(entry)) {
                            return ImageIO.read(stream);
                        }
                    }
                }
            }
        } catch(IOException e) {
            return null;
        }
        return null;
    }
}
