package net.md_5.bungee;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatConverter {

    private static final Gson gson = new Gson();
    private static final char COLOR_CHAR = '\u00A7';
    private static final Pattern url = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");

    public static String[] toJSONChat(String txt) {
        Message msg = new Message();
        ArrayList<String> parts = new ArrayList<String>();
        StringBuilder outBuffer = new StringBuilder("[");
        StringBuilder buf = new StringBuilder();
        Matcher matcher = url.matcher(txt);
        for (int i = 0; i < txt.length(); i++) {
            char c = txt.charAt(i);
            if (c != COLOR_CHAR) {
                int pos = txt.indexOf(' ', i);
                if (pos == -1) pos = txt.length();
                if (matcher.region(i, pos).find()) { //Web link handling
                    msg.text = buf.toString();
                    buf = new StringBuilder();
                    outBuffer = append(parts, outBuffer, msg);
                    Message old = msg;
                    msg = new Message(old);
                    msg.clickEvent = new ClickEvent();
                    msg.clickEvent.action = "open_url";
                    String urlString = txt.substring(i, pos);
                    if (urlString.startsWith("http")) {
                        msg.text = msg.clickEvent.value = urlString;
                    } else {
                        msg.text = urlString;
                        msg.clickEvent.value = "http://" + urlString;
                    }
                    outBuffer = append(parts, outBuffer, msg);
                    i += pos - i - 1;
                    msg = new Message(old);
                    continue;
                }
                buf.append(c);
                continue;
            }
            i++;
            c = txt.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 32;
            }
            msg.text = buf.toString();
            buf = new StringBuilder();
            outBuffer = append(parts, outBuffer, msg);
            msg = new Message(msg);
            switch(c) {
                case 'k':
                    msg.obfuscated = Boolean.TRUE;
                    break;
                case 'l':
                    msg.bold = Boolean.TRUE;
                    break;
                case 'm':
                    msg.strikethrough = Boolean.TRUE;
                    break;
                case 'n':
                    msg.underlined = Boolean.TRUE;
                    break;
                case 'o':
                    msg.italic = Boolean.TRUE;
                    break;
                default:
                    msg.obfuscated = null;
                    msg.bold = null;
                    msg.strikethrough = null;
                    msg.underlined = null;
                    msg.italic = null;
                    if (c != 'r') {
                        msg.color = Color.fromCode(Character.toString(c));
                    } else {
                        msg.color = Color.WHITE;
                    }
                    break;
            }
        }
        msg.text = buf.toString();
        append(parts, outBuffer, msg);

        parts.add(outBuffer.append("]").toString());
        String[] pArray = new String[parts.size()];
        parts.toArray(pArray);
        return pArray;
    }

    private static StringBuilder append(ArrayList<String> parts, StringBuilder outBuffer, Message part) {
        String p = gson.toJson(part);
        if (p.length() + outBuffer.length() + 1 >= Short.MAX_VALUE  - 20) {
            outBuffer.append("]");
            parts.add(outBuffer.toString());
            outBuffer = new StringBuilder("[");
        }
        if (outBuffer.length() != 1) {
            outBuffer.append(",");
        }
        outBuffer.append(p);
        return outBuffer;
    }
}

class Message {
    public String text;

    public Boolean bold;
    public Boolean italic;
    public Boolean underlined;
    public Boolean strikethrough;
    public Boolean obfuscated;

    public Color color;

    public ClickEvent clickEvent;

    public Message() {

    }

    public Message(Message old) {
        this.bold = old.bold;
        this.italic = old.italic;
        this.underlined = old.underlined;
        this.strikethrough = old.strikethrough;
        this.color = old.color;
    }
}

class ClickEvent {
    public String action;
    public String value;
}

enum Color {
    @SerializedName("black")
    BLACK("0"),
    @SerializedName("dark_blue")
    DARK_BLUE("1"),
    @SerializedName("dark_green")
    DARK_GREEN("2"),
    @SerializedName("dark_aqua")
    DARK_AQUA("3"),
    @SerializedName("dark_red")
    DARK_RED("4"),
    @SerializedName("dark_purple")
    DARK_PURPLE("5"),
    @SerializedName("gold")
    GOLD("6"),
    @SerializedName("gray")
    GRAY("7"),
    @SerializedName("dark_gray")
    DARK_GRAY("8"),
    @SerializedName("blue")
    BLUE("9"),
    @SerializedName("green")
    GREEN("a"),
    @SerializedName("aqua")
    AQUA("b"),
    @SerializedName("red")
    RED("c"),
    @SerializedName("light_purple")
    LIGHT_PURPLE("d"),
    @SerializedName("yellow")
    YELLOW("e"),
    @SerializedName("white")
    WHITE("f");

    public String code;

    Color(String code) {
        this.code = code;
    }


    private static HashMap<String, Color> codeMap = new HashMap<String, Color>();

    public static Color fromCode(String code) {
        return codeMap.get(code);
    }

    static {
        for (Color color : values()) {
            codeMap.put(color.code, color);
        }
    }
}
