package gregapi.audio.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.audio.SoundLoop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class MachineSoundTickhandler {

    private final Map<TileEntity, SoundLoop> activeSounds = new HashMap<>();

    private final Minecraft mc = Minecraft.getMinecraft();
    private final SoundHandler soundhandler = mc.getSoundHandler();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event){
        if(event.phase != TickEvent.Phase.END) return;

        for (TileEntity te : activeSounds.keySet().toArray(new TileEntity[0])) {
            //Skip unloaded machines
            if (te.isInvalid()) {
                stopSound(te);
                continue;
            }

            boolean isRunning = checkMachineRunning(te);
            SoundLoop sound = activeSounds.get(te);

            if (isRunning && sound == null) {
                SoundLoop newSound = createSoundForMachine(te);
                soundhandler.playSound(newSound);
                activeSounds.put(te, newSound);

            } else if (!isRunning && sound != null) {
                stopSound(te);
            }
        }
    }

    private void stopSound(TileEntity te) {
        SoundLoop sound = activeSounds.remove(te);
        if (sound != null){
            // Don't use vanilla sound manager
            mc.getSoundHandler().stopSound(sound);
        }
    }

    private SoundLoop createSoundForMachine(TileEntity te){
        //Example: select sound key base on machine type/state
        String soundKey = "gregtech:running";
        SoundLoop sound = new SoundLoop(soundKey, te);
        sound.setVolume(0.45f);
        sound.setPitch(0.5f);
        return sound;
    }

    private boolean checkMachineRunning(TileEntity te) {
        if (te instanceof gregapi.tileentity.machines.ITileEntityRunningActively) {
            return ((gregapi.tileentity.machines.ITileEntityRunningActively) te).getStateRunningActively();
        }
        return false;
    }
}
