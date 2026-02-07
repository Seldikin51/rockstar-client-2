package moscow.rockstar.systems.test;

import lombok.Generated;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL33C;

public final class DrawCallRainTest {
   private static int vao;
   private static int vbo;
   private static int shaderProgram;

   public static void init() {
      String vertexShader = "#version 330 core\nlayout (location = 0) in vec3 aPos;\nvoid main() {\n    gl_Position = vec4(aPos, 1.0);\n}\n";
      String fragmentShader = "#version 330 core\nout vec4 FragColor;\nvoid main() {\n    FragColor = vec4(0.0, 1.0, 0.0, 1.0); // Зеленый\n}\n";
      int vs = GL33C.glCreateShader(35633);
      GL33C.glShaderSource(vs, vertexShader);
      GL33C.glCompileShader(vs);
      int fs = GL33C.glCreateShader(35632);
      GL33C.glShaderSource(fs, fragmentShader);
      GL33C.glCompileShader(fs);
      shaderProgram = GL33C.glCreateProgram();
      GL33C.glAttachShader(shaderProgram, vs);
      GL33C.glAttachShader(shaderProgram, fs);
      GL33C.glLinkProgram(shaderProgram);
      float[] vertices = new float[]{-0.5F, -0.5F, 0.0F, 0.5F, -0.5F, 0.0F, 0.5F, 0.5F, 0.0F, -0.5F, 0.5F, 0.0F};
      vao = GL33C.glGenVertexArrays();
      vbo = GL33C.glGenBuffers();
      GL33C.glBindVertexArray(vao);
      GL33C.glBindBuffer(34962, vbo);
      GL33C.glBufferData(34962, vertices, 35044);
      GL33C.glVertexAttribPointer(0, 3, 5126, false, 12, 0L);
      GL33C.glEnableVertexAttribArray(0);
      GL33C.glBindBuffer(34962, 0);
      GL33C.glBindVertexArray(0);
   }

   public static void renderSquare(MatrixStack matrices) {
      GL33C.glUseProgram(shaderProgram);
      GL33C.glBindVertexArray(vao);
      GL33C.glDrawArrays(6, 0, 4);
      GL33C.glBindVertexArray(0);
      GL33C.glUseProgram(0);
   }

   @Generated
   private DrawCallRainTest() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
