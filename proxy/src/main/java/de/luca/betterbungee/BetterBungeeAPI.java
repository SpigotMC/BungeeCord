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
    private static String bungeecordversion = "373dab05ad456bdb72b3d70e533ef7c281453c78";
}
