package gregapi.audio.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gregapi.audio.SoundLoop;
import gregtech.tileentity.energy.generators.MultiTileEntityMotorLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class GTSoundTickHandler {

    private final GTSoundHandler soundHandler;
    private final Minecraft mc = Minecraft.getMinecraft();

    private final Map<TileEntity, SoundLoop> activeSounds = new HashMap<>();

    private static final String SOUND_IDLE = "gregtech:gt.engine_idle";
    private static final String SOUND_ACTIVE = "gregtech:gt.engine_active";
    private static final String SOUND_STALL = "gregtech:gt.engine_stall";

    public GTSoundTickHandler(GTSoundHandler handler) {
        this.soundHandler = handler;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null) return;

        // Loop
        for (Object o : mc.theWorld.loadedTileEntityList) {
            System.out.println("first test");
            if (!(o instanceof MultiTileEntityMotorLiquid)) continue;
            System.out.println("second test");
            MultiTileEntityMotorLiquid engine = (MultiTileEntityMotorLiquid) o;

            SoundLoop current = activeSounds.get(engine);
            String desiredKey = determineKey(engine);

            //Start new sound
            if (current == null) {
                SoundLoop loop = createLoopedSound(desiredKey, engine);
                activeSounds.put(engine, loop);
                mc.getSoundHandler().playSound(loop);
                continue;
            }

            //swap sound
            if(!desiredKey.equals(current.getKey())) {
                stopSound(engine);
                SoundLoop loop = createLoopedSound(desiredKey, engine);
                activeSounds.put(engine, loop);
                mc.getSoundHandler().playSound(loop);
                continue;
            }

            // update params
            current.updatePosition();

            float pitch = 1.0f;
            float vol = 1.0f;

            current.setVolume(vol);
            current.setPitch(pitch);
        }

    }

    private void stopSound(TileEntity engine) {
        SoundLoop sound = activeSounds.remove(engine);
        if(sound != null) {
            mc.getSoundHandler().stopSound(sound);
        }
    }

    private String determineKey(MultiTileEntityMotorLiquid engine) {
        if (engine.getStateRunningActively()) return SOUND_ACTIVE;
        if (engine.getStateRunningPossible()) return SOUND_IDLE;
        return SOUND_STALL;
    }

    private SoundLoop createLoopedSound(String key, MultiTileEntityMotorLiquid engine) {
        SoundLoop loop = new SoundLoop(key, engine);
        loop.setRepeat(true);
        loop.setVolume(0.45f);
        loop.setPitch(0.5f + (float) Math.random() * 0.1f);
        return loop;
    }

    private ISound determineSound(MultiTileEntityMotorLiquid engine) {
        int s = engine.mActivity.mState;

        if (s == 0) return null;

        ResourceLocation r1;

        switch (s) {
            case 1: r1 = new ResourceLocation("gregtech", "diesel.starting"); break;
            case 2: r1 = new ResourceLocation("gregtech", "diesel.running"); break;
            case 3: r1 = new ResourceLocation("gregtech", "diesel.struggle"); break;
            default: r1 = new ResourceLocation("gregtech","diesel.idle"); break;
        }

        float volume = 1.0f;
        float pitch = 1.0f;

        return new PositionedSoundRecord(
                r1,
                volume,
                pitch,
                engine.xCoord + 0.5f,
                engine.yCoord + 0.5f,
                engine.zCoord + 0.5f
        );
    }

}
