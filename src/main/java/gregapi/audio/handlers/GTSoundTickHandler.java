package gregapi.audio.handlers;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gregapi.audio.SoundLoop;
import gregapi.tileentity.machines.MultiTileEntityBasicMachine;
import gregtech.tileentity.energy.converters.MultiTileEntityBoilerTank;
import gregtech.tileentity.energy.converters.MultiTileEntityEngineSteam;
import gregtech.tileentity.energy.reactors.MultiTileEntityReactorCore;
import gregtech.tileentity.energy.transformers.MultiTileEntityGearBox;
import gregtech.tileentity.energy.transformers.MultiTileEntityTransformerRotation;
import gregtech.tileentity.tools.MultiTileEntitySmeltery;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static gregapi.data.CS.B;

public class GTSoundTickHandler {

    private final GTSoundHandler soundHandler;
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<TileEntity, ActiveSound> activeSounds = new HashMap<>();
    public static final WeakHashMap<TileEntity, Integer> teSoundTokens = new WeakHashMap<>();
    private static final String PREFIX = "gregapi:gt.";

    private static final Map<String, String[]> SOUND_MAP = new HashMap<>();
    private static final Map<Class<?>, Method> VISUAL_METHOD_CACHE = new HashMap<>();

    public GTSoundTickHandler(GTSoundHandler handler) {
        this.soundHandler = handler;
    }

