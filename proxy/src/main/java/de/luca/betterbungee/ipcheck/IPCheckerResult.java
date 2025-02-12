package de.luca.betterbungee.ipcheck;

public class IPCheckerResult {

	
	public String getIP() {
		return IP;
	}


	public String getCompany() {
		return Company;
	}


	public Integer getASN() {
		return ASN;
	}


	public String getCity() {
		return City;
	}


	public String getCountry() {
		return Country;
	}


	public String getCountryCode() {
		return CountryCode;
	}


	public boolean isHosting() {
		return Hosting;
	}


	public boolean isProxy() {
		return Proxy;
	}


	public boolean isVPN() {
		return VPN;
	}


	public boolean isTOR() {
		return TOR;
	}


	public boolean isResidental() {
		return Residental;
	}


	private String IP;

	private String Company;

	private Integer ASN;

	private String City;

	private String Country;

	private String CountryCode;

	private boolean Hosting;
	
	private boolean Proxy;

	private boolean VPN;

	private boolean TOR;

	private boolean Residental;
}
