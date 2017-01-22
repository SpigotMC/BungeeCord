package org.bukkit.map;

import java.util.HashMap;

public class MapFont {

    private final HashMap<Character, CharacterSprite> chars = new HashMap<Character, CharacterSprite>();
    private int height = 0;
    protected boolean malleable = true;

    public void setChar(char ch, MapFont.CharacterSprite sprite) {
        if (!this.malleable) {
            throw new IllegalStateException("this font is not malleable");
        } else {
            this.chars.put(Character.valueOf(ch), sprite);
            if (sprite.getHeight() > this.height) {
                this.height = sprite.getHeight();
            }

        }
    }

    public MapFont.CharacterSprite getChar(char ch) {
        return (MapFont.CharacterSprite) this.chars.get(Character.valueOf(ch));
    }

    public int getWidth(String text) {
        if (!this.isValid(text)) {
            throw new IllegalArgumentException("text contains invalid characters");
        } else if (text.length() == 0) {
            return 0;
        } else {
            int result = 0;

            for (int i = 0; i < text.length(); ++i) {
                result += ((MapFont.CharacterSprite) this.chars.get(Character.valueOf(text.charAt(i)))).getWidth();
            }

            result += text.length() - 1;
            return result;
        }
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isValid(String text) {
        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);

            if (ch != 167 && ch != 10 && this.chars.get(Character.valueOf(ch)) == null) {
                return false;
            }
        }

        return true;
    }

    public static class CharacterSprite {

        private final int width;
        private final int height;
        private final boolean[] data;

        public CharacterSprite(int width, int height, boolean[] data) {
            this.width = width;
            this.height = height;
            this.data = data;
            if (data.length != width * height) {
                throw new IllegalArgumentException("size of data does not match dimensions");
            }
        }

        public boolean get(int row, int col) {
            return row >= 0 && col >= 0 && row < this.height && col < this.width ? this.data[row * this.width + col] : false;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }
    }
}
