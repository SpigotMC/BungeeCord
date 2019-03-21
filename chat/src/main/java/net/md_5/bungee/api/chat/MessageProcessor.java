package net.md_5.bungee.api.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.Getter;


/**
 * Created by marvin on 31.01.15.
 */
@RequiredArgsConstructor
public abstract class MessageProcessor {

    @Getter
    private final Pattern pattern;


    public Matcher matcher(String message) {
        return pattern.matcher(message);
    }

    public abstract TextComponent process(String match, TextComponent old);

    public static MessageProcessor URL_MESSAGEPROCESSOR = new MessageProcessor(Pattern.compile( "^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$" )) {
        @Override
        public TextComponent process(String match, TextComponent old) {
            TextComponent component = new TextComponent(old);
            component.setText( "Link: " + match );
            component.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL,
                    match.startsWith( "http" ) ? match : "http://" + match ) );
            return component;
        }
    };
    
}
