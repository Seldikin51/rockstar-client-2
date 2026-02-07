package moscow.rockstar.utility.sounds;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstance.AttenuationType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class ClientSoundInstance extends PositionedSoundInstance {
   private static final float DEFAULT_PITCH = 1.0F;
   private final String fileName;

   public ClientSoundInstance(String fileName, float volume) {
      super(
         Identifier.of(Rockstar.MOD_ID + ":" + fileName),
         SoundCategory.MASTER,
         volume,
         1.0F,
         SoundInstance.createRandom(),
         false,
         0,
         AttenuationType.NONE,
         0.0,
         0.0,
         0.0,
         true
      );
      this.fileName = fileName;
   }

   public ClientSoundInstance(String fileName, float volume, float pitch) {
      super(
         Identifier.of(Rockstar.MOD_ID + ":" + fileName),
         SoundCategory.MASTER,
         volume,
         pitch,
         SoundInstance.createRandom(),
         false,
         0,
         AttenuationType.NONE,
         0.0,
         0.0,
         0.0,
         true
      );
      this.fileName = fileName;
   }

   public void play(float volume) {
      MinecraftClient.getInstance().getSoundManager().play(new ClientSoundInstance(this.fileName, volume));
   }

   public void play(float volume, float pitch) {
      MinecraftClient.getInstance().getSoundManager().play(new ClientSoundInstance(this.fileName, volume, pitch));
   }

   @Generated
   public String getFileName() {
      return this.fileName;
   }
}
