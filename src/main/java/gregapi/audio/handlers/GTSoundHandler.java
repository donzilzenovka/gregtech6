package gregapi.audio.handlers;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

import static gregapi.data.CS.*;


public class GTSoundHandler extends SoundHandler {
    boolean logDebug = false;

    public GTSoundHandler(SoundHandler originalHandler) {
        super(getResourceManager(originalHandler), getGameSettings(originalHandler));

        SoundManager originalManager = ReflectionHelper
                .getPrivateValue(SoundHandler.class, originalHandler, "sndManager", "field147694_f");

        if(originalManager == null) {
            ERR.println("GT_Mod: CRITICAL FAILURE: Original SoundManager reflection returned null. Sounds will not play.");
            return;
        }

        try{
            ReflectionHelper.setPrivateValue(SoundHandler.class, this, originalManager, "sndManager", "field_147694_f");
            OUT.println("GT_Mod: SoundManager substitution successful. Audio Delegation active.");
        } catch (Exception e){
            ERR.println("GT_Mod: CRITICAL FAILURE: Could not inject original SoundManager.");
        }
    }

    private static IResourceManager getResourceManager(SoundHandler handler) {
        IResourceManager mgr = ReflectionHelper
                .getPrivateValue(SoundHandler.class, handler, "mcResourceManager", "field_147695_g");
        if (mgr == null) {
            throw new IllegalStateException("Failed to get IResourceManager from SoundHandler");
        }
        return mgr;
    }

    private static GameSettings getGameSettings(SoundHandler handler) {
        SoundManager sndManager = ReflectionHelper
                .getPrivateValue(SoundHandler.class, handler, "sndManager", "field_147694_f");
        if (sndManager == null) {
            throw new IllegalStateException("Failed to get SoundManager from SoundHandler (required for GameSettings lookup)");
        }

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
        writeLog(sound);
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
}
