package moscow.rockstar.ui.components.textfield;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.sounds.ClientSounds;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;

public class TextField extends CustomComponent implements IMinecraft {
   public static TextField LAST_FIELD;
   private final HashMap<Character, Float> charSoundCache = new HashMap<>();
   private final List<TextField.TypedText> texts = new ArrayList<>();
   private final Font font;
   private String lastBuilt = "";
   private String builtText = "";
   private final Animation focusing = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   private boolean focused;
   private final Animation cursorAnim = new Animation(300L, 0.0F, Easing.BAKEK);
   private int cursor;
   private TextField.Selection selection;
   private float startX = 0.0F;
   private float endX = 0.0F;
   private int drag = -1;
   private long lastClickTime = 0L;
   private int clickCount = 0;
   private final Timer typingTimer = new Timer();
   private String preview = "";
   private String icon = "";
   private Map<String, FieldAction> append = new HashMap<>();
   private String appending = "";
   private float xPos;
   private final Timer moveTimer = new Timer();
   private float alpha = 1.0F;
   private ColorRGBA textColor = ColorRGBA.WHITE;

   @Override
   protected void renderComponent(UIContext context) {
      float offset = 0.0F;
      float cleanOffset = 0.0F;
      float cursorOffset = 0.0F;
      float fontOffset = this.height / 2.0F - this.font.height() / 2.0F;
      float cursorWidth = this.font.height() / 8.0F;
      this.texts.removeIf(textx -> textx.showing.getValue() == 0.0F && textx.removing);
      this.focusing.update(this.focused);
      if (this.selection != null && this.selection.getStart() == this.selection.getEnd()) {
         this.selection = null;
      }

      if (this.drag != -1) {
         this.typingTimer.reset();
         int current = -1;
         float v = 0.0F;

         for (TextField.TypedText typedText : this.texts) {
            String text = String.valueOf(typedText.type);
            if (context.getMouseX() < this.x + this.xPos + v + this.font.width(text) + this.font.width(text) / 2.0F) {
               current = this.texts.indexOf(typedText);
               break;
            }

            v += this.font.width(text);
         }

         if (current == -1) {
            current = this.texts.size();
         }

         if (current != this.drag) {
            this.selection = new TextField.Selection(current > this.drag, Math.min(this.drag, current), Math.max(this.drag, current));
            this.cursor = current;
         } else {
            if (this.selection != null) {
               this.cursor = this.selection.getStart();
            }

            this.selection = null;
         }
      }

      if (this.isHovered(context)) {
         CursorUtility.set(CursorType.TEXT);
      }

      this.updateAppend();
      ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
      context.drawRect(
         this.x + this.xPos + fontOffset + this.startX,
         this.y + fontOffset - 1.0F,
         this.endX - this.startX,
         this.font.height() + 2.0F,
         ColorRGBA.BLUE.mix(new ColorRGBA(76.0F, 99.0F, 122.0F), 0.7F).withAlpha(255.0F * this.focusing.getValue())
      );
      this.startX = 0.0F;
      this.endX = 0.0F;
      Batching fontBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, this.font.getFont());
      if (this.texts.isEmpty()) {
         context.drawText(
            this.font,
            this.preview,
            this.x + offset + fontOffset,
            this.y + fontOffset - 2.0F * this.focusing.getValue(),
            this.textColor.mulAlpha(0.75F * (1.0F - this.focusing.getValue()))
         );
      }

      if (!this.appending.isEmpty() && this.appending.toLowerCase().startsWith(this.builtText.toLowerCase()) && !this.builtText.isEmpty()) {
         context.drawText(
            this.font,
            this.builtText + this.appending.substring(this.builtText.length()),
            this.x + offset + fontOffset,
            this.y + fontOffset,
            this.textColor.withAlpha(150.0F * this.focusing.getValue())
         );
      }

      for (TextField.TypedText typedText : this.texts) {
         String text = String.valueOf(typedText.type);
         typedText.showing.setDuration(200L);
         typedText.showing.update(!typedText.removing);
         context.drawText(
            this.font,
            text,
            this.x + offset + fontOffset + this.xPos,
            this.y + fontOffset + 2.0F - 2.0F * typedText.showing.getValue(),
            this.textColor.withAlpha(255.0F * typedText.showing.getValue())
         );
         offset += this.font.width(text) * typedText.showing.getValue();
         cleanOffset += this.font.width(text);
         if (this.texts.indexOf(typedText) == this.cursor - 1) {
            cursorOffset = cleanOffset;
         }

         if (this.selection != null) {
            if (this.texts.indexOf(typedText) == this.selection.getStart() - 1) {
               this.startX = cleanOffset;
            }

            if (this.texts.indexOf(typedText) == this.selection.getEnd() - 1) {
               this.endX = cleanOffset;
            }
         }
      }

