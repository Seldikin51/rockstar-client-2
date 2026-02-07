package moscow.rockstar.framework.msdf;

import lombok.Generated;
import net.minecraft.text.Text;

public class Font {
   private MsdfFont font;
   private float size;

   public float height() {
      return this.size * 0.7F;
   }

   public float width(String text) {
      return this.font.getWidth(text, this.size);
   }

   public float width(Text text) {
      return this.font.getTextWidth(text, this.size);
   }

   @Generated
   public MsdfFont getFont() {
      return this.font;
   }

   @Generated
   public float getSize() {
      return this.size;
   }

   @Generated
   public Font(MsdfFont font, float size) {
      this.font = font;
      this.size = size;
   }
}
