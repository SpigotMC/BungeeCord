package de.luca.betterbungee.ipcheck;

import com.google.gson.Gson;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.*;

public class IPChecker {

	ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	ExecutorService threads = Executors.newCachedThreadPool();

	Set<String> proxyips = ConcurrentHashMap.newKeySet();

	@Getter
	private boolean serviceonline = false;

	public IPChecker() {
		scheduler.scheduleAtFixedRate(() -> {
			threads.execute(() -> {
				try {
					RestAPIResponse ipcheckeralive = get("http://ipcheck.skydb.de/alive");
					if (ipcheckeralive.getFailed()) {
						serviceonline = false;
					} else {
						serviceonline = true;
					}
					Thread.sleep(60000);
				} catch (Exception e) {
					//e.printStackTrace();
				}
			});
		}, 0, 1, TimeUnit.MINUTES);
	}

	public boolean isipresidental(String ip) {
		if (proxyips.contains(ip)) {
			return false;
		}
		if (serviceonline) {
			RestAPIResponse isipresidental = get("http://ipcheck.skydb.de/residental?ip=" + ip);
			if (isipresidental.getFailed()) {
				serviceonline = false;
			} else {
				if (isipresidental.getText().contains("false")) {
					proxyips.add(ip);
					return false;
				} else {
					return true;
				}
			}
		}
		return true;
	}

	public IPCheckerResult getIPInfo(String ip) {
		if (serviceonline) {
			RestAPIResponse getIPInfo = get("http://ipcheck.skydb.de/getinfo?ip=" + ip);
			if (getIPInfo.getFailed()) {
				serviceonline = false;
			} else {
				Gson gson = new Gson();
				return gson.fromJson(getIPInfo.getText(), IPCheckerResult.class);
			}

		}
		return null;
	}


	public ProxysResult getProxyList() {
		if (serviceonline) {
			RestAPIResponse getIPInfo = get("http://ipinfo.skydb.de/getproxys");
			if (getIPInfo.getFailed()) {
				serviceonline = false;
			} else {
				Gson gson = new Gson();
				return gson.fromJson(getIPInfo.getText(), ProxysResult.class);
			}

		}
		return null;
	}

	public void start(Runnable run) {
		threads.execute(run);
	}

	public static class ProxysResult {
		public ArrayList<String> IPs;
	}

	public RestAPIResponse get(String urlstring) {
		return get(urlstring,15000, null);
	}

	public RestAPIResponse get(String urlstring,Proxy proxy) {
		return get(urlstring,15000, proxy);
	}

	public RestAPIResponse get(String urlstring, int timeout, Proxy proxy) {
		String response = "";
		try {
			URL url = new URL(urlstring.replaceAll("\n", ""));
			URLConnection con = null;
			if (proxy == null) {
				con = url.openConnection();
			} else {
				con = url.openConnection(proxy);
			}
			con.setConnectTimeout(timeout);
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response += inputLine + "\n";
			}
			in.close();
		} catch (IOException e) {
			//e.printStackTrace();
			return new RestAPIResponse("Error", true, urlstring);
		}
		return new RestAPIResponse(response, false, urlstring);
	}

	public static class RestAPIResponse {

		private String text;

		private boolean failed;

		private String url;

		public RestAPIResponse(String text,boolean failed, String url) {
			this.url = url;
			this.text = text;
			this.failed = failed;
		}


		public String getText() {
			return text;
		}

		public boolean getFailed() {
			return failed;
		}


		public String getUrl() {
			return url;
		}
	}
}