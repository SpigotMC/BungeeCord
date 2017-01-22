package org.bukkit.map;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.util.Arrays;

import net.md_5.bungee.protocol.packet.extra.MapDataPacket;

public class CraftMapCanvas {

    private final byte[] buffer = new byte[16384];

    public CraftMapCanvas() {
        Arrays.fill(this.buffer, (byte) -1);
    }

    public void setPixel(int x, int y, byte color) {
        if (x >= 0 && y >= 0 && x < 128 && y < 128) {
            if (this.buffer[y * 128 + x] != color) {
                this.buffer[y * 128 + x] = color;
            }

        }
    }

    protected byte[] getBuffer() {
        return this.buffer;
    }

    @SuppressWarnings("deprecation")
    public void drawImage(int x, int y, Image image) {
        byte[] bytes = MapPalette.imageToBytes(image);

        for (int x2 = 0; x2 < image.getWidth((ImageObserver) null); ++x2) {
            for (int y2 = 0; y2 < image.getHeight((ImageObserver) null); ++y2) {
                this.setPixel(x + x2, y + y2, bytes[y2 * image.getWidth((ImageObserver) null) + x2]);
            }
        }

    }

    public void drawText(int x, int y, MapFont font, String text) {
        int xStart = x;
        byte color = 44;

        if (!font.isValid(text)) {
            throw new IllegalArgumentException("text contains invalid characters");
        } else {
            for (int i = 0; i < text.length(); ++i) {
                char ch = text.charAt(i);

                if (ch == 10) {
                    x = xStart;
                    y += font.getHeight() + 1;
                } else {
                    if (ch == 167) {
                        int sprite = text.indexOf(59, i);

                        if (sprite >= 0) {
                            try {
                                color = Byte.parseByte(text.substring(i + 1, sprite));
                                i = sprite;
                                continue;
                            } catch (NumberFormatException numberformatexception) {
                                ;
                            }
                        }
                    }

                    MapFont.CharacterSprite mapfont_charactersprite = font.getChar(text.charAt(i));

                    for (int r = 0; r < font.getHeight(); ++r) {
                        for (int c = 0; c < mapfont_charactersprite.getWidth(); ++c) {
                            if (mapfont_charactersprite.get(r, c)) {
                                this.setPixel(x + c, y + r, color);
                            }
                        }
                    }

                    x += mapfont_charactersprite.getWidth() + 1;
                }
            }

        }
    }

    public MapDataPacket.MapDataNew getMapData() {
        byte[] buffer = new byte[16384];
        byte[] buf = this.getBuffer();

        for (int i = 0; i < buf.length; ++i) {
            byte color = buf[i];

            if (color >= 0 || color <= -113) {
                buffer[i] = color;
            }
        }

        return new MapDataPacket.MapDataNew(128, 128, 0, 0, buffer);
    }

    private byte getColor(int x, int y) {
        return x >= 0 && y >= 0 && x < 128 && y < 128 ? this.buffer[y * 128 + x] : 0;
    }

    public MapDataPacket.MapData getOldData(int a) {
        byte[] colors = new byte[128];

        for (int i = 0; i < 128; ++i) {
            colors[i] = this.getColor(a, i);
        }

        return new MapDataPacket.MapColumnUpdate(a, 0, 128, colors);
    }
}
