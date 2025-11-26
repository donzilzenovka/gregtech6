package gregapi.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class SoundLoop extends PositionedSound implements ISound {

    public final String key;
    private final TileEntity source;

    public SoundLoop(String soundKey, TileEntity te) {
        super(new ResourceLocation(soundKey));
        this.key = soundKey;
        this.source = te;
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

    public void updatePosition() {
        if (source != null && !source.isInvalid()) {
            this.xPosF = source.xCoord + 0.5f;
            this.yPosF = source.yCoord + 0.5f;
            this.zPosF = source.zCoord + 0.5f;
        }
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
