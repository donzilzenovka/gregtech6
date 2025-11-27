package gregapi.audio.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gregapi.audio.SoundLoop;
import gregapi.tileentity.machines.MultiTileEntityBasicMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GTSoundTickHandler {

    private final Minecraft mc = Minecraft.getMinecraft();

    private final Map<TileEntity, SoundLoop> activeSounds = new HashMap<>();

    private static final Map<String, String[]> MACHINE_SOUND_MAP = new HashMap<>();

    static {
        MACHINE_SOUND_MAP.put("cokeoven", new String[] { null, null, null, "burning_internal"});
        MACHINE_SOUND_MAP.put("centrifuge", new String[] {null, null, "spin_idle", "spin_processing"});
    }

    private static final String SOUND_ACTIVE = "gregapi:gt.engine_active";
    private static final String SOUND_STALL = "gregapi:gt.engine_stall";

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null) return;

        // Loop
        for (Object o : mc.theWorld.loadedTileEntityList) {
            int offSet = 0;
            if ((o instanceof MultiTileEntityBasicMachine)) {
                MultiTileEntityBasicMachine te = (MultiTileEntityBasicMachine) o;
                String mName = getMachineType(te);
                if(Objects.equals(mName, "cokeoven")) offSet = 2;
                if(te.getVisualData() > offSet){
                    //System.out.println(getMachineType(te));
                    //System.out.println(te.getVisualData());
                    String desiredKey = resolve(mName, te.getVisualData());
                    System.out.println("Machine: " + mName + ", Sound:" + desiredKey);
                }


                //System.out.println("Coord: " + engine.getX() + ", " + engine.getY() + ", " + engine.getZ() + " State: " + engine.mActivity.mState);
                //String desiredKey = resolve(mName, te.getVisualData());

                //System.out.println(desiredKey);
                /*

                if (desiredKey == null) {
                    stopSound(te);
                    continue;
                }

                SoundLoop current = activeSounds.get(te);

                if (current == null) {
                    playLoop(desiredKey, te);
                    continue;
                }

                if (!desiredKey.equals(current.getKey())) {
                    stopSound(te);
                    playLoop(desiredKey, te);
                    continue;
                }

                current.updatePosition();

                 */
            }
        }

        //cleanupInvalidTiles();
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

    private String resolve(String machine, int state) {
        String[] mSound = MACHINE_SOUND_MAP.get(machine.toLowerCase());
        if (mSound == null) return null;
        if (state < 0 || state >= mSound.length) return null;
        return mSound[state];
    }



    private void cleanupInvalidTiles(){
        activeSounds.keySet().removeIf(te ->
                te.isInvalid() || te.getWorldObj() != mc.theWorld);
    }

    private String getMachineType(MultiTileEntityBasicMachine te) {
        if (te.mRecipes == null) return null;
        //String s = te.mRecipes.toString();
        //int idx = s.lastIndexOf('.');
        //if (idx == -1 || idx == s.length() - 1) return s;
        //return te.mRecipes.toString().substring(idx + 1);
        return te.mRecipes.toString().replace("gt.recipe.", "");
    }

}
