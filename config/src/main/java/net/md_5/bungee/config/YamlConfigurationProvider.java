package net.md_5.bungee.config;

import java.io.*;

@Deprecated
class YamlConfigurationProvider extends ConfigurationProvider {
    @Override
    public void save(Configuration config, File file) throws IOException {
        config.save( file );
    }

    @Override
    public void save(Configuration config, Writer writer) {
        config.save( writer );
    }

    @Override
    public Configuration load(File file) throws IOException {
        return new YamlConfiguration().load( file );
    }

    @Override
    public Configuration load(File file, Configuration defaults) throws IOException {
        return new YamlConfiguration().load( file ).setDefaults( defaults );
    }

    @Override
    public Configuration load(Reader reader) {
        return new YamlConfiguration().load( reader );
    }

    @Override
    public Configuration load(Reader reader, Configuration defaults) {
        return new YamlConfiguration().load( reader ).setDefaults( defaults );
    }

    @Override
    public Configuration load(InputStream is) {
        return new YamlConfiguration().load( is );
    }

    @Override
    public Configuration load(InputStream is, Configuration defaults) {
        return new YamlConfiguration().load( is ).setDefaults( defaults );
    }

    @Override
    public Configuration load(String string) {
        return new YamlConfiguration().load( string );
    }

    @Override
    public Configuration load(String string, Configuration defaults) {
        return new YamlConfiguration().load( string ).setDefaults( defaults );
    }
}
