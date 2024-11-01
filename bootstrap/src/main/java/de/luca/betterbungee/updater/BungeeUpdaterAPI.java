package de.luca.betterbungee.updater;

import net.md_5.bungee.api.ProxyServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BungeeUpdaterAPI {

	private static final String SERVERURL = "http://updaterapi.skydb.de";

	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private String uuid = UUID.randomUUID().toString();

	private TimeUnit unit = TimeUnit.SECONDS;

	private int updatetime = 30;

	private String key = "none";

	private UpdateType type = UpdateType.DEFAULT;

	private LogLevel loglevel = LogLevel.NONE;

	private final long filesize = calculcatefilesize();

	private boolean onlyempty = false;

	private boolean hibernat = false;

	private long timesinceempty = System.currentTimeMillis();

	public BungeeUpdaterAPI(String uuid, String key) {
		if (key != "") {
			this.key = key;
		}
		this.uuid = uuid;
		overridecheck();

		start(updatetime, unit);
	}

	private void overridecheck() {
		try {
			File thisfile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsoluteFile();
			File updaterfile = new File(thisfile.getParentFile().getAbsoluteFile() + "/" + thisfile.getName().replaceAll(".jar", ".updater"));
			if (!updaterfile.exists()) {
				return;
			}
			try {
				String content = new String(Files.readAllBytes(updaterfile.toPath()));
				String[] parts = content.split(":");
				if (parts.length > 0) {
					uuid = parts[0];
				}
				if (parts.length > 1) {
					key = parts[1];
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public BungeeUpdaterAPI(String uuid, String key, int updatetime, TimeUnit unit) {
		if (key != "") {
			this.key = key;
		}
		this.uuid = uuid;
		overridecheck();

		start(updatetime, unit);
	}

	private void plugincheck() {
		if (ProxyServer.getInstance().getPluginManager().getPlugin("CloudNet-Bridge") != null) {
			type = UpdateType.CLOUDNETV3;
		}
		if (ProxyServer.getInstance().getPluginManager().getPlugin("SimpleCloud-Plugin") != null) {
			type = UpdateType.SIMPLECLOUDV2;
		}
	}

	private void start(int updatetime, TimeUnit unit) {
		scheduler.scheduleAtFixedRate(() -> {
			try {
				pauseupdating();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}, 5, 5, TimeUnit.SECONDS);
		scheduler.scheduleAtFixedRate(() -> {
			try {
				if (pauseupdating()) {
					return;
				}
				String restrequest = restrequest(SERVERURL + "/getupdate?uuid=" + uuid + "&size=" + getFilesize());
				if (restrequest.equals("Update")) {
					plugincheck();
					filelocation().parallelStream().forEach(locations -> {
						download(SERVERURL + "/downloadupdate?uuid=" + uuid + "&size=" + getFilesize() + "&key=" + key,
								locations);
					});
					ProxyServer.getInstance().stop();
					scheduler.shutdown();
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}, updatetime, updatetime, unit);
	}

	public boolean pauseupdating() {
		if (onlyempty) {
			if (ProxyServer.getInstance().getOnlineCount() != 0) {
				timesinceempty = 0;
				return true;
			} else {
				if (hibernat) {
					if (timesinceempty == 0) {
						timesinceempty = System.currentTimeMillis();
					} else {
						long hibernatetime = 1000 * 60 * 30;
						if (timesinceempty < System.currentTimeMillis()-hibernatetime) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private Collection<String> filelocation() {
		Set<String> locations = ConcurrentHashMap.newKeySet();
		try {
			locations.add(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		File thisfile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		log(thisfile.getName());
		log("Search for Plugin Jar files to Update");
		log("Update Mode: " + type);

		if (type == UpdateType.SIMPLECLOUDV2) {
			File parentfile = new File("");

			String absolutpath = parentfile.getAbsolutePath();
			log("temp service Location: " + absolutpath);

			String foldername = absolutpath.substring(absolutpath.lastIndexOf("/") + 1, absolutpath.length());
			log("temp service Name: " + foldername);

			String tmpfolder = new File(new File("../").toURI().normalize().getPath()).getName().toLowerCase();
			log("temp folder name: " + tmpfolder);

			if (foldername.contains("-") && tmpfolder.contains("tmp")) {
				log("detected simplecloud service");
				String groupname = foldername.split("-")[0];
				log("group name: " + groupname);

				String globalpath1 = new File(new File("../../templates/EVERY/" + thisfile.getName()).toURI().normalize().getPath()).getAbsolutePath();

				if (new File(globalpath1).exists()) {
					log("global 1 plugin detected: " + globalpath1);
					locations.add(globalpath1);
				}

				String globalpath2 = new File(new File("../../templates/EVERY_PROXY/" + thisfile.getName()).toURI().normalize().getPath()).getAbsolutePath();

				if (new File(globalpath2).exists()) {
					log("global 2 plugin detected: " + globalpath2);
					locations.add(globalpath2);
				}

				String templatepath = new File(new File("../../templates/" + groupname + "/" + thisfile.getName()).toURI().normalize().getPath()).getAbsolutePath();

				if (new File(templatepath).exists()) {
					log("template plugin detected: " + templatepath);
					locations.add(templatepath);
				}
			}
		} else if (type == UpdateType.CLOUDNETV2) {

			File parentfile = new File("");

			String absolutpath = parentfile.getAbsolutePath();
			log("temp service Location: " + absolutpath);

			String foldername = absolutpath.substring(absolutpath.lastIndexOf("/") + 1, absolutpath.length());
			log("temp service Name: " + foldername);

			if (foldername.contains("-") && foldername.contains("_")) {
				log("detected cloudnet dynamic service");
				String groupname = foldername.split("_")[0].split("-")[0];

				String globalpath = new File(
						new File("../../local/global/" + thisfile.getName()).toURI()
								.normalize().getPath())
						.getAbsolutePath();

				if (new File(globalpath).exists()) {
					log("global plugin detected: " + globalpath);
					locations.add(globalpath);
				}

				String templatepath = new File(
						new File("../../local/templates/" + groupname).toURI().normalize().getPath())
						.getAbsolutePath();

				log(templatepath);

				File templatedir = new File(templatepath);

				log(templatedir.getAbsolutePath());

				for (String template : templatedir.list()) {
					log("detected template: " + template);
					if (new File(templatedir.getAbsolutePath() + "/" + template).isDirectory()) {
						File pluginfile = new File(
								templatedir.getAbsolutePath() + "/" + template + "/plugins/" + thisfile.getName());
						log("Detected Plugin To Update: " + thisfile.getName());
						log(pluginfile.getAbsolutePath());
						locations.add(pluginfile.getAbsolutePath());
					}
				}
			}
		} else if (type == UpdateType.CLOUDNETV3) {

			File parentfile = new File("");

			String absolutpath = parentfile.getAbsolutePath();
			log("temp service Location: " + absolutpath);

			String foldername = absolutpath.substring(absolutpath.lastIndexOf("/") + 1, absolutpath.length());
			log("temp service Name: " + foldername);

			if (foldername.contains("-") && foldername.contains("_")) {
				log("detected cloudnet dynamic service");
				String groupname = foldername.split("_")[0].split("-")[0];

				String globalpathtemplates = new File(
						new File("../../../local/templates/Global/" + groupname).toURI().normalize().getPath())
						.getAbsolutePath();

				File globaldir = new File(globalpathtemplates);

				for (String templates : globaldir.list()) {
					File pluginfile = new File(globaldir.getAbsolutePath() + "/" + templates + "/plugins/" + thisfile.getName());
					if (pluginfile.exists()) {
						log("Detected Global Plugin To Update: " + thisfile.getName());
						log(pluginfile.getAbsolutePath());
						locations.add(pluginfile.getAbsolutePath());
					}
				}

//				String globalpath = new File(
//						new File("../../../local/templates/Global/server/plugins/" + thisfile.getName()).toURI().normalize().getPath()).getAbsolutePath();
//
//				if (new File(globalpath).exists()) {
//					log("global plugin detected: " + globalpath);
//					locations.add(globalpath);
//				}

				String templatepath = new File(
						new File("../../../local/templates/" + groupname).toURI().normalize().getPath())
						.getAbsolutePath();

				log(templatepath);

				File templatedir = new File(templatepath);

				log(templatedir.getAbsolutePath());

				for (String template : templatedir.list()) {
					log("detected template: " + template);
					if (new File(templatedir.getAbsolutePath() + "/" + template).isDirectory()) {
						File pluginfile = new File(
								templatedir.getAbsolutePath() + "/" + template + "/plugins/" + thisfile.getName());
						log("Detected Plugin To Update: " + thisfile.getName());
						log(pluginfile.getAbsolutePath());
						locations.add(pluginfile.getAbsolutePath());
					}
				}
			}
		}

		return locations;
	}

	private long calculcatefilesize() {
		try {
			return new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).length();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		};
		return 0;
	}

	private String restrequest(String urlstring) {
		try {
			URL url = new URL(urlstring);
			URLConnection con = null;
			con = url.openConnection();
			con.setConnectTimeout(5000);
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String result = in.readLine();
			in.close();
			return result;
		} catch (IOException e) {
		}
		return null;
	}

	private static boolean download(String link, String path) {
		try {
			InputStream in = new URL(link).openStream();
			Files.copy(in, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public LogLevel getLoglevel() {
		return loglevel;
	}

	public void setLoglevel(LogLevel loglevel) {
		this.loglevel = loglevel;
	}

	public enum UpdateType {
		DEFAULT, CLOUDNETV3, CLOUDNETV2, SIMPLECLOUDV2, ORIGIN
	}

	public enum LogLevel {
		NONE, DEBUG
	}

	public void log(String message) {
		if (loglevel == LogLevel.DEBUG) {
			System.out.println(message);
		}
	}

	public long getFilesize() {
		return filesize;
	}

	public BungeeUpdaterAPI setOnlyempty(boolean onlyempty) {
		this.onlyempty = onlyempty;
		return this;
	}

	public BungeeUpdaterAPI setHibernat(boolean hibernat) {
		this.hibernat = hibernat;
		return this;
	}
}
