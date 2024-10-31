package net.md_5.bungee.command;

import java.net.InetAddress;
import java.util.Collections;
import java.util.regex.Pattern;

import de.luca.betterbungee.BetterBungeeAPI;
import de.luca.betterbungee.ipcheck.IPCheckerResult;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CommandIP extends PlayerCommand
{

    public CommandIP()
    {
        super( "ip", "bungeecord.command.ip" , new String[] {"bip", "betterip"});
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( args.length < 1 )
        {
            sender.sendMessage( ProxyServer.getInstance().getTranslation( "username_needed" ) );
            return;
        }

        BetterBungeeAPI.getIpchecker().start(() -> {
            try {
                final ProxiedPlayer user = ProxyServer.getInstance().getPlayer(args[0]);

                if (user == null) {
                    String ip = args[0];
                    if (isValidInet4Address(ip)) {
                        IPCheckerResult result = BetterBungeeAPI.getIpchecker().getIPInfo(ip);
                        if (result == null || !BetterBungeeAPI.getIpchecker().isServiceonline()) {
                            sendipinfos(sender, ip);
                            return;
                        }
                        sendipmessage(sender, result, true);
                    } else {
                        try {
                            InetAddress ipv6 = InetAddress.getByName(ip);

                            if (ipv6 != null) {
                                IPCheckerResult result = BetterBungeeAPI.getIpchecker().getIPInfo(ip);
                                if (result == null || !BetterBungeeAPI.getIpchecker().isServiceonline()) {
                                    sendipinfos(sender, ip);
                                    return;
                                }
                                sendipmessage(sender, result, true);
                            }
                        } catch (Exception ex) {
                            sender.sendMessage(ProxyServer.getInstance().getTranslation("user_not_online", new Object[0]));
                        }
                    }
                } else {
                    IPCheckerResult result = BetterBungeeAPI.getIpchecker().getIPInfo(user.getAddress().getAddress().getHostAddress());

                    if (result == null || !BetterBungeeAPI.getIpchecker().isServiceonline()) {
                        sender.sendMessage(TextComponent.fromLegacyText(BetterBungeeAPI.getPrefix() + "§8[§6IPINFO§8]"));
                        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7IP: §e" + user.getAddress().getAddress().getHostAddress()));
                        sender.sendMessage(TextComponent.fromLegacyText(BetterBungeeAPI.getPrefix() + "§8[§6IPINFO§8]"));
                        return;
                    }

                    sendipmessage(sender, result, sender.hasPermission("bungeecord.command.ip.uncensored"));
                }
            } catch (Throwable t) {
                sender.sendMessage(t.getMessage());
            }
        });
    }


    private void sendipinfos(final CommandSender sender, String ip) {
        sender.sendMessage(TextComponent.fromLegacyText(BetterBungeeAPI.getPrefix() + " §8[§6IPINFO§8]"));
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7IP: §c" + ip));
        sender.sendMessage(TextComponent.fromLegacyText(BetterBungeeAPI.getPrefix() + " §8[§6IPINFO§8]"));
    }

    public static void sendipmessage(final CommandSender sender, IPCheckerResult result, boolean uncensored) {
        sender.sendMessage(TextComponent.fromLegacyText(BetterBungeeAPI.getPrefix() + "§8[§6IPINFO§8]"));
        if (uncensored) {
            sender.sendMessage(TextComponent.fromLegacyText("§8 - §7IP: §e" + result.getIP()));
        } else {
            sender.sendMessage(TextComponent.fromLegacyText("§8 - §7IP: §c" + "§lCENSORED"));
        }
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7Country: §e" + result.getCountry()));
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7CountryCode: §e" + result.getCountryCode()));
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7City: §e" + result.getCity()));
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7ASN: §e" + result.getASN()));
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7Company: §e" + result.getCompany()));
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7Hosting: §e" + result.isHosting()));
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7VPN: §e" + result.isVPN()));
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7Proxy: §e" + result.isProxy()));
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7TOR: §e" + result.isTOR()));
        sender.sendMessage(TextComponent.fromLegacyText("§8 - §7Residental: §e" + result.isResidental()));
        sender.sendMessage(TextComponent.fromLegacyText(BetterBungeeAPI.getPrefix() + "§8[§6IPINFO§8]"));
    }




    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        return ( args.length == 1 ) ? super.onTabComplete( sender, args ) : Collections.emptyList();
    }


    private static final String IPV4_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";

    private static final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);

    public static boolean isValidInet4Address(String ip) {
        if (ip == null) {
            return false;
        }

        if (!IPv4_PATTERN.matcher(ip).matches()) {
            return false;
        }

        String[] parts = ip.split("\\.");

        // verify that each of the four subgroups of IPv4 addresses is legal
        try {
            for (String segment : parts) {
                // x.0.x.x is accepted but x.01.x.x is not
                if (Integer.parseInt(segment) > 255 || (segment.length() > 1 && segment.startsWith("0"))) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