    static {
        SOUND_MAP.put("MultiTileEntityFluidTap", new String[] { null, null, null, null});
        SOUND_MAP.put("MultiTileEntityCokeOven", new String[] { null, null, null, "burning_internal"});
        SOUND_MAP.put("centrifuge", new String[] {null, null, "spin_idle", "spin_processing"});
        SOUND_MAP.put("MultiTileEntityMotorLiquid", new String[] {null, "engine_active", "engine_idle", null});
        SOUND_MAP.put("MultiTileEntityMPipeFluid", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityBarrelMetal", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityResinHoleRubber", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityBumbleHive", new String[] {"bumble_hive", null, null, null});
        SOUND_MAP.put("MultiTileEntityPipeFluid", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityFluidFunnel", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityBush", new String[] {null, null, null, null});
        SOUND_MAP.put("distillery", new String[] {null, null, "distill_idle", "distill_processing"});
        SOUND_MAP.put("MultiTileEntityAxle", new String[] {null, null, "axle_turning", null});
        SOUND_MAP.put("MultiTileEntityGearBox", new String[] {null, "gear_turn", null, null});
        SOUND_MAP.put("MultiTileEntityGeneratorBrick", new String[] {null, "burning_external", null, null});
        SOUND_MAP.put("MultiTileEntityGeneratorMetal", new String[] {null, "burning_external", null, null});
        SOUND_MAP.put("MultiTileEntityGeneratorGas", new String[] {null, "burning_gas", null, null});
        SOUND_MAP.put("MultiTileEntityGeneratorLiquid", new String[] {null, "burning_external", null, null});
        SOUND_MAP.put("bath", new String[] {null, null, null, "bath_processing"});
        SOUND_MAP.put("MultiTileEntitySmeltery", new String[] {null, "stress_crack", null, null});
        SOUND_MAP.put("MultiTileEntityMixingBowlTable", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityMortar", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityMold", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityBarrelWood", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntitySafeMechanical", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityBottleCrate", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityAdvancedCraftingTable", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityDrawerQuad", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityAnvil", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityGrindStone", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityBathingPotTable", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityMassStorageStandard", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityBookShelf", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityCrank", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntitySafeKeyLocked", new String[] {null, null, null, null});
        SOUND_MAP.put("shredder", new String[] {null, null, "shredder_idle", "shredder_processing"});
        SOUND_MAP.put("MultiTileEntityPipeItem", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityHopper", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityTank3x3x3Metal", new String[] {null, null, null, null});
        SOUND_MAP.put("drying", new String[] {null, null, "distill_idle", "dryer_processing"});
        SOUND_MAP.put("MultiTileEntityTurbineSteam", new String[] {null, "steam_turbine_running", "steam_turbine_stall", null});
        SOUND_MAP.put("MultiTileEntityBoilerTank", new String[] {
                null, "boiler_1", "boiler_2", "boiler_3", "boiler_4", "boiler_5", "boiler_6", "boiler_7", "boiler_8",
                "boiler_9", "boiler_10", "boiler_11", "boiler_12", "boiler_13", "boiler_14", "boiler_15", "boiler_16",
                "boiler_17", "boiler_18", "boiler_19", "boiler_20", "boiler_21", "boiler_22", "boiler_23", "boiler_24",
                "boiler_25", "boiler_26", "boiler_27", "boiler_28", "boiler_29", "boiler_30", "boiler_31"});
        SOUND_MAP.put("sluice", new String[] {null, null, "sluice_idle", "sluice_processing"});
        
        SOUND_MAP.put("crusher", new String[] {null, null, "crusher_idle", "crusher_processing"});
        SOUND_MAP.put("MultiTileEntityEngineSteam", new String[] {null, "steam_engine_active", null, null});
        SOUND_MAP.put("MultiTileEntityGeneratorHotFluid", new String[] {null, "heat_exchanger_active", "heat_exchanger_active", null});
        SOUND_MAP.put("MultiTileEntityAutoToolHammer", new String[] {null, null, null, null});
        SOUND_MAP.put("compressor", new String[] {null, null, "compressor_idle", "compressor_processing"});
        SOUND_MAP.put("cutter", new String[] {null, null, "cutter_idle", "cutter_processing"});
        SOUND_MAP.put("MultiTileEntityLocker", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityReactorCore2x2", new String[] {null, "reactor_active", null, null});
        SOUND_MAP.put("MultiTileEntityGeigerCounter", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityTransformerRotation", new String[] {null, "transform_gearbox_active", null, null});
        SOUND_MAP.put("rollingmill", new String[] {null, null, "rolling_mill_idle", "rolling_mill_processing"});
        SOUND_MAP.put("rollformer", new String[] {null, null, "rolling_mill_idle", "rolling_mill_processing"});
        SOUND_MAP.put("rollbender", new String[] {null, null, "rolling_bender_idle", "rolling_bender_processing"});
        SOUND_MAP.put("lathe", new String[] {null, null, "lathe_idle", "lathe_processing"});
        SOUND_MAP.put("MultiTileEntityPump", new String[] {null, "pump_active", "pump_stall", null});
        SOUND_MAP.put("wiremill", new String[] {null, null, "wiremill_idle", "wiremill_processing"});
        SOUND_MAP.put("mixer", new String[] {null, null, "mixer_idle", "mixer_processing"});
        SOUND_MAP.put("loom", new String[] {null, null, "loom_idle", "loom_processing"});
        SOUND_MAP.put("sharpener", new String[] {null, null, "sharpener_idle", "sharpener_processing"});
        SOUND_MAP.put("burnmixer", new String[] {null, null, "burnmixer_idle", "burnmixer_processing"});
        SOUND_MAP.put("pressurewasher", new String[] {null, null, "pressurewasher_idle", "pressurewasher_processing"});
        SOUND_MAP.put("MultiTileEntityEngineRotation", new String[] {null, "rotation_engine_active", "rotation_engine_stall", null});
        SOUND_MAP.put("press", new String[] {null, null, "press_idle", "press_processing"});
        SOUND_MAP.put("squeezer", new String[] {null, null, "squeezer_idle", "squeezer_processing"});
        SOUND_MAP.put("clustermill", new String[] {null, null, "clustermill_idle", "clustermill_processing"});
        SOUND_MAP.put("mc.recipe.furnace", new String[] {null, null, "oven_idle", "oven_processing"});
        SOUND_MAP.put("smelter", new String[] {null, null, "oven_idle", "smelter_processing"});
        SOUND_MAP.put("melter", new String[] {null, null, "oven_idle", "smelter_processing"});
        SOUND_MAP.put("MultiTileEntityGeneratorFluidBed", new String[] {null, "burning_fluidized", null, null});
        SOUND_MAP.put("sifter", new String[] {null, null, "sifter_idle", "sifter_processing"});
        SOUND_MAP.put("roaster", new String[] {null, null, "oven_idle", "oven_processing"});

        SOUND_MAP.put("MultiTileEntityDynamoElectric", new String[] {null, "dynamo_active", "dynamo_stall", null});
        SOUND_MAP.put("MultiTileEntityBatteryBox", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityMagnetElectric", new String[] {null, null, null, null});
        SOUND_MAP.put("polarizer", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityBatteryEU128", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityBatteryAdvEU512", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityBatteryAdvEU128", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityTransformerElectric", new String[] {null, "transformer_active", "transformer_stall", null});
        SOUND_MAP.put("MultiTileEntityBatteryEU512", new String[] {null, null, null, null});
        SOUND_MAP.put("MultiTileEntityWireElectric", new String[] {null, null, null, null});
        SOUND_MAP.put("electrolyzer", new String[] {null, null, "electrolyzer_idle", "electrolyzer_processing"});





    }

    static String[] debugNameList = {"MultiTileEntityBush", "MultiTileEntityPipeFluid", "MultiTileEntityMotorLiquid",
    "MultiTileEntityBumbleHive", "MultiTileEntityResinHoleRubber", "MultiTileEntityFluidTap", "bath", "MultiTileEntityCokeOven",
    "MultiTileEntityGeneratorGas", "MultiTileEntityGeneratorLiquid", "MultiTileEntityGeneratorMetal", "MultiTileEntityBarrelMetal",
    "MultiTileEntitySmeltery", "distillery", "centrifuge", "MultiTileEntityGearBox", "MultiTileEntityAxle","MultiTileEntityMortar",
    "MultiTileEntityMold", "MultiTileEntityMixingBowlTable", "MultiTileEntityFluidFunnel", "MultiTileEntityBarrelWood",
    "MultiTileEntitySafeMechanical", "MultiTileEntityBottleCrate", "MultiTileEntityAdvancedCraftingTable", "MultiTileEntityDrawerQuad",
    "MultiTileEntityAnvil", "MultiTileEntityGrindStone", "MultiTileEntityBathingPotTable", "MultiTileEntityMassStorageStandard",
    "MultiTileEntityBookShelf", "MultiTileEntityCrank", "MultiTileEntitySafeKeyLocked", "MultiTileEntityPipeItem", "shredder",
    "MultiTileEntityHopper", "MultiTileEntityTank3x3x3Metal", "drying", "MultiTileEntityBoilerTank", "sluice",
    "MultiTileEntityGeneratorHotFluid", "MultiTileEntityTurbineSteam", "crusher", "MultiTileEntityEngineSteam",
    "MultiTileEntityAutoToolHammer", "compressor", "MultiTileEntityLocker", "MultiTileEntityGeigerCounter", "cutter", "lathe",
    "MultiTileEntityReactorCore2x2", "rollingmill", "MultiTileEntityPump", "MultiTileEntityTransformerRotation", "rollbender",
    "mixer", "loom", "sharpener", "burnmixer", "pressurewasher", "rollformer", "press", "squeezer", "MultiTileEntityEngineRotation",
    "MultiTileEntityGeneratorBrick", "mc.recipe.furnace", "MultiTileEntityGeneratorFluidBed", "sifter", "roaster", "MultiTileEntityDynamoElectric",
    "MultiTileEntityBatteryEU512", "MultiTileEntityBatteryAdvEU128", "MultiTileEntityWireElectric", "MultiTileEntityTransformerElectric",
    "MultiTileEntityBatteryEU128", "MultiTileEntityBatteryBox", "electrolyzer"};

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

            String mName = te.getClass().getSimpleName();
            if (mName.equals("MultiTileEntityBasicMachine") || mName.equals(("MultiTileEntityBasicMachineElectric"))) {
                mName = ((MultiTileEntityBasicMachine) (TileEntity)te).mRecipes.toString().replace("gt.recipe.", "");
            }
            String desiredKey = null;
            int mState = getTileEntityState(te);




            if (mState != -1 && !isOutOfRange) {
                if (!debugNameIgnoreListContains(mName)){//TODO for debugging, remove

                    System.out.println(mName);
                    System.out.println(mState);
                }

                desiredKey = resolve(mName, mState);
                if (desiredKey != null) {
                    //System.out.println(desiredKey); //TODO remove
                }
            }

            ActiveSound active = activeSounds.get(te);
            if (active != null){
                int currentToken = teSoundTokens.getOrDefault(te, 0);
                if (active.token != currentToken) {
                    stopSound(te);
                    active = null;
                }
            }

            boolean isSoundPlaying = active != null && mc.getSoundHandler().isSoundPlaying(active.loop);

            //fix out of range audio not restarting
            if (isOutOfRange || desiredKey == null) {
                teSoundTokens.put(te, teSoundTokens.getOrDefault(te, 0) + 1);
                if (active != null) stopSound(te);
                continue;
            }

            if (!isSoundPlaying) {
                if(active != null) stopSound(te);
                playLoop(PREFIX + desiredKey, te, mState);
            } else if (!desiredKey.equals(active.key)) {
                teSoundTokens.put(te, teSoundTokens.getOrDefault(te, 0) + 1);
                stopSound(te);
                playLoop(PREFIX + desiredKey, te, mState);
            } else {
                active.loop.update();
            }
        }
        cleanupInvalidTiles();
    }

    private void playLoop(String keyString, TileEntity te, int mState) {
        int newToken = teSoundTokens.getOrDefault(te, 0) + 1;
        teSoundTokens.put(te, newToken);

        SoundLoop loop = new SoundLoop(keyString, te, newToken, mState);
        loop.setRepeat(true);
        loop.setVolume(1.0f);
        //loop.setPitch(0.85f + (float)Math.random() * 0.30f);
        loop.setPitch(0.85f);
        String shortKey = keyString.substring(PREFIX.length());
        activeSounds.put(te, new ActiveSound(loop, shortKey, newToken));
        mc.getSoundHandler().playSound(loop);
    }

    private void stopSound(TileEntity te) {
        ActiveSound active = activeSounds.remove(te);
        if(active != null) mc.getSoundHandler().stopSound(active.loop);
        teSoundTokens.put(te, teSoundTokens.getOrDefault(te, 0) + 1);
    }

    private String resolve(String tileEnt, int state) {
        String[] mSound = SOUND_MAP.get(tileEnt);
        if (mSound == null) {
            System.out.println("WARNING: No sound map for: " + tileEnt);
            return null;
        }
        if (state < 0 || state >= mSound.length) return null;
        return mSound[state];
    }

    private void cleanupInvalidTiles() {
        activeSounds.entrySet().removeIf(entry -> {
            TileEntity te = entry.getKey();
            TileEntity worldTE = te.getWorldObj().getTileEntity(te.xCoord, te.yCoord, te.zCoord);
            if (te.isInvalid() || te.getWorldObj() == null || worldTE != te || entry.getValue().loop.isDonePlaying()) {
                mc.getSoundHandler().stopSound(entry.getValue().loop);
                teSoundTokens.put(te, teSoundTokens.getOrDefault(te, 0) + 1);
                return true;
            }
            return false;
        });
    }

    private boolean debugNameIgnoreListContains(String name) {
        for (String s : debugNameList) {
            if (s.equals(name)) return true;
        }
        return false;
    }

    private static class ActiveSound {
        public final SoundLoop loop;
        public String key;
        public final int token;

        public ActiveSound(SoundLoop loop, String key, int token) {
            this.loop = loop;
            this.key = key;
            this.token = token;
        }
    }

    public static int getTileEntityState(@NotNull TileEntity te) {
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
                mState = ((Number) getVisualData.invoke(te)).byteValue();
            } catch (Throwable ignored) {}
        }

        /*
        if (mName.equals("MultiTileEntityBasicMachine") ) {
            mName = ((MultiTileEntityBasicMachine) (TileEntity)te).mRecipes.toString().replace("gt.recipe.", "");
        }

        if (mName.equals("MultiTileEntityBasicMachineElectric") ) {
            MultiTileEntityBasicMachine me = (MultiTileEntityBasicMachine) (TileEntity) te;
            String electricMachineName = me.mRecipes.toString().replace("gt.recipe.", "");
            System.out.println(electricMachineName);
            mName = electricMachineName;
        }

         */

        if (mName.equals("MultiTileEntityGearBox")) {
            int mRotationData = ((MultiTileEntityGearBox) (TileEntity)te).mRotationData;
            mState = (byte) (mRotationData & B[6]) != 0 ? 1 : 0;
        }

        if (mName.equals("MultiTileEntityBoilerTank")) {
            MultiTileEntityBoilerTank be = (MultiTileEntityBoilerTank) (TileEntity) te;
            try{
                Field barometer = MultiTileEntityBoilerTank.class.getDeclaredField("mBarometer");
                barometer.setAccessible(true);
                Object getBarometer = barometer.get(be);
                mState = (int) getBarometer;
            } catch (Throwable ignored){}
        }

        if (mName.equals("MultiTileEntityEngineSteam")) {
            MultiTileEntityEngineSteam es = (MultiTileEntityEngineSteam) (TileEntity) te;
            try {
                Field active = MultiTileEntityEngineSteam.class.getDeclaredField("mActive");
                active.setAccessible(true);
                Object getStopped = active.get(es);
                mState = (boolean)getStopped ? 1 : 0;
            } catch (Throwable ignored){}
        }

        if (mName.equals("MultiTileEntityTransformerRotation")) {
            MultiTileEntityTransformerRotation gb = (MultiTileEntityTransformerRotation) (TileEntity) te;
            mState = gb.mActivity.mState == 1 ? 1 : 0;
        }

        if (mName.equals("MultiTileEntityReactorCore2x2")) {
            MultiTileEntityReactorCore es = (MultiTileEntityReactorCore) (TileEntity) te;
            try {
                Field active = MultiTileEntityReactorCore.class.getDeclaredField("mStopped");
                active.setAccessible(true);
                Object getStopped = active.get(es);
                mState = (boolean)getStopped ? 0 : 1;
            } catch (Throwable ignored){}
        }

        if (mName.equals("MultiTileEntitySmeltery")) {
            MultiTileEntitySmeltery se = (MultiTileEntitySmeltery) (TileEntity) te;
            try {
                Field meltDown = MultiTileEntitySmeltery.class.getDeclaredField("mMeltDown");
                meltDown.setAccessible(true);
                Object isMeltdown = meltDown.get(se);
                mState = (boolean)isMeltdown ? 1 : 0;
            } catch (Throwable ignored){}
        }
        return mState;
    }
}
