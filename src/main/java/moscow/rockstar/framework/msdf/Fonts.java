package moscow.rockstar.framework.msdf;

import lombok.Generated;

public final class Fonts {
   public static final MsdfFont BOLD = MsdfFont.builder().atlas("bold").data("bold").build();
   public static final MsdfFont MEDIUM = MsdfFont.builder().atlas("medium").data("medium").build();
   public static final MsdfFont REGULAR = MsdfFont.builder().atlas("regular").data("regular").build();
   public static final MsdfFont SEMIBOLD = MsdfFont.builder().atlas("semibold").data("semibold").build();
   public static final MsdfFont ROUND_BOLD = MsdfFont.builder().atlas("roundbold").data("roundbold").build();

   @Generated
   private Fonts() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
