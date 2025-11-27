package gregapi.audio.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gregapi.audio.SoundLoop;
import gregtech.tileentity.energy.generators.MultiTileEntityMotorLiquid;
import li.cil.oc.client.Sound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class GTSoundTickHandler {

    //private final GTSoundHandler soundHandler;
    private final Minecraft mc = Minecraft.getMinecraft();

    private final Map<TileEntity, SoundLoop> activeSounds = new HashMap<>();

    private static final String SOUND_ACTIVE = "gregapi:gt.engine_active";
    private static final String SOUND_STALL = "gregapi:gt.engine_stall";

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null) return;

        // Loop
        for (Object o : mc.theWorld.loadedTileEntityList) {
            if ((o instanceof MultiTileEntityMotorLiquid)) {
                MultiTileEntityMotorLiquid engine = (MultiTileEntityMotorLiquid) o;
                System.out.println("Coord: " + engine.getX() + ", " + engine.getY() + ", " + engine.getZ() + " State: " + engine.mActivity.mState);
                String desiredKey = resolve(engine);

                if (desiredKey == null) {
                    stopSound(engine);
                    continue;
                }

                SoundLoop current = activeSounds.get(engine);

                if (current == null) {
                    playLoop(desiredKey, engine);
                    continue;
                }

                if (!desiredKey.equals(current.getKey())) {
                    stopSound(engine);
                    playLoop(desiredKey, engine);
                    continue;
                }

                current.updatePosition();
            }
        }

        cleanupInvalidTiles();
    }

    private void playLoop(String key, TileEntity te) {
        SoundLoop loop = new SoundLoop(key, te);
        loop.setRepeat(true);
        loop.setVolume(0.45f);
        loop.setPitch(0.5f + (float)Math.random() * 0.1f);
        activeSounds.put(te, loop);
        mc.getSoundHandler().playSound(loop);
    }

    private void stopSound(TileEntity engine) {
        SoundLoop sound = activeSounds.remove(engine);
        if(sound != null) mc.getSoundHandler().stopSound(sound);
    }

    private String resolve(MultiTileEntityMotorLiquid engine) {
        int s = engine.mActivity.mState;

        switch (s) {
            case 1: return SOUND_ACTIVE;
            case 2: return SOUND_STALL;
            default: return null;
        }
    }

    private void cleanupInvalidTiles(){
        activeSounds.keySet().removeIf(te ->
                te.isInvalid() || te.getWorldObj() != mc.theWorld);
    }

}
