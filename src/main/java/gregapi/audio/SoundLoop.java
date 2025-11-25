package gregapi.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundLoop extends PositionedSound implements ISound {

    public SoundLoop(String soundKey, TileEntity te) {
        super(new ResourceLocation(soundKey));
        this.repeat = true;          // loop
        this.field_147665_h = 0;     // delay between loops
        this.volume = 0.45F;         // subtle volume
        this.field_147663_c = 0.5F;  // pitch
        this.xPosF = te.xCoord;           // positional sound X (can stay 0)
        this.yPosF = te.yCoord;           // positional sound Y
        this.zPosF = te.zCoord;           // positional sound Z
        this.field_147666_i = ISound.AttenuationType.NONE; // no positional attenuation
    }
}
