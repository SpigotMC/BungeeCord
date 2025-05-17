package net.md_5.bungee.api.dialog;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Accessors(fluent = true)
public class DialogBase
{

    private final BaseComponent title;
    private BaseComponent externalTitle;
    private List<?> body;
    private boolean canCloseWithEscape;
}
