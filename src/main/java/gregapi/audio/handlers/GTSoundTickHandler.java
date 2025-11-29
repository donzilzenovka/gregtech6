package gregapi.audio.handlers;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gregapi.audio.SoundLoop;
import gregapi.code.IMath;
import gregapi.tileentity.machines.MultiTileEntityBasicMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GTSoundTickHandler {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<TileEntity, ActiveSound> activeSounds = new HashMap<>();
    private static final String PREFIX = "gregapi:gt.";

    private static final Map<String, String[]> MACHINE_SOUND_MAP = new HashMap<>();
    private static final Map<Class<?>, Method> VISUAL_METHOD_CACHE = new HashMap<>();

    static {
        MACHINE_SOUND_MAP.put("MultiTileEntityFluidTap", new String[] { null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityCokeOven", new String[] { null, null, null, "burning_internal"});
        MACHINE_SOUND_MAP.put("centrifuge", new String[] {null, null, "spin_idle", "spin_processing"});
        MACHINE_SOUND_MAP.put("MultiTileEntityMotorLiquid", new String[] {null, "engine_active", "engine_stall", null});
        MACHINE_SOUND_MAP.put("MultiTileEntityMPipeFluid", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityBarrelMetal", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityResinHoleRubber", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityBumbleHive", new String[] {"bumble_hive", null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityPipeFluid", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityFluidFunnel", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityBush", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("distillery", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityAxle", new String[] {null, null, "axle_turning", null});
        MACHINE_SOUND_MAP.put("MultiTileEntityGearBox", new String[] {null, null, null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityGeneratorBrick", new String[] {null, "burning_external", null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityGeneratorMetal", new String[] {null, "burning_external", null, null});
        MACHINE_SOUND_MAP.put("MultiTileEntityGeneratorGas", new String[] {null, "burning_gas", null, null});


    }

    static String[] nameList = {"MultiTileEntityBasicMachine"};

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null) return;

        for (Object o : mc.theWorld.loadedTileEntityList) {
            if (!(o instanceof TileEntity)) continue;
            TileEntity te = (TileEntity) o;

            double dx = te.xCoord - mc.thePlayer.posX;
            double dy = te.yCoord - mc.thePlayer.posY;
            double dz = te.zCoord - mc.thePlayer.posZ;
            boolean isOutOfRange = (dx * dx + dy * dy + dz * dz > 256);

            String desiredKey = null;
            int mState = -1;
            String mName = te.getClass().getSimpleName();

            Method getVisualData = VISUAL_METHOD_CACHE.get(te.getClass());
            if (getVisualData == null && !VISUAL_METHOD_CACHE.containsKey(te.getClass())) {
                try {
                    getVisualData = te.getClass().getMethod("getVisualData");
                    VISUAL_METHOD_CACHE.put(te.getClass(), getVisualData);
                } catch (Throwable ignored) {
                    VISUAL_METHOD_CACHE.put(te.getClass(), null);
                }
            }
            if (getVisualData != null) {
                try {
                    mState = ((Number) getVisualData.invoke(o)).byteValue();
                } catch (Throwable ignored) {
                }
            }

            if (mName.equals("MultiTileEntityBasicMachine")) {
                mName = ((MultiTileEntityBasicMachine) (TileEntity)o).mRecipes.toString().replace("gt.recipe.", "");
            }


            if (mState != -1 && !isOutOfRange) {
                if (!mName.equals("MultiTileEntityBush")
                        && (!mName.equals("MultiTileEntityBumbleHive"))
                        && (!mName.equals("MultiTileEntityResinHoleRubber"))
                        && (!mName.equals("MultiTileEntityFluidTap"))
                        && (!mName.equals("MultiTileEntityBarrelMetal"))) { //TODO for debugging, remove

                    System.out.println(mName);
                    System.out.println(mState);
                }
                desiredKey = resolve(mName, mState);
                if (desiredKey != null) {
                    //System.out.println(desiredKey); //TODO remove
                }
            }

            ActiveSound active = activeSounds.get(te);
            boolean isSoundPlaying = active != null && mc.getSoundHandler().isSoundPlaying(active.loop);
            //fix out of range audio not restarting
            if (isOutOfRange || desiredKey == null) {
                if (active != null) stopSound(te);
                continue;
            }

            if (!isSoundPlaying) {
                if(active != null) stopSound(te);
                playLoop(PREFIX + desiredKey, te);
            } else if (!desiredKey.equals(active.key)) {
                stopSound(te);
                playLoop(PREFIX + desiredKey, te);
            } else {
                active.loop.updatePosition();
            }
        }
        cleanupInvalidTiles();
    }

    private void playLoop(String keyString, TileEntity te) {
        SoundLoop loop = new SoundLoop(keyString, te);
        loop.setRepeat(true);
        loop.setVolume(1.0f);
        loop.setPitch(1.0f + (float)Math.random() * 0.1f);
        String shortKey = keyString.substring(PREFIX.length());
        activeSounds.put(te, new ActiveSound(loop, shortKey));
        mc.getSoundHandler().playSound(loop);
    }

    private void stopSound(TileEntity te) {
        ActiveSound active = activeSounds.remove(te);
        if(active != null) mc.getSoundHandler().stopSound(active.loop);
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

    private void cleanupInvalidTiles() {
        activeSounds.entrySet().removeIf(entry -> {
            TileEntity te = entry.getKey();
            TileEntity worldTE = te.getWorldObj().getTileEntity(te.xCoord, te.yCoord, te.zCoord);
            if (te.isInvalid() || te.getWorldObj() == null || worldTE != te) {
                mc.getSoundHandler().stopSound(entry.getValue().loop);
                return true;
            }
            return false;
        });
    }

    private boolean nameListContains(String name) {
        for (String s : nameList) {
            if (s.equals(name)) return true;
        }
        return false;
    }

    private static class ActiveSound {
        public final SoundLoop loop;
        public String key;
        public ActiveSound(SoundLoop loop, String key) {
            this.loop = loop;
            this.key = key;
        }
    }
}
