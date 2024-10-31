package net.md_5.bungee.command;

import de.luca.betterbungee.BetterBungeeAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class CommandBungee extends Command
{

    public CommandBungee()
    {
        super( "bungee" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        TextComponent text1 = new TextComponent();
        text1.addExtra("§7This server is running §eBetterBungee§7 version §a" + BetterBungeeAPI.getBetterBungeeVersion());
        text1.addExtra("§7 by §bLuca_zPlays");
        text1.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/LucazPlays/BetterBungee/"));
        text1.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Link to Github")));
        sender.sendMessage(text1);
        
        TextComponent text2 = new TextComponent();
        text2.addExtra("§7More Infos at §dhttps://dsc.gg/betterbungee");
        text2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://dsc.gg/betterbungee"));
        text2.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Link to Discord")));
        sender.sendMessage(text2);

        
        TextComponent text3 = new TextComponent();
        text3.addExtra("§7BungeeCord Version §f#" + BetterBungeeAPI.getBungeecordversion().substring(0, 7));
        text3.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/SpigotMC/BungeeCord/tree/"+BetterBungeeAPI.getBungeecordversion()));
        text3.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Link to Github")));
        sender.sendMessage(text3);
    }
}
