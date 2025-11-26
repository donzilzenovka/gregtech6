package gregapi.audio.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gregapi.audio.SoundLoop;
import gregapi.tileentity.base.TileEntityBase09FacingSingle;
import gregtech.tileentity.energy.generators.MultiTileEntityMotorLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;
import java.util.Map;

public class GTTickSoundHandler {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final Map<TileEntity, ISound> activeSounds = new HashMap<>();

    private static final String SOUND_IDLE = "gregtech:engine.idle";
    private static final String SOUND_ACTIVE = "gregtech:engine.active";
    private static final String SOUND_STALL = "gregtech:engine_stall";

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null) return;

        // Loop
        for (Object o : mc.theWorld.loadedTileEntityList) {
            if (!(o instanceof MultiTileEntityMotorLiquid)) continue;

            MultiTileEntityMotorLiquid engine = (MultiTileEntityMotorLiquid) o;

            ISound currentSound = activeSounds.get(engine);
            ISound newSound = determineSound(engine);
        }

    }

    private ISound determineSound(MultiTileEntityMotorLiquid engine) {
        if (engine.getStateRunningPossible()){
            return createLoopedSound(SOUND_IDLE, engine);
        } else if (engine.getStateRunningActively()){
            return createLoopedSound(SOUND_ACTIVE, engine);
        } else {
          return createLoopedSound(SOUND_STALL, engine);
        }
    }

    private ISound createLoopedSound(String key, MultiTileEntityMotorLiquid engine) {
        SoundLoop loop = new SoundLoop(key, engine);
        loop.setRepeat(loop,true);
        loop.setSoundVolume(loop, 0.45f);

        loop.setSoundPitch(loop, 0.5f + (float) Math.random() * 0.1f);
        return loop;
    }
}
