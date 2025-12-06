package gregapi.audio;

import gregapi.audio.handlers.GTSoundTickHandler;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class SoundLoop extends MovingSound {

    public final String key;
    private final TileEntity source;
    private final int token;
    private final int expectedState;


    public SoundLoop(String soundKey, TileEntity te, int token, int expectedState) {
        super(new ResourceLocation(soundKey));
        this.key = soundKey;
        this.source = te;
        this.token = token;
        this.expectedState = expectedState;

        this.repeat = true;          // loop
        this.field_147665_h = 0;     // repeatDelay
        this.volume = 0.45F;         // volume
        this.field_147663_c = 0.5F;  // pitch
        this.xPosF = te.xCoord + 0.5f;
        this.yPosF = te.yCoord + 0.5f;
        this.zPosF = te.zCoord + 0.5f;
        this.field_147666_i = ISound.AttenuationType.LINEAR;
    }

    public String getKey() {return key;}

    @Override
    public void update() {
        if (source == null || source.isInvalid() || source.getWorldObj() == null) {
            this.donePlaying = true;
            return;
        }

        TileEntity atPos = source.getWorldObj().getTileEntity(source.xCoord, source.yCoord, source.zCoord);
        if (atPos != source) {
            this.donePlaying = true;
            return;
        }

        int currentState = GTSoundTickHandler.getTileEntityState(source);
        if(currentState != expectedState) {
            this.donePlaying = true;
            return;
        }

        this.xPosF = source.xCoord + 0.5f;
        this.yPosF = source.yCoord + 0.5f;
        this.zPosF = source.zCoord + 0.5f;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPitch(float pitch) {
        this.field_147663_c = pitch;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}
