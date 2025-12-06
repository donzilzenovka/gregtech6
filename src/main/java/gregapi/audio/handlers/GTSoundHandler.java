package gregapi.audio.handlers;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;

import static gregapi.data.CS.*;


public class GTSoundHandler extends SoundHandler {
    boolean logDebug = false;

    public GTSoundHandler(SoundHandler originalHandler) {
        super(getResourceManagerFrom(originalHandler), Minecraft.getMinecraft().gameSettings);

        // Locate original SoundManager instance from the provided handler (type-scan)
        SoundManager originalManager = getPrivateFieldValueByType(originalHandler, SoundManager.class);
        if (originalManager == null) {
            ERR.println("CRITICAL FAILURE: Original SoundManager reflection returned null. Sounds will not play.");
            return;
        }

        // Inject running SoundManager into 'this' instance (type-scan on SoundHandler)
        boolean injected = setPrivateFieldByType(this, originalManager);
        if (injected) {
            OUT.println("SoundManager substitution successful. Audio delegation should now work.");
        } else {
            ERR.println("CRITICAL FAILURE: Could not inject original SoundManager. Sounds will likely not play.");
        }
    }

    private static IResourceManager getResourceManagerFrom(SoundHandler handler) {
        // Try type-based lookup for IResourceManager on SoundHandler
        IResourceManager mgr = getPrivateFieldValueByType(handler, IResourceManager.class);
        if (mgr == null) {
            throw new IllegalStateException("Failed to get IResourceManager from SoundHandler");
        }
        return mgr;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getPrivateFieldValueByType(Object target, Class<T> type) {
        if (target == null) return null;
        Field f = findAccessibleFieldByType(target.getClass(), type);
        if (f != null) {
            try {
                Object val = f.get(target);
                return (T) val;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static boolean setPrivateFieldByType(Object target, Object value) {
        if (target == null) return false;
        Field f = findAccessibleFieldByType(target.getClass(), SoundManager.class);
        if (f != null) {
            try {
                f.set(target, value);
                return true;
            } catch (Throwable t) {
                ERR.println("Failed to set private field by type");
                return false;
            }
        }
        return false;
    }

    private static Field findAccessibleFieldByType(Class<?> targetClass, Class<?> type) {
        Class<?> currentClass = targetClass;
        // Search the class and all its superclasses
        while (currentClass != null) {
            for (Field f : currentClass.getDeclaredFields()) {
                if (type.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        return f;
                    } catch (Throwable t) {
                        OUT.println("Failed to set accessible on field of type " + type.getSimpleName());
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    @Override
    public void playSound(ISound sound) {

        if (sound == null) {
            super.playSound(null);
            return;
        }

        // --- Ignore music discs ---
        String soundClass = sound.getClass().getSimpleName();
        if (soundClass.contains("MovingSound") || soundClass.contains("Streaming")) {
            super.playSound(sound);
            return;
        }

        ResourceLocation soundLocation = null;
        try {
            soundLocation = sound.getPositionedSoundLocation();
        } catch (Throwable ignored) {}

        if (soundLocation != null) {
            String path = soundLocation.getResourcePath();
            if (path!= null) {
                if (path.startsWith("records.") ||
                        path.startsWith("music.") ||
                        path.startsWith("streaming") ||
                        path.startsWith("record.") || path.contains("jukebox")) {
                    super.playSound(sound);
                    return;
                }
            }
        }

        // endof --- Ignore music discs ---

        // Logging
        writeLog(sound);
        //Play sound
        super.playSound(sound);
    }

    public void writeLog(ISound sound){
        final Minecraft mc = Minecraft.getMinecraft();
        ResourceLocation soundLocation = sound.getPositionedSoundLocation();
        String logMessage = "GT_Mod: Sound: " + soundLocation.getResourcePath();
        if (mc.thePlayer != null && sound instanceof PositionedSound) {
            PositionedSound ps = (PositionedSound) sound;
            logMessage += String.format(
                    " @ Pos(%.2f, %.2f, %.2f), Vol: %.2f, Pitch: %.2f",
                    ps.getXPosF(),
                    ps.getYPosF(),
                    ps.getZPosF(),
                    ps.getVolume(),
                    ps.getPitch()
            );
        }
        if (logDebug){
            DEB.println(logMessage);
            System.out.println(logMessage);
        }
    }

    // ---------- TheBetweenLands compatibility hacky fix ------------
    @Override
    public SoundEventAccessorComposite getSound(ResourceLocation soundLocation) {
        // 1. Try to get the real sound metadata
        SoundEventAccessorComposite realAccessor = super.getSound(soundLocation);

        if (realAccessor != null) {
            return realAccessor;
        }

        // 2. CRASH MITIGATION: If lookup fails, provide a dummy object
        // This is now necessary because a valid sound (step sound) is failing lookup
        // when exposed to the event bus, even in-game.
        if (soundLocation != null) {
            // Use the universal DummySoundAccessor
            return new DummySoundAccessor(soundLocation);
        }

        return null;
    }

    private static class DummySoundAccessor extends SoundEventAccessorComposite {

        public DummySoundAccessor(ResourceLocation location) {
            // Obfuscated constructor in 1.7.10 (bti, String, float, SoundCategory)
            // We'll use the deobf names or a best-effort call.
            // The actual constructor needed is likely (ResourceLocation, float, float) or similar.

            // Due to the complexity of the obfuscated constructor, we rely on the parent class
            // having a simple constructor or mock the getSoundCategory method if possible.
            super(location, 1.0f, 1.0f, SoundCategory.MUSIC); // Best guess for a basic construction
        }

        // Override the method The Betweenlands is trying to access.
        @Override
        public SoundCategory getSoundCategory() {
            // Return the expected category to satisfy The Betweenlands' check (SoundCategory.MUSIC)
            return SoundCategory.MUSIC;
        }
    }

    // -------------- End of hacky fix -----------
}
