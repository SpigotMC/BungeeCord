module org.spigotmc.bungee.slf4j {

    requires java.logging;
    requires org.slf4j;

    exports org.slf4j.jul;
}