      fontBatching.draw();
      cursorOffset += this.cursor == this.texts.size() ? 1 : 0;
      if (this.moveTimer.finished(10L)) {
         for (TextField.TypedText typedText : this.texts) {
            String textx = String.valueOf(typedText.type);
            if (cursorOffset + fontOffset + this.xPos > this.width - 5.0F) {
               this.xPos = this.xPos - this.font.width(textx);
               this.moveTimer.reset();
               break;
            }

            if (cursorOffset + fontOffset + this.xPos < 5.0F) {
               this.xPos = this.xPos + this.font.width(textx);
               this.moveTimer.reset();
               break;
            }
         }

         if (this.font.width(this.builtText) < this.width - 10.0F) {
            this.xPos = 0.0F;
         }
      }

      this.cursorAnim.setEasing(Easing.BAKEK_SMALLER);
      this.cursorAnim.update(cursorOffset);
      RenderUtility.rotate(
         context.getMatrices(),
         this.x + fontOffset + this.xPos + this.cursorAnim.getValue() + cursorWidth / 2.0F,
         this.y + fontOffset - 1.0F,
         Math.clamp(cursorOffset - this.cursorAnim.getValue(), -20.0F, 20.0F)
      );
      context.drawRect(
         this.x + fontOffset + this.cursorAnim.getValue() + this.xPos,
         this.y + fontOffset - 1.0F,
         cursorWidth,
         this.font.height() + 2.0F,
         this.textColor
            .withAlpha(
               (float)(
                  200.0F * this.focusing.getValue()
                     * (!this.typingTimer.finished(300L) ? 3.0 : MathUtility.sin(System.currentTimeMillis() / 200.0) + 2.0)
                     / 3.0
               )
            )
      );
      RenderUtility.end(context.getMatrices());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      ScissorUtility.pop();
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      if (this.isHovered(mouseX, mouseY)) {
         if (button == MouseButton.LEFT) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastClickTime < 500L) {
               this.clickCount++;
            } else {
               this.clickCount = 1;
            }

            this.lastClickTime = currentTime;
            this.focused = true;
            float offset = 0.0F;
            int newCursor = this.texts.size();

            for (TextField.TypedText typedText : this.texts) {
               String text = String.valueOf(typedText.type);
               if (mouseX < this.x + this.xPos + offset + this.font.width(text) + this.font.width(text) / 2.0F) {
                  newCursor = this.texts.indexOf(typedText);
                  break;
               }

               offset += this.font.width(text);
            }

