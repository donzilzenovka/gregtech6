package gregapi.audio.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gregapi.audio.SoundLoop;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GTSoundTickHandler {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<TileEntity, SoundLoop> activeSounds = new HashMap<>();

    private static final Map<String, String[]> MACHINE_SOUND_MAP = new HashMap<>();

    static {
        MACHINE_SOUND_MAP.put("MultiTileEntityFluidTap", new String[] { null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityCokeOven", new String[] { null, null, null, "burning_internal"});
        MACHINE_SOUND_MAP.put("gt.recipe.centrifuge", new String[] {null, null, "spin_idle", "spin_processing"});
        MACHINE_SOUND_MAP.put("MultiTileEntityMotorLiquid", new String[] {null, "engine_active", "engine_stall", null});
        MACHINE_SOUND_MAP.put("MultiTileEntityMPipeFluid", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityBarrelMetal", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityResinHoleRubber", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityBumbleHive", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityBasicMachine", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityPipeFluid", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityFluidFunnel", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityBush", new String[] {null, null, null, null});


    }

    //private static final String SOUND_ACTIVE = "gregapi:gt.engine_active";
    //private static final String SOUND_STALL = "gregapi:gt.engine_stall";

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null) return;

        for (Object o : mc.theWorld.loadedTileEntityList) {
            String desiredKey = null;
            int mState = -1;
            String mName = null;
            String recipeName = null;

            if (o instanceof TileEntity) {
                Class<?> cls = o.getClass();
                mName = cls.getSimpleName();
                try {
                    Method getVisualData = cls.getMethod("getVisualData");
                    mState = ((Number) getVisualData.invoke(o)).byteValue();
                    } catch (Throwable ignored) {}
                try {
                    Field mRecipesField = cls.getDeclaredField("mRecipes");
                    mRecipesField.setAccessible(true);
                    Object recipeMap = mRecipesField.get(o);
                    if (recipeMap != null){
                        Field internalNameField = recipeMap.getClass().getField("aNameLocal");
                        recipeName = (String) internalNameField.get(recipeMap);
                    }
                } catch (Throwable ignored) {}


                if (mState != -1 && mName != null) {
                   // if (!mName.equals("MultiTileEntityBush") && (!mName.equals("MultiTileEntityBumbleHive"))) { //TODO for debugging, remove

                        System.out.println(mName);
                        System.out.println(recipeName);
                        System.out.println(mState);
                    //}
                    desiredKey = resolve(mName, mState);
                    if (desiredKey != null) {
                        //System.out.println(desiredKey); //TODO remove
                    }
                }

                if (desiredKey == null) {
                    stopSound((TileEntity) o);
                    continue;
                }

                SoundLoop current = activeSounds.get((TileEntity) o);

                if (current == null) {
                    playLoop(desiredKey, (TileEntity) o);
                    continue;
                }

                if (!desiredKey.equals(current.getKey())) {
                    stopSound((TileEntity) o);
                    playLoop(desiredKey, (TileEntity) o);
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

    private String resolve(String machine, int state) {
        String[] mSound = MACHINE_SOUND_MAP.get(machine);
        if (mSound == null) {
            System.out.println("WARNING: No sound map for: " + machine);
            return null;
        }
        if (state < 0 || state >= mSound.length) return null;
        return mSound[state];
    }

    private void cleanupInvalidTiles(){
        activeSounds.keySet().removeIf(te ->
                te.isInvalid() || te.getWorldObj() != mc.theWorld);
    }
}
