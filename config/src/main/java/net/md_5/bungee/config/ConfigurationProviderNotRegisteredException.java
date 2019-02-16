package net.md_5.bungee.config;

class ConfigurationProviderNotRegisteredException extends Exception {

    @Override
    public String getMessage() {
        return "The class of the object is not registered.";
    }
}
