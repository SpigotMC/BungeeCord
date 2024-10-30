package de.luca.betterbungee;

import de.luca.betterbungee.ipcheck.IPChecker;
import lombok.Getter;

public class BetterBungeeAPI {
    @Getter
    private static IPChecker ipchecker = new IPChecker();

    @Getter
    private static String prefix = "§6BetterBungee §7- §e ";
}