            this.cursor = newCursor;
            if (this.clickCount == 2) {
               this.selectWordAtCursor();
               this.drag = -1;
            } else {
               this.selection = null;
               this.drag = this.cursor;
            }
         }
      } else {
         this.focused = false;
      }
   }

   @Override
   public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
      this.drag = -1;
   }

   @Override
   public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.focused) {
         if ((keyCode == 259 || keyCode == 261) && this.selection != null) {
            this.clearSelection();
            ClientSounds.TYPING.play(0.3F, 1.2F);
            this.moveCursor(0);
         } else if (keyCode == 259 && this.cursor > 0) {
            int offset = Screen.hasControlDown() ? Math.max(1, this.getWordSize(false)) : 1;

            for (int i = 0; i < offset; i++) {
               this.moveCursor(-1);
               TextField.TypedText last = null;

               for (TextField.TypedText text : this.texts) {
                  if (!text.removing) {
                     last = text;
                  }

                  if (this.texts.indexOf(text) == this.cursor) {
                     break;
                  }
               }

               if (last != null) {
                  last.removing = true;
               }
            }

            ClientSounds.TYPING.play(0.3F, 1.2F);
         } else if (keyCode == 261 && this.cursor < this.texts.size()) {
            int offset = Screen.hasControlDown() ? Math.max(1, this.getWordSize(true)) : 1;

            for (int i = 0; i < offset && this.cursor < this.texts.size(); i++) {
               for (int j = this.cursor; j < this.texts.size(); j++) {
                  TextField.TypedText text = this.texts.get(j);
                  if (!text.removing) {
                     text.removing = true;
                     break;
                  }
               }
            }

            this.moveCursor(0);
            ClientSounds.TYPING.play(0.3F, 1.2F);
         } else if (keyCode == 263) {
            ClientSounds.TYPING.play(0.3F, 1.3F);
            int offset = Screen.hasControlDown() ? Math.max(1, this.getWordSize(false)) : 1;
            if (Screen.hasShiftDown()) {
               this.select(-offset);
            } else if (this.selection != null) {
               this.cursor = this.selection.getStart();
               this.selection = null;
               return;
            }

            this.moveCursor(-offset);
         } else if (keyCode == 262) {
            ClientSounds.TYPING.play(0.3F, 1.3F);
            int offset = Screen.hasControlDown() ? Math.max(1, this.getWordSize(true)) : 1;
            if (Screen.hasShiftDown()) {
               this.select(offset);
            } else if (this.selection != null) {
               this.cursor = this.selection.getEnd();
               this.selection = null;
               return;
            }

            this.moveCursor(offset);
         } else if (Screen.isSelectAll(keyCode)) {
            this.selection = new TextField.Selection(true, 0, this.texts.size());
         } else if (Screen.isCopy(keyCode)) {
            if (this.selection != null) {
               mc.keyboard.setClipboard(this.getSelectedText());
               return;
            }

            mc.keyboard.setClipboard(this.builtText);
         } else if (Screen.isCut(keyCode)) {
            if (this.selection != null) {
               mc.keyboard.setClipboard(this.getSelectedText());
               this.clearSelection();
               this.moveCursor(0);
               return;
            }

            mc.keyboard.setClipboard(this.builtText);

            for (TextField.TypedText text : this.texts) {
               text.removing = true;
            }

            this.builtText = "";
         } else if (Screen.isPaste(keyCode)) {
            this.paste(mc.keyboard.getClipboard());
         } else if (keyCode == 258 || keyCode == 257) {
            for (Entry<String, FieldAction> sugg : this.append.entrySet()) {
               if (sugg.getKey().toLowerCase().startsWith(this.builtText.toLowerCase()) && !this.builtText.isEmpty() && sugg.getValue() != null) {
                  this.clear();
                  if (keyCode == 257) {
                     sugg.getValue().getEnter().run();
                  } else {
                     sugg.getValue().getTab().run();
                  }

                  this.focused = false;
                  return;
               }
            }

            if (keyCode == 257) {
               this.focused = false;
            }
         } else if (keyCode == 259 && this.texts.isEmpty()) {
            this.focused = false;
         }
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      if (!this.focused) {
         return false;
      } else {
         if (chr == ' ') {
            ClientSounds.TYPING.play(0.3F, 0.8F);
         } else {
            if (!this.charSoundCache.containsKey(chr)) {
               this.charSoundCache.put(chr, MathUtility.random(0.8, 1.2));
            }

            ClientSounds.TYPING.play(0.3F, this.charSoundCache.get(chr));
         }

         this.typeChar(chr);
         return true;
      }
   }

   private void updateAppend() {
      this.lastBuilt = this.builtText;
      StringBuilder builder = new StringBuilder();

      for (TextField.TypedText text : this.texts) {
         builder.append(text.type);
      }

      this.builtText = builder.toString();
      if (!this.lastBuilt.equals(this.builtText)) {
         this.appending = "";

         for (String string : this.append.keySet()) {
            if (string.toLowerCase().startsWith(this.builtText.toLowerCase()) && !this.builtText.isEmpty()) {
               this.appending = string;
            }
         }
      }
   }

   public int getWordSize(boolean forward) {
      int counter = 0;
      if (forward) {
         for (int i = this.cursor; i < this.texts.size(); i++) {
            TextField.TypedText text = this.texts.get(i);
            if (text.removing || text.type == ' ') {
               break;
            }

            counter++;
         }
      } else {
         for (int i = this.cursor - 1; i >= 0; i--) {
            TextField.TypedText text = this.texts.get(i);
            if (text.removing || text.type == ' ') {
               break;
            }

            counter++;
         }
      }

      return counter;
   }

   public void paste(String paste) {
      for (char c : paste.toCharArray()) {
         this.typeChar(c);
      }
   }

   public void typeChar(char c) {
      this.clearSelection();
      this.texts.add(Math.clamp((long)this.cursor, 0, Math.max(0, this.texts.size())), new TextField.TypedText(c));
      this.moveCursor(1);
      LAST_FIELD = this;
   }

   private void moveCursor(int offset) {
      this.cursor = MathHelper.clamp(this.cursor + offset, 0, this.texts.size());
      this.typingTimer.reset();
   }

   public void clear() {
      this.texts.clear();
      this.builtText = "";
   }

   private void selectWordAtCursor() {
      if (!this.texts.isEmpty()) {
         int wordStart = this.cursor;
         int wordEnd = this.cursor;

         for (int i = this.cursor - 1; i >= 0; wordStart = i--) {
            TextField.TypedText text = this.texts.get(i);
            if (text.removing || text.type == ' ' || !Character.isLetterOrDigit(text.type)) {
               break;
            }
         }

         for (int ix = this.cursor; ix < this.texts.size(); ix++) {
            TextField.TypedText text = this.texts.get(ix);
            if (text.removing || text.type == ' ' || !Character.isLetterOrDigit(text.type)) {
               break;
            }

            wordEnd = ix + 1;
         }

         if (wordStart != wordEnd) {
            this.selection = new TextField.Selection(true, wordStart, wordEnd);
            this.cursor = wordEnd;
         }
      }
   }

   private void clearSelection() {
      if (this.selection != null) {
         for (TextField.TypedText text : this.getSelected()) {
            text.removing = true;
         }

         this.cursor = this.selection.getStart();
         this.selection = null;
      }
   }

   private List<TextField.TypedText> getSelected() {
      List<TextField.TypedText> typedTexts = new ArrayList<>();
      boolean inSelection = false;

      for (TextField.TypedText text : this.texts) {
         if (this.texts.indexOf(text) == this.selection.getStart()) {
            inSelection = true;
         }

         if (this.texts.indexOf(text) == this.selection.getEnd()) {
            inSelection = false;
         }

         if (inSelection) {
            typedTexts.add(text);
         }
      }

      return typedTexts;
   }

   private String getSelectedText() {
      StringBuilder builder = new StringBuilder();
      boolean inSelection = false;

      for (TextField.TypedText text : this.texts) {
         if (this.texts.indexOf(text) == this.selection.getStart()) {
            inSelection = true;
         }

         if (this.texts.indexOf(text) == this.selection.getEnd()) {
            inSelection = false;
         }

         if (inSelection) {
            builder.append(text.type);
         }
      }

      return builder.toString();
   }

   private void select(int offset) {
      if (this.selection == null) {
         this.selection = new TextField.Selection(offset > 0, this.cursor, this.cursor);
      }

      if (!this.selection.forward) {
         this.selection.start = MathHelper.clamp(this.selection.getStart() + offset, 0, this.texts.size());
      } else {
         this.selection.end = MathHelper.clamp(this.selection.getEnd() + offset, 0, this.texts.size());
      }
   }

   @Generated
   public TextField(Font font) {
      this.font = font;
   }

   @Generated
   public String getBuiltText() {
      return this.builtText;
   }

   @Generated
   public boolean isFocused() {
      return this.focused;
   }

   @Generated
   public void setFocused(boolean focused) {
      this.focused = focused;
   }

   @Generated
   public String getPreview() {
      return this.preview;
   }

   @Generated
   public String getIcon() {
      return this.icon;
   }

   @Generated
   public void setPreview(String preview) {
      this.preview = preview;
   }

   @Generated
   public void setIcon(String icon) {
      this.icon = icon;
   }

   @Generated
   public void setAppend(Map<String, FieldAction> append) {
      this.append = append;
   }

   @Generated
   public String getAppending() {
      return this.appending;
   }

   @Generated
   public void setAlpha(float alpha) {
      this.alpha = alpha;
   }

   @Generated
   public ColorRGBA getTextColor() {
      return this.textColor;
   }

   @Generated
   public void setTextColor(ColorRGBA textColor) {
      this.textColor = textColor;
   }

   static class Selection {
      final boolean forward;
      int start;
      int end;

      int getStart() {
         return Math.min(this.end, this.start);
      }

      int getEnd() {
         return Math.max(this.end, this.start);
      }

      @Generated
      public Selection(boolean forward, int start, int end) {
         this.forward = forward;
         this.start = start;
         this.end = end;
      }
   }

   static class TypedText {
      final Animation showing = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
      boolean removing;
      final char type;

      @Generated
      public TypedText(char type) {
         this.type = type;
      }
   }
}
