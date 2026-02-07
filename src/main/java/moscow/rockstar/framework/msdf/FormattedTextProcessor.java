package moscow.rockstar.framework.msdf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Generated;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class FormattedTextProcessor {
   public static List<FormattedTextProcessor.TextSegment> processText(Text text, int defaultColor) {
      List<FormattedTextProcessor.TextSegment> segments = new ArrayList<>();
      text.visit((style, string) -> {
         if (!string.isEmpty()) {
            int color = extractColor(style, defaultColor);
            boolean bold = style.isBold();
            boolean italic = style.isItalic();
            boolean underlined = style.isUnderlined();
            boolean strikethrough = style.isStrikethrough();
            segments.add(new FormattedTextProcessor.TextSegment(string, color, bold, italic, underlined, strikethrough));
         }

         return Optional.empty();
      }, Style.EMPTY);
      return segments;
   }

   private static int extractColor(Style style, int defaultColor) {
      TextColor textColor = style.getColor();
      return textColor != null ? textColor.getRgb() | 0xFF000000 : defaultColor;
   }

   public static class TextSegment {
      public final String text;
      public final int color;
      public final boolean bold;
      public final boolean italic;
      public final boolean underlined;
      public final boolean strikethrough;

      @Generated
      public TextSegment(String text, int color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
         this.text = text;
         this.color = color;
         this.bold = bold;
         this.italic = italic;
         this.underlined = underlined;
         this.strikethrough = strikethrough;
      }

      @Generated
      public String getText() {
         return this.text;
      }

      @Generated
      public int getColor() {
         return this.color;
      }

      @Generated
      public boolean isBold() {
         return this.bold;
      }

      @Generated
      public boolean isItalic() {
         return this.italic;
      }

      @Generated
      public boolean isUnderlined() {
         return this.underlined;
      }

      @Generated
      public boolean isStrikethrough() {
         return this.strikethrough;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof FormattedTextProcessor.TextSegment other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else if (this.getColor() != other.getColor()) {
            return false;
         } else if (this.isBold() != other.isBold()) {
            return false;
         } else if (this.isItalic() != other.isItalic()) {
            return false;
         } else if (this.isUnderlined() != other.isUnderlined()) {
            return false;
         } else if (this.isStrikethrough() != other.isStrikethrough()) {
            return false;
         } else {
            Object this$text = this.getText();
            Object other$text = other.getText();
            return this$text == null ? other$text == null : this$text.equals(other$text);
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof FormattedTextProcessor.TextSegment;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         result = result * 59 + this.getColor();
         result = result * 59 + (this.isBold() ? 79 : 97);
         result = result * 59 + (this.isItalic() ? 79 : 97);
         result = result * 59 + (this.isUnderlined() ? 79 : 97);
         result = result * 59 + (this.isStrikethrough() ? 79 : 97);
         Object $text = this.getText();
         return result * 59 + ($text == null ? 43 : $text.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "FormattedTextProcessor.TextSegment(text="
            + this.getText()
            + ", color="
            + this.getColor()
            + ", bold="
            + this.isBold()
            + ", italic="
            + this.isItalic()
            + ", underlined="
            + this.isUnderlined()
            + ", strikethrough="
            + this.isStrikethrough()
            + ")";
      }
   }
}
