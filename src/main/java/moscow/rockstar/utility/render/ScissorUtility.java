package moscow.rockstar.utility.render;

import java.util.ArrayDeque;
import java.util.Deque;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IWindow;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MatrixUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public final class ScissorUtility implements IWindow {
   private static final Deque<ScissorUtility.ScissorRect> scissorStack = new ArrayDeque<>();

   private static void applyScissor(ScissorUtility.ScissorRect rect) {
      int height = mw.getFramebufferHeight();
      double scaleFactor = mw.getScaleFactor();
      float left = rect.x * (float)scaleFactor;
      float top = rect.y * (float)scaleFactor;
      float right = (rect.x + rect.w) * (float)scaleFactor;
      float bottom = (rect.y + rect.h) * (float)scaleFactor;
      int x = (int)Math.floor(left);
      int y = (int)Math.floor(height - Math.ceil(bottom) + 0.5);
      int w = (int)Math.max(0.0F, (float)((int)Math.ceil(right) - x));
      int h = (int)Math.max(0.0F, (int)Math.ceil(bottom) - (int)Math.floor(top) - 1.0F);
      GL11.glEnable(3089);
      GL11.glScissor(x, y, w, h);
   }

   public static void push(float x, float y, float width, float height) {
      ScissorUtility.ScissorRect rect = new ScissorUtility.ScissorRect(x, y, width, height);
      push(rect);
   }

   public static void push(MatrixStack stack, float x, float y, float width, float height) {
      Matrix4f matrix = stack.peek().getPositionMatrix();
      ScissorUtility.ScissorRect rect = new ScissorUtility.ScissorRect(x, y, width, height).transformRect(matrix);
      push(rect);
   }

   public static void push(Matrix4f transformationMatrix, float x, float y, float width, float height) {
      ScissorUtility.ScissorRect rect = new ScissorUtility.ScissorRect(x, y, width, height);
      if (transformationMatrix != null) {
         rect = rect.transformRect(transformationMatrix);
      }

      push(rect);
   }

   private static void push(ScissorUtility.ScissorRect rect) {
      if (!scissorStack.isEmpty()) {
         rect = intersectRects(scissorStack.peek(), rect);
      }

      scissorStack.push(rect);
      applyScissor(rect);
   }

   public static void pop() {
      if (!scissorStack.isEmpty()) {
         scissorStack.pop();
      }

      if (!scissorStack.isEmpty()) {
         applyScissor(scissorStack.peek());
      } else {
         GL11.glDisable(3089);
      }
   }

   private static ScissorUtility.ScissorRect intersectRects(ScissorUtility.ScissorRect a, ScissorUtility.ScissorRect b) {
      float left = Math.max(a.left(), b.left());
      float top = Math.max(a.top(), b.top());
      float right = Math.min(a.right(), b.right());
      float bottom = Math.min(a.bottom(), b.bottom());
      float width = Math.max(0.0F, right - left);
      float height = Math.max(0.0F, bottom - top);
      return new ScissorUtility.ScissorRect(left, top, width, height);
   }

   public static boolean isScissorEnabled() {
      return !scissorStack.isEmpty();
   }

   public static void clear() {
      scissorStack.clear();
      GL11.glDisable(3089);
   }

   public static int getStackSize() {
      return scissorStack.size();
   }

   @Deprecated
   public static void startScissor(float x, float y, float width, float height) {
      push(x, y, width, height);
   }

   @Deprecated
   public static void startScissor(float x, float y, float width, float height, MatrixStack matrices) {
      if (matrices != null) {
         push(matrices, x, y, width, height);
      } else {
         push(x, y, width, height);
      }
   }

   @Deprecated
   public static void startScissor(float x, float y, float width, float height, Matrix4f transformationMatrix) {
      push(transformationMatrix, x, y, width, height);
   }

   @Deprecated
   public static void stopScissor() {
      pop();
   }

   @Generated
   private ScissorUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static class ScissorRect {
      final float x;
      final float y;
      final float w;
      final float h;

      ScissorRect(float x, float y, float w, float h) {
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
      }

      float left() {
         return this.x;
      }

      float top() {
         return this.y;
      }

      float right() {
         return this.x + this.w;
      }

      float bottom() {
         return this.y + this.h;
      }

      private ScissorUtility.ScissorRect transformRect(Matrix4f matrix) {
         if (MatrixUtil.isIdentity(matrix)) {
            return new ScissorUtility.ScissorRect(this.x, this.y, this.w, this.h);
         } else {
            Vector3f v1 = new Vector3f(this.x, this.y, 0.0F);
            Vector3f v2 = new Vector3f(this.x + this.w, this.y + this.h, 0.0F);
            matrix.transformPosition(v1);
            matrix.transformPosition(v2);
            return new ScissorUtility.ScissorRect(v1.x, v1.y, v2.x - v1.x, v2.y - v1.y);
         }
      }
   }
}
