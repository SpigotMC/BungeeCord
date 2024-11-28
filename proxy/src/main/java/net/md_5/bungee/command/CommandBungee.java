package net.md_5.bungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.module.ModuleVersion;
import net.md_5.bungee.util.BuildVersionRetriever;

public class CommandBungee extends Command
{
    private final BuildVersionRetriever versionRetriever = new BuildVersionRetriever();
    private final ModuleVersion moduleVersion;
    private int currentVersion = -1;

    public CommandBungee(ProxyServer proxy)
    {
        super( "bungee" );
        moduleVersion = ModuleVersion.parse( proxy.getVersion() );
        try
        {
            if ( moduleVersion != null )
            {
                currentVersion = Integer.parseInt( moduleVersion.getBuild() );
            }
        } catch ( NumberFormatException ignored )
        {
        }
    }
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        TextComponent component = new TextComponent( "This server is running BungeeCord version " );
        component.setColor( ChatColor.BLUE );
        TextComponent version = new TextComponent( ProxyServer.getInstance().getVersion() );
        version.setClickEvent( new ClickEvent( ClickEvent.Action.COPY_TO_CLIPBOARD, ProxyServer.getInstance().getVersion() ) );
        version.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( new TextComponent( ProxyServer.getInstance().getTranslation( "click_to_copy" ) ) ) ) );
        component.addExtra( version );
        component.addExtra( " by md_5" );
        sender.sendMessage( component );

        versionRetriever.retrieveLatestBuild().thenAccept( latest ->
        {
            // custom build or spammed command while retrieving
            if ( moduleVersion == null || currentVersion == -1 || latest == 0 )
            {
                return;
            }

            if ( currentVersion >= latest )
            {
                sender.sendMessage( ChatColor.GREEN + "This server is running the latest build of BungeeCord" );
                return;
            }

            int behind = latest - currentVersion;
            sender.sendMessage( ChatColor.YELLOW + "This server is " + behind + " builds behind" );
            sender.sendMessage( new ComponentBuilder( "Click here to get the latest build" )
                    .italic( true )
                    .color( ChatColor.YELLOW )
                    .event( new ClickEvent( ClickEvent.Action.OPEN_URL, "https://ci.md-5.net/job/BungeeCord/lastBuild/" ) )
                    .build() );

        } ).exceptionally( ex ->
        {
            sender.sendMessage( ChatColor.RED + "Error fetching latest successful build" );
            return null;
        } );
    }
}
