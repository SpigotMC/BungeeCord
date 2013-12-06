package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TextComponent extends BaseComponent {
    private String text;

    public TextComponent(TextComponent old) {
        super(old);
        setText(old.getText());
    }

    @Override
    protected void toPlainText(StringBuilder builder) {
        builder.append(text);
        super.toPlainText(builder);
    }

    @Override
    protected void toLegacyText(StringBuilder builder) {
        builder.append(getColor());
        if (isBold()) builder.append(ChatColor.BOLD);
        if (isItalic()) builder.append(ChatColor.ITALIC);
        if (isUnderlined()) builder.append(ChatColor.UNDERLINE);
        if (isStrikethrough()) builder.append(ChatColor.STRIKETHROUGH);
        if (isObfuscated()) builder.append(ChatColor.MAGIC);
        builder.append(text);
        super.toLegacyText(builder);
    }

    @Override
    public String toString() {
        return String.format("TextComponent{text=%s, %s}", text, super.toString());
    }
}
