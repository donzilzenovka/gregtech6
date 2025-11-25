package gregapi.audio;

import cpw.mods.fml.relauncher.ReflectionHelper;
import gregtech.GT6_Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;


public class GTSoundHandler extends SoundHandler {
    boolean logDebug = true;

    public GTSoundHandler(SoundHandler originalHandler) {
        super(getResourceManager(originalHandler), getGameSettings(originalHandler));
    }

    private static IResourceManager getResourceManager(SoundHandler handler) {
        // Names: "mcResourceManager" (MCP), "field_147695_g" (SRG)
        IResourceManager mgr = ReflectionHelper
                .getPrivateValue(SoundHandler.class, handler, "mcResourceManager", "field_147695_g");
        if (mgr == null) {
            throw new IllegalStateException("Failed to get IResourceManager from SoundHandler");
        }
        return mgr;
    }

    private static GameSettings getGameSettings(SoundHandler handler) {
        // 1. Get SoundManager from SoundHandler (Names: "sndManager", "field_147694_f")
        SoundManager sndManager = ReflectionHelper
                .getPrivateValue(SoundHandler.class, handler, "sndManager", "field_147694_f");
        if (sndManager == null) {
            throw new IllegalStateException("Failed to get SoundManager from SoundHandler (required for GameSettings lookup)");
        }

        // 2. Get GameSettings from SoundManager (Names: "options", "field_78903_e")
        GameSettings settings = ReflectionHelper
                .getPrivateValue(SoundManager.class, sndManager, "options", "field_78903_e");
        if (settings == null) {
            throw new IllegalStateException("Failed to get GameSettings from SoundManager");
        }
        return settings;
    }

    @Override
    public void playSound(ISound sound) {
        // Logging
        final Minecraft mc = Minecraft.getMinecraft();
        ResourceLocation soundLocation = sound.getPositionedSoundLocation();
        String logMessage = "[GT6] Sound: " + soundLocation.getResourcePath();
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
            GT6_Main.LOG.info(logMessage);
        }
    }

}
