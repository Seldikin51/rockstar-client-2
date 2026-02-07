package moscow.rockstar.utility.game.cursor;

import lombok.Generated;

public final class CursorUtility {
   private static CursorType currentType = CursorType.DEFAULT;
   private static CursorType prev = CursorType.HAND;

   public static void set(CursorType type) {
      currentType = type;
   }

   @Generated
   private CursorUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static CursorType getCurrentType() {
      return currentType;
   }

   @Generated
   public static CursorType getPrev() {
      return prev;
   }

   @Generated
   public static void setPrev(CursorType prev) {
      CursorUtility.prev = prev;
   }
}
