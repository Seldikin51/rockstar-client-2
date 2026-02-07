package moscow.rockstar.utility.game.cursor;

import lombok.Generated;
import org.lwjgl.glfw.GLFW;

public enum CursorType {
   DEFAULT(GLFW.glfwCreateStandardCursor(221185)),
   HAND(GLFW.glfwCreateStandardCursor(221188)),
   ARROW_HORIZONTAL(GLFW.glfwCreateStandardCursor(221189)),
   ARROW_VERTICAL(GLFW.glfwCreateStandardCursor(221190)),
   TEXT(GLFW.glfwCreateStandardCursor(221186)),
   CROSSHAIR(GLFW.glfwCreateStandardCursor(221187)),
   BLOCK(GLFW.glfwCreateStandardCursor(221194)),
   RESIZE_ALL(GLFW.glfwCreateStandardCursor(221193));

   private final long code;

   @Generated
   public long getCode() {
      return this.code;
   }

   @Generated
   private CursorType(final long code) {
      this.code = code;
   }
}
