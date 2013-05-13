package net.md_5.bungee.api.plugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

public class PluginLogger extends Logger {

	private String pluginName;

	protected PluginLogger(Plugin context) {
		super(context.getClass().getCanonicalName(), null);
		pluginName = "[" + context.getDescription.getName() + "] ";
		setParent(BungeeCord.getInstance().getLogger());
		setLevel(Level.ALL);
	}

	@Override
	public void log(LogRecord logRecord) {
		logRecord.setMessage(pluginName + logRecord.getMessage());
		super.log(logRecord);
	}

}