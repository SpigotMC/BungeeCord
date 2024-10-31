package de.luca.betterbungee;

import de.luca.betterbungee.ipcheck.IPChecker;
import lombok.Getter;

public class BetterBungeeAPI {
    @Getter
    private static IPChecker ipchecker = new IPChecker();

    @Getter
    private static String prefix = "§6BetterBungee";

    @Getter
    private static String BetterBungeeVersion = "1.0";

    @Getter
    private static String bungeecordversion = "4886c4be017761333dc67b9c300acc7481354a7d";
}
