package net.md_5.bungee.forge;

import com.google.common.io.BaseEncoding;
import net.md_5.bungee.protocol.packet.PluginMessage;

public class ForgeConstants
{
    private static byte[] vanillaBlocks17 = null;

    // Forge
    public static final String FORGE_HANDSHAKE_TAG = "FML|HS";
    
    public static final PluginMessage FML_REGISTER = new PluginMessage( "REGISTER", FORGE_HANDSHAKE_TAG.getBytes(), false );
    public static final PluginMessage FML_UNREGISTER = new PluginMessage( "UNREGISTER", FORGE_HANDSHAKE_TAG.getBytes(), false );
    public static final PluginMessage FML_ACK = new PluginMessage( FORGE_HANDSHAKE_TAG, new byte[] { -1, 0 }, false );
    public static final PluginMessage FML_START_CLIENT_HANDSHAKE = new PluginMessage( FORGE_HANDSHAKE_TAG, new byte[] { 0, 1 }, false );
    public static final PluginMessage FML_START_SERVER_HANDSHAKE = new PluginMessage( FORGE_HANDSHAKE_TAG, new byte[] { 1, 1 }, false );
    public static final PluginMessage FML_EMPTY_MOD_LIST = new PluginMessage( FORGE_HANDSHAKE_TAG, new byte[] { 2, 0 }, false );
    
    // Vanilla blocks. Obtained though packet sniffing. See the ForgeConstants class
    public static final PluginMessage FML_DEFAULT_IDS_17 = new PluginMessage( FORGE_HANDSHAKE_TAG, getVanillaBlocks17(), true);
    
    /**
     * Gets the Vanilla Blocks ID list for Minecraft 1.7 from the ForgeConstants class,
     * caches the byte form, and returns it.
     * @return The byte form of the ID list to return.
     */
    private static byte[] getVanillaBlocks17() {
        if (vanillaBlocks17 != null) {
            return vanillaBlocks17;
        }
        
        // Construct it once and store the bytes.
        vanillaBlocks17 = BaseEncoding.base64().decode( ForgeConstants.BASE_64_ENCODED_ID_LIST );
        return vanillaBlocks17;
    }    
    
    /**
     * The Forge vanilla block ID list for minecraft 1.7, encoded as Base64.
     */
    public static final String BASE_64_ENCODED_ID_LIST = "A+YDEAFtaW5lY3JhZnQ6Z2xhc3MUFwFtaW5lY3JhZnQ6c3RvbmVfc3RhaXJzQxECbWluZWNyYWZ0OmdyYXZlbA0bAm1pbmVjcmFmdDpwb2lzb25vdXNfcG" +
        "90YXRvigMWAW1pbmVjcmFmdDptb25zdGVyX2VnZ2EYAW1pbmVjcmFmdDpicmV3aW5nX3N0YW5kdQ8CbWluZWNyYWZ0OmRpcnQDDwJtaW5lY3JhZnQ6c25vd1AWAm1pbmVjcmFmdDpyZWNvcmRfd2Fp" + 
        "dNsRDgJtaW5lY3JhZnQ6dG50LhYCbWluZWNyYWZ0Om5ldGhlcmJyaWNrlQMWAW1pbmVjcmFmdDplbWVyYWxkX29yZYEBEAJtaW5lY3JhZnQ6Y2hlc3Q2EAJtaW5lY3JhZnQ6bGV2ZXJFFQJtaW5lY3" +
        "JhZnQ6c25vd19sYXllck4VAm1pbmVjcmFmdDpvYWtfc3RhaXJzNRgBbWluZWNyYWZ0OmRpYW1vbmRfYmxvY2s5FQFtaW5lY3JhZnQ6cmVkX2Zsb3dlciYSAm1pbmVjcmFmdDpiZWRyb2NrBxsCbWlu" +
        "ZWNyYWZ0OnNhbmRzdG9uZV9zdGFpcnOAARYCbWluZWNyYWZ0OmVtZXJhbGRfb3JlgQERAW1pbmVjcmFmdDpjYXJwZXSrAQ8CbWluZWNyYWZ0OmxvZzKiARoCbWluZWNyYWZ0OmNoYWlubWFpbF9ib2" +
        "90c7ECKAJtaW5lY3JhZnQ6bGlnaHRfd2VpZ2h0ZWRfcHJlc3N1cmVfcGxhdGWTARgBbWluZWNyYWZ0OnNwcnVjZV9zdGFpcnOGARkBbWluZWNyYWZ0OmFjdGl2YXRvcl9yYWlsnQEVAm1pbmVjcmFm" + 
        "dDpzdG9uZWJyaWNrYhACbWluZWNyYWZ0OnRvcmNoMhsBbWluZWNyYWZ0OmVuZF9wb3J0YWxfZnJhbWV4EwFtaW5lY3JhZnQ6ZGVhZGJ1c2ggHQFtaW5lY3JhZnQ6c3RvbmVfYnJpY2tfc3RhaXJzbR" + 
        "UCbWluZWNyYWZ0Omlyb25fYm9vdHO1AhcBbWluZWNyYWZ0OmRvdWJsZV9wbGFudK8BGAFtaW5lY3JhZnQ6YWNhY2lhX3N0YWlyc6MBEgJtaW5lY3JhZnQ6bGVhdGhlcs4CEwJtaW5lY3JhZnQ6ZGVh" +
        "ZGJ1c2ggHAFtaW5lY3JhZnQ6ZGF5bGlnaHRfZGV0ZWN0b3KXARkCbWluZWNyYWZ0OnJlZHN0b25lX2Jsb2NrmAEZAW1pbmVjcmFmdDpjcmFmdGluZ190YWJsZToSAW1pbmVjcmFmdDpqdWtlYm94VB" +
        "8BbWluZWNyYWZ0OnVucG93ZXJlZF9jb21wYXJhdG9ylQEeAm1pbmVjcmFmdDpuZXRoZXJfYnJpY2tfc3RhaXJzcg8CbWluZWNyYWZ0OnNpZ27DAhQCbWluZWNyYWZ0OndhdGVybGlseW8ZAm1pbmVj" +
        "cmFmdDpyZWNvcmRfbWVsbG9oadYRFQFtaW5lY3JhZnQ6aXJvbl9ibG9jayoaAm1pbmVjcmFmdDpob3BwZXJfbWluZWNhcnSYAxgBbWluZWNyYWZ0OnllbGxvd19mbG93ZXIlFQFtaW5lY3JhZnQ6c3" +
        "RvbmVfc2xhYiwYAm1pbmVjcmFmdDpyZWNvcmRfYmxvY2tz0hESAm1pbmVjcmFmdDpjaGlja2Vu7QIOAm1pbmVjcmFmdDptYXCLAxcCbWluZWNyYWZ0OnF1YXJ0el9ibG9ja5sBDwJtaW5lY3JhZnQ6" +
        "Y29hbIcCGAJtaW5lY3JhZnQ6d3JpdGFibGVfYm9va4IDGAJtaW5lY3JhZnQ6bXVzaHJvb21fc3Rld5oCFAJtaW5lY3JhZnQ6dGFsbGdyYXNzHxQCbWluZWNyYWZ0Omd1bnBvd2RlcqECFwJtaW5lY3" +
        "JhZnQ6cmVjb3JkX2NoaXJw0xEbAm1pbmVjcmFmdDplbmRfcG9ydGFsX2ZyYW1leBQBbWluZWNyYWZ0Om5vdGVibG9jaxkPAW1pbmVjcmFmdDpsb2cyogETAm1pbmVjcmFmdDpteWNlbGl1bW4QAm1p" +
        "bmVjcmFmdDpza3VsbI0DGQJtaW5lY3JhZnQ6YnJvd25fbXVzaHJvb20nGAJtaW5lY3JhZnQ6ZGlhbW9uZF9zd29yZJQCFAJtaW5lY3JhZnQ6ZW5kZXJfZXll/QIXAm1pbmVjcmFmdDp3YXRlcl9idW" +
        "NrZXTGAhcBbWluZWNyYWZ0OnF1YXJ0el9ibG9ja5sBEQJtaW5lY3JhZnQ6c3BvbmdlExECbWluZWNyYWZ0OnN0cmluZ58CDgJtaW5lY3JhZnQ6Ym93hQITAm1pbmVjcmFmdDpyZXBlYXRlcuQCEwJt" +
        "aW5lY3JhZnQ6b2JzaWRpYW4xDwFtaW5lY3JhZnQ6ZGlydAMaAm1pbmVjcmFmdDpmaXJld29ya19jaGFyZ2WSAxYCbWluZWNyYWZ0Om5ldGhlcl93YXJ09AIYAm1pbmVjcmFmdDpkZXRlY3Rvcl9yYW" +
        "lsHBgCbWluZWNyYWZ0OnRyaXB3aXJlX2hvb2uDARICbWluZWNyYWZ0OmRyb3BwZXKeARUCbWluZWNyYWZ0OmVuZF9wb3J0YWx3DwFtaW5lY3JhZnQ6c25vd1AUAW1pbmVjcmFmdDppcm9uX2Rvb3JH" +
        "EQJtaW5lY3JhZnQ6cG90aW9u9QIbAm1pbmVjcmFmdDppcm9uX2hvcnNlX2FybW9yoQMUAm1pbmVjcmFmdDpyZWNvcmRfMTPQERoCbWluZWNyYWZ0OmRpYW1vbmRfcGlja2F4ZZYCEwFtaW5lY3JhZn" +
        "Q6bXljZWxpdW1uGwFtaW5lY3JhZnQ6c2FuZHN0b25lX3N0YWlyc4ABEgFtaW5lY3JhZnQ6ZnVybmFjZT0cAW1pbmVjcmFmdDptb3NzeV9jb2JibGVzdG9uZTARAm1pbmVjcmFmdDpwbGFua3MFGAJt" +
        "aW5lY3JhZnQ6dHJhcHBlZF9jaGVzdJIBFwFtaW5lY3JhZnQ6bmV0aGVyX2JyaWNrcBUBbWluZWNyYWZ0OmdvbGRfYmxvY2spFAJtaW5lY3JhZnQ6bm90ZWJsb2NrGRABbWluZWNyYWZ0OndhdGVyCR" +
        "UBbWluZWNyYWZ0OmRyYWdvbl9lZ2d6DgFtaW5lY3JhZnQ6dG50LhYBbWluZWNyYWZ0Ondvb2Rlbl9kb29yQB0BbWluZWNyYWZ0Om5ldGhlcl9icmlja19mZW5jZXEeAW1pbmVjcmFmdDpuZXRoZXJf" +
        "YnJpY2tfc3RhaXJzchABbWluZWNyYWZ0OmxldmVyRRMCbWluZWNyYWZ0Omlyb25faG9lpAIVAm1pbmVjcmFmdDpnb2xkZW5fYXhlngIZAm1pbmVjcmFmdDpsZWF0aGVyX2hlbG1ldKoCDgFtaW5lY3" +
        "JhZnQ6YWlyABUCbWluZWNyYWZ0OmRyYWdvbl9lZ2d6FwJtaW5lY3JhZnQ6aXJvbl9waWNrYXhlgQIQAW1pbmVjcmFmdDpza3VsbJABFgJtaW5lY3JhZnQ6cHVtcGtpbl9waWWQAxEBbWluZWNyYWZ0" +
        "OnBvcnRhbFoPAm1pbmVjcmFmdDpib2F0zQITAW1pbmVjcmFmdDpwb3RhdG9lc44BHwJtaW5lY3JhZnQ6c3RvbmVfcHJlc3N1cmVfcGxhdGVGGQJtaW5lY3JhZnQ6YWN0aXZhdG9yX3JhaWydARUCbW" +
        "luZWNyYWZ0OmdvbGRfYmxvY2spFAJtaW5lY3JhZnQ6c3RvbmVfYXhlkwIWAm1pbmVjcmFmdDptYWdtYV9jcmVhbfoCEwFtaW5lY3JhZnQ6Y2F1bGRyb252GAJtaW5lY3JhZnQ6Zmxvd2luZ193YXRl" +
        "cggdAm1pbmVjcmFmdDpuZXRoZXJfYnJpY2tfZmVuY2VxEQFtaW5lY3JhZnQ6bGFkZGVyQRYCbWluZWNyYWZ0Om1lbG9uX3NlZWRz6gITAm1pbmVjcmFmdDpuYW1lX3RhZ6UDFwJtaW5lY3JhZnQ6Z2" +
        "9sZGVuX3N3b3JkmwIYAm1pbmVjcmFmdDplbWVyYWxkX2Jsb2NrhQEdAm1pbmVjcmFmdDpkaWFtb25kX2NoZXN0cGxhdGW3Ag8CbWluZWNyYWZ0OnNhbmQMFgJtaW5lY3JhZnQ6ZmlzaGluZ19yb2Ta" +
        "AhgCbWluZWNyYWZ0OmRpYW1vbmRfYmxvY2s5FAFtaW5lY3JhZnQ6bGFwaXNfb3JlFRUCbWluZWNyYWZ0OmdvbGRfaW5nb3SKAhsCbWluZWNyYWZ0OmVuY2hhbnRpbmdfdGFibGV0GAJtaW5lY3JhZn" +
        "Q6c3BydWNlX3N0YWlyc4YBGwFtaW5lY3JhZnQ6bGl0X3JlZHN0b25lX29yZUoXAm1pbmVjcmFmdDpzdG9uZV9zdGFpcnNDEAJtaW5lY3JhZnQ6Y2xvY2vbAhcBbWluZWNyYWZ0OnJlZHN0b25lX29y" +
        "ZUkQAm1pbmVjcmFmdDp3aGVhdKgCGAJtaW5lY3JhZnQ6YWNhY2lhX3N0YWlyc6MBFQFtaW5lY3JhZnQ6c3RvbmVicmlja2IQAm1pbmVjcmFmdDpmZW5jZVUbAm1pbmVjcmFmdDpjaGFpbm1haWxfaG" +
        "VsbWV0rgIcAm1pbmVjcmFmdDpleHBlcmllbmNlX2JvdHRsZYADEAJtaW5lY3JhZnQ6YXJyb3eGAhQCbWluZWNyYWZ0OnNhbmRzdG9uZRgYAW1pbmVjcmFmdDpoYXJkZW5lZF9jbGF5rAEWAW1pbmVj" +
        "cmFmdDpsaXRfcHVtcGtpblsWAW1pbmVjcmFmdDp3b29kZW5fc2xhYn4XAm1pbmVjcmFmdDp3b29kZW5fc3dvcmSMAhECbWluZWNyYWZ0OmJ1Y2tldMUCFgJtaW5lY3JhZnQ6aXJvbl9zaG92ZWyAAh" +
        "UCbWluZWNyYWZ0Om5ldGhlcnJhY2tXEgJtaW5lY3JhZnQ6bGVhdmVzMqEBDgJtaW5lY3JhZnQ6YmVk4wIQAm1pbmVjcmFmdDptZWxvbugCGwFtaW5lY3JhZnQ6cG93ZXJlZF9yZXBlYXRlcl4cAm1p" +
        "bmVjcmFmdDpnb2xkZW5fY2hlc3RwbGF0ZbsCEwJtaW5lY3JhZnQ6c25vd2JhbGzMAhcCbWluZWNyYWZ0OmZsb3dpbmdfbGF2YQoaAm1pbmVjcmFmdDpmbGludF9hbmRfc3RlZWyDAhUCbWluZWNyYW" +
        "Z0Omlyb25faW5nb3SJAhYCbWluZWNyYWZ0OmxpdF9wdW1wa2luWxYCbWluZWNyYWZ0Om5ldGhlcl9zdGFyjwMRAW1pbmVjcmFmdDpwbGFua3MFFwJtaW5lY3JhZnQ6YmxhemVfcG93ZGVy+QIWAm1p" +
        "bmVjcmFmdDp3b29kZW5fc2xhYn4QAm1pbmVjcmFmdDpicmlja9ACGAJtaW5lY3JhZnQ6Y29tbWFuZF9ibG9ja4kBFwJtaW5lY3JhZnQ6Z2xhc3NfYm90dGxl9gIXAW1pbmVjcmFmdDpyZWRfbXVzaH" +
        "Jvb20oFgFtaW5lY3JhZnQ6bmV0aGVyX3dhcnRzFQJtaW5lY3JhZnQ6aXRlbV9mcmFtZYUDGgJtaW5lY3JhZnQ6Y29va2VkX3BvcmtjaG9wwAIVAm1pbmVjcmFmdDppcm9uX2Jsb2NrKhQBbWluZWNy" +
        "YWZ0OmhheV9ibG9ja6oBGAFtaW5lY3JhZnQ6dHJpcHdpcmVfaG9va4MBFAFtaW5lY3JhZnQ6dGFsbGdyYXNzHxACbWluZWNyYWZ0OmFwcGxlhAIWAW1pbmVjcmFmdDpicmlja19ibG9jay0bAm1pbm" +
        "VjcmFmdDpmdXJuYWNlX21pbmVjYXJ01wIdAW1pbmVjcmFmdDpwb3dlcmVkX2NvbXBhcmF0b3KWARoCbWluZWNyYWZ0Omlyb25fY2hlc3RwbGF0ZbMCFQFtaW5lY3JhZnQ6cXVhcnR6X29yZZkBFAFt" +
        "aW5lY3JhZnQ6aXJvbl9iYXJzZREBbWluZWNyYWZ0OmNhY3R1c1EWAm1pbmVjcmFmdDpzdG9uZV9zd29yZJACFQJtaW5lY3JhZnQ6Zmxvd2VyX3BvdIYDEQFtaW5lY3JhZnQ6bGVhdmVzEhUBbWluZW" +
        "NyYWZ0OnBhY2tlZF9pY2WuARYCbWluZWNyYWZ0OmNvYmJsZXN0b25lBA4BbWluZWNyYWZ0OmJlZBoXAm1pbmVjcmFmdDpiaXJjaF9zdGFpcnOHARgCbWluZWNyYWZ0OnF1YXJ0el9zdGFpcnOcARkC" +
        "bWluZWNyYWZ0Omdsb3dzdG9uZV9kdXN03AIPAm1pbmVjcmFmdDpib3dsmQIVAm1pbmVjcmFmdDpzcGlkZXJfZXll9wIdAm1pbmVjcmFmdDpnb2xkZW5faG9yc2VfYXJtb3KiAxECbWluZWNyYWZ0Om" +
        "hvcHBlcpoBDwFtaW5lY3JhZnQ6c2FuZAwXAm1pbmVjcmFmdDpnb2xkZW5fYm9vdHO9AhUBbWluZWNyYWZ0OmNvYWxfYmxvY2utASgBbWluZWNyYWZ0OmhlYXZ5X3dlaWdodGVkX3ByZXNzdXJlX3Bs" +
        "YXRllAETAm1pbmVjcmFmdDpmYXJtbGFuZDwXAm1pbmVjcmFmdDpuZXRoZXJfYnJpY2twEgJtaW5lY3JhZnQ6cHVtcGtpblYdAW1pbmVjcmFmdDpkb3VibGVfd29vZGVuX3NsYWJ9FgFtaW5lY3JhZn" +
        "Q6ZGlhbW9uZF9vcmU4EAFtaW5lY3JhZnQ6Y2hlc3Q2FwFtaW5lY3JhZnQ6Zmxvd2luZ19sYXZhChACbWluZWNyYWZ0OmdsYXNzFBsCbWluZWNyYWZ0OmRpYW1vbmRfbGVnZ2luZ3O4AhYBbWluZWNy" +
        "YWZ0Om1vYl9zcGF3bmVyNBgCbWluZWNyYWZ0OnJlZHN0b25lX2xhbXB7DwJtaW5lY3JhZnQ6YmVlZusCFwJtaW5lY3JhZnQ6c3RvbmVfYnV0dG9uTRIBbWluZWNyYWZ0OmNhcnJvdHONARYCbWluZW" +
        "NyYWZ0Om1vbnN0ZXJfZWdnYRgBbWluZWNyYWZ0OnJlZHN0b25lX3dpcmU3HwFtaW5lY3JhZnQ6YnJvd25fbXVzaHJvb21fYmxvY2tjGAJtaW5lY3JhZnQ6Z29sZGVuX2hlbG1ldLoCEgJtaW5lY3Jh" +
        "ZnQ6Y29tcGFzc9kCGAJtaW5lY3JhZnQ6c3RpY2t5X3Bpc3Rvbh0YAW1pbmVjcmFmdDpzdGFuZGluZ19zaWduPx8BbWluZWNyYWZ0OnN0b25lX3ByZXNzdXJlX3BsYXRlRhYCbWluZWNyYWZ0Ondvb2" +
        "Rlbl9kb29yxAIWAm1pbmVjcmFmdDplbmRlcl9jaGVzdIIBFgJtaW5lY3JhZnQ6ZGlhbW9uZF9heGWXAhQCbWluZWNyYWZ0OmRpc3BlbnNlchcXAm1pbmVjcmFmdDpyZWRfbXVzaHJvb20oFgJtaW5l" +
        "Y3JhZnQ6Z29sZF9udWdnZXTzAhcCbWluZWNyYWZ0OndyaXR0ZW5fYm9va4MDGAJtaW5lY3JhZnQ6Z29sZGVuX2NhcnJvdIwDFwJtaW5lY3JhZnQ6c3RvbmVfc2hvdmVskQIUAW1pbmVjcmFmdDpzb3" +
        "VsX3NhbmRYFAFtaW5lY3JhZnQ6Ym9va3NoZWxmLxcCbWluZWNyYWZ0OnJlZHN0b25lX29yZUkVAW1pbmVjcmFmdDpmZW5jZV9nYXRlaw4CbWluZWNyYWZ0OmxvZxETAm1pbmVjcmFmdDp0cmFwZG9v" +
        "cmAYAm1pbmVjcmFmdDpoYXJkZW5lZF9jbGF5rAEUAW1pbmVjcmFmdDpnbG93c3RvbmVZFQJtaW5lY3JhZnQ6Z2hhc3RfdGVhcvICGAFtaW5lY3JhZnQ6anVuZ2xlX3N0YWlyc4gBFgJtaW5lY3JhZn" +
        "Q6aXJvbl9oZWxtZXSyAh0CbWluZWNyYWZ0OmxlYXRoZXJfY2hlc3RwbGF0ZasCFQFtaW5lY3JhZnQ6Z2xhc3NfcGFuZWYSAW1pbmVjcmFmdDpkcm9wcGVyngEUAm1pbmVjcmFmdDpibGF6ZV9yb2Tx" +
        "AhgCbWluZWNyYWZ0OnB1bXBraW5fc2VlZHPpAhECbWluZWNyYWZ0OnBvdGF0b4gDKAFtaW5lY3JhZnQ6bGlnaHRfd2VpZ2h0ZWRfcHJlc3N1cmVfcGxhdGWTARUBbWluZWNyYWZ0Om5ldGhlcnJhY2" +
        "tXGAFtaW5lY3JhZnQ6c3RhaW5lZF9nbGFzc18dAW1pbmVjcmFmdDpzdGFpbmVkX2dsYXNzX3BhbmWgARYBbWluZWNyYWZ0OmVuZGVyX2NoZXN0ggEoAm1pbmVjcmFmdDpoZWF2eV93ZWlnaHRlZF9w" +
        "cmVzc3VyZV9wbGF0ZZQBEAJtaW5lY3JhZnQ6YnJlYWSpAhYCbWluZWNyYWZ0OmdvbGRlbl9yYWlsGxUCbWluZWNyYWZ0OmZpbGxlZF9tYXDmAhQBbWluZWNyYWZ0OnNhbmRzdG9uZRgWAm1pbmVjcm" +
        "FmdDp3aGVhdF9zZWVkc6cCEwFtaW5lY3JhZnQ6dHJhcGRvb3JgEwFtaW5lY3JhZnQ6dHJpcHdpcmWEAQ8CbWluZWNyYWZ0OmxlYWSkAxgBbWluZWNyYWZ0OnJlZHN0b25lX2xhbXB7HAJtaW5lY3Jh" +
        "ZnQ6ZG91YmxlX3N0b25lX3NsYWIrFQFtaW5lY3JhZnQ6bWVsb25fc3RlbWkRAm1pbmVjcmFmdDpsZWF2ZXMSGAJtaW5lY3JhZnQ6d29vZGVuX2J1dHRvbo8BGAJtaW5lY3JhZnQ6aXJvbl9sZWdnaW" +
        "5nc7QCGQJtaW5lY3JhZnQ6Y2hlc3RfbWluZWNhcnTWAhABbWluZWNyYWZ0OmNvY29hfxECbWluZWNyYWZ0OmNhcnJvdIcDGgFtaW5lY3JhZnQ6ZGFya19vYWtfc3RhaXJzpAESAm1pbmVjcmFmdDpz" +
        "YXBsaW5nBhgBbWluZWNyYWZ0OmNvbW1hbmRfYmxvY2uJARECbWluZWNyYWZ0OmNvb2tpZeUCEQFtaW5lY3JhZnQ6cGlzdG9uISABbWluZWNyYWZ0OnN0YWluZWRfaGFyZGVuZWRfY2xheZ8BGQJtaW" +
        "5lY3JhZnQ6c3BlY2tsZWRfbWVsb27+AhQCbWluZWNyYWZ0OmNsYXlfYmFsbNECFwJtaW5lY3JhZnQ6Z29sZGVuX2FwcGxlwgIXAW1pbmVjcmFmdDpzdG9uZV9idXR0b25NGAJtaW5lY3JhZnQ6ZGlh" +
        "bW9uZF9ib290c7kCDwFtaW5lY3JhZnQ6cmFpbEIYAW1pbmVjcmFmdDp0cmFwcGVkX2NoZXN0kgEWAW1pbmVjcmFmdDpnb2xkZW5fcmFpbBsSAm1pbmVjcmFmdDpmdXJuYWNlPQ4CbWluZWNyYWZ0On" +
        "dlYh4PAm1pbmVjcmFmdDp2aW5lahABbWluZWNyYWZ0OmFudmlskQEYAm1pbmVjcmFmdDpsZWF0aGVyX2Jvb3RzrQIQAm1pbmVjcmFmdDpwYXBlctMCEQJtaW5lY3JhZnQ6cG9ydGFsWhIBbWluZWNy" +
        "YWZ0OnB1bXBraW5WDwFtaW5lY3JhZnQ6dmluZWoPAm1pbmVjcmFmdDpyYWlsQhEBbWluZWNyYWZ0OnNwb25nZRMSAm1pbmVjcmFmdDpmZWF0aGVyoAIZAm1pbmVjcmFmdDpkaWFtb25kX2hlbG1ldL" +
        "YCEQJtaW5lY3JhZnQ6Y2FycGV0qwEdAm1pbmVjcmFmdDpjaGFpbm1haWxfbGVnZ2luZ3OwAhsBbWluZWNyYWZ0OnBpc3Rvbl9leHRlbnNpb24kEQJtaW5lY3JhZnQ6YmVhY29uigEXAm1pbmVjcmFm" +
        "dDpyb3R0ZW5fZmxlc2jvAhgCbWluZWNyYWZ0OmNvb2tlZF9maXNoZWTeAhMCbWluZWNyYWZ0OnBhaW50aW5nwQIQAW1pbmVjcmFmdDp3aGVhdDsWAW1pbmVjcmFmdDpsaXRfZnVybmFjZT4XAW1pbm" +
        "VjcmFmdDpwdW1wa2luX3N0ZW1oGwFtaW5lY3JhZnQ6Y29iYmxlc3RvbmVfd2FsbIsBFwJtaW5lY3JhZnQ6YmFrZWRfcG90YXRviQMYAm1pbmVjcmFmdDpnb2xkZW5fc2hvdmVsnAIgAm1pbmVjcmFm" +
        "dDpzdGFpbmVkX2hhcmRlbmVkX2NsYXmfARYCbWluZWNyYWZ0Om1pbGtfYnVja2V0zwIdAW1pbmVjcmFmdDpyZWRfbXVzaHJvb21fYmxvY2tkIAJtaW5lY3JhZnQ6d29vZGVuX3ByZXNzdXJlX3BsYX" +
        "RlSA8CbWluZWNyYWZ0OmJvb2vUAhcBbWluZWNyYWZ0OmJyaWNrX3N0YWlyc2wRAm1pbmVjcmFmdDpzYWRkbGXJAhMBbWluZWNyYWZ0OmZhcm1sYW5kPA8CbWluZWNyYWZ0OmxhdmELFwJtaW5lY3Jh" +
        "ZnQ6YnJpY2tfc3RhaXJzbCABbWluZWNyYWZ0Ondvb2Rlbl9wcmVzc3VyZV9wbGF0ZUgZAm1pbmVjcmFmdDpnb2xkZW5fcGlja2F4ZZ0CGAFtaW5lY3JhZnQ6cXVhcnR6X3N0YWlyc5wBHwJtaW5lY3" +
        "JhZnQ6YnJvd25fbXVzaHJvb21fYmxvY2tjHwJtaW5lY3JhZnQ6Y2hhaW5tYWlsX2NoZXN0cGxhdGWvAg4CbWluZWNyYWZ0OmVnZ9gCGAFtaW5lY3JhZnQ6c3RpY2t5X3Bpc3Rvbh0SAW1pbmVjcmFm" +
        "dDpiZWRyb2NrBxUCbWluZWNyYWZ0Ondvb2Rlbl9ob2WiAhMCbWluZWNyYWZ0OmNvYWxfb3JlEBABbWluZWNyYWZ0OmdyYXNzAhkCbWluZWNyYWZ0OnJlZHN0b25lX3RvcmNoTBwCbWluZWNyYWZ0Om" +
        "RheWxpZ2h0X2RldGVjdG9ylwEOAW1pbmVjcmFmdDpsb2cRHAJtaW5lY3JhZnQ6bW9zc3lfY29iYmxlc3RvbmUwFAJtaW5lY3JhZnQ6ZW5kX3N0b25leRcBbWluZWNyYWZ0OmJpcmNoX3N0YWlyc4cB" +
        "GAFtaW5lY3JhZnQ6ZW1lcmFsZF9ibG9ja4UBFAFtaW5lY3JhZnQ6ZW5kX3N0b25leRICbWluZWNyYWZ0Omp1a2Vib3hUDgFtaW5lY3JhZnQ6d2ViHhACbWluZWNyYWZ0OmFudmlskQESAm1pbmVjcm" +
        "FmdDplbWVyYWxkhAMWAW1pbmVjcmFmdDpjb2JibGVzdG9uZQQdAm1pbmVjcmFmdDpyZWRfbXVzaHJvb21fYmxvY2tkGgJtaW5lY3JhZnQ6Z29sZGVuX2xlZ2dpbmdzvAIWAm1pbmVjcmFmdDpkaWFt" +
        "b25kX29yZTgRAW1pbmVjcmFmdDpob3BwZXKaAR0BbWluZWNyYWZ0OnVucG93ZXJlZF9yZXBlYXRlcl0bAm1pbmVjcmFmdDpjb2JibGVzdG9uZV93YWxsiwEVAm1pbmVjcmFmdDp3b29kZW5fYXhljw" +
        "IWAm1pbmVjcmFmdDpyZWNvcmRfd2FyZNkRGAJtaW5lY3JhZnQ6c3RhaW5lZF9nbGFzc18VAm1pbmVjcmFmdDpnbGFzc19wYW5lZhUBbWluZWNyYWZ0OnNub3dfbGF5ZXJOFQJtaW5lY3JhZnQ6cmVk" +
        "X2Zsb3dlciYPAW1pbmVjcmFmdDpsYXZhCxYCbWluZWNyYWZ0OmxhcGlzX2Jsb2NrFhkCbWluZWNyYWZ0OmRpYW1vbmRfc2hvdmVslQIUAW1pbmVjcmFmdDp3YXRlcmxpbHlvEgJtaW5lY3JhZnQ6Y2" +
        "Fycm90c40BEwFtaW5lY3JhZnQ6Z29sZF9vcmUOFAJtaW5lY3JhZnQ6c3RvbmVfaG9lowIPAm1pbmVjcmFmdDpjbGF5UhsCbWluZWNyYWZ0OmxlYXRoZXJfbGVnZ2luZ3OsAhYCbWluZWNyYWZ0OmZp" +
        "cmVfY2hhcmdlgQMYAW1pbmVjcmFmdDp3b29kZW5fYnV0dG9ujwETAm1pbmVjcmFmdDpnb2xkX29yZQ4VAm1pbmVjcmFmdDpmZW5jZV9nYXRlaxACbWluZWNyYWZ0OnJlZWRz0gIRAm1pbmVjcmFmdD" +
        "pjYWN0dXNRGAJtaW5lY3JhZnQ6d29vZGVuX3Nob3ZlbI0CEgFtaW5lY3JhZnQ6bGVhdmVzMqEBGAJtaW5lY3JhZnQ6eWVsbG93X2Zsb3dlciUWAm1pbmVjcmFmdDptb2Jfc3Bhd25lcjQPAm1pbmVj" +
        "cmFmdDpmaXNo3QIVAm1pbmVjcmFmdDpyZWNvcmRfZmFy1BEVAW1pbmVjcmFmdDpvYWtfc3RhaXJzNRQCbWluZWNyYWZ0OnNwYXduX2VnZ/8CEQFtaW5lY3JhZnQ6YmVhY29uigEQAW1pbmVjcmFmdD" +
        "pmZW5jZVUUAm1pbmVjcmFmdDppcm9uX2Rvb3LKAhQBbWluZWNyYWZ0OndhbGxfc2lnbkQWAW1pbmVjcmFmdDpwaXN0b25faGVhZCIVAm1pbmVjcmFmdDpjb2FsX2Jsb2NrrQEPAm1pbmVjcmFmdDpi" +
        "b25l4AIPAW1pbmVjcmFmdDp3b29sIxUBbWluZWNyYWZ0OmVuZF9wb3J0YWx3DgJtaW5lY3JhZnQ6ZHll3wIRAm1pbmVjcmFmdDpzaGVhcnPnAhQCbWluZWNyYWZ0OnJlY29yZF8xMdoREwFtaW5lY3" +
        "JhZnQ6b2JzaWRpYW4xEwJtaW5lY3JhZnQ6cG90YXRvZXOOARQCbWluZWNyYWZ0OmJvb2tzaGVsZi8TAW1pbmVjcmFmdDppcm9uX29yZQ8WAm1pbmVjcmFmdDpkaWFtb25kX2hvZaUCFAJtaW5lY3Jh" +
        "ZnQ6c291bF9zYW5kWBUCbWluZWNyYWZ0OnNsaW1lX2JhbGzVAg4CbWluZWNyYWZ0OmljZU8TAm1pbmVjcmFmdDpjYXVsZHJvbvwCHAJtaW5lY3JhZnQ6Y2Fycm90X29uX2Ffc3RpY2uOAxQCbWluZW" +
        "NyYWZ0Omdsb3dzdG9uZVkWAW1pbmVjcmFmdDptZWxvbl9ibG9ja2cPAm1pbmVjcmFmdDp3b29sIxYCbWluZWNyYWZ0Om1lbG9uX2Jsb2NrZxYCbWluZWNyYWZ0OmNvb2tlZF9iZWVm7AIYAm1pbmVj" +
        "cmFmdDpicmV3aW5nX3N0YW5k+wIWAW1pbmVjcmFmdDpsYXBpc19ibG9jaxYQAm1pbmVjcmFmdDpzdWdhcuECFgJtaW5lY3JhZnQ6YnJpY2tfYmxvY2stFgJtaW5lY3JhZnQ6ZW5kZXJfcGVhcmzwAh" +
        "cCbWluZWNyYWZ0OnRudF9taW5lY2FydJcDEAJtaW5lY3JhZnQ6d2F0ZXIJHAFtaW5lY3JhZnQ6ZG91YmxlX3N0b25lX3NsYWIrFQJtaW5lY3JhZnQ6c3RvbmVfc2xhYiwaAm1pbmVjcmFmdDpkYXJr" +
        "X29ha19zdGFpcnOkARgCbWluZWNyYWZ0Omp1bmdsZV9zdGFpcnOIARwBbWluZWNyYWZ0OmxpdF9yZWRzdG9uZV9sYW1wfBQCbWluZWNyYWZ0OmhheV9ibG9ja6oBGQFtaW5lY3JhZnQ6cmVkc3Rvbm" +
        "VfYmxvY2uYARECbWluZWNyYWZ0OmxhZGRlckEWAm1pbmVjcmFmdDpsaXRfZnVybmFjZT4UAm1pbmVjcmFmdDpmaXJld29ya3ORAxgBbWluZWNyYWZ0OmRldGVjdG9yX3JhaWwcHwFtaW5lY3JhZnQ6" +
        "dW5saXRfcmVkc3RvbmVfdG9yY2hLEAJtaW5lY3JhZnQ6Z3Jhc3MCGQJtaW5lY3JhZnQ6ZW5jaGFudGVkX2Jvb2uTAxUBbWluZWNyYWZ0OmZsb3dlcl9wb3SMAR0CbWluZWNyYWZ0OmRvdWJsZV93b2" +
        "9kZW5fc2xhYn0TAm1pbmVjcmFmdDpwb3JrY2hvcL8CHwJtaW5lY3JhZnQ6ZmVybWVudGVkX3NwaWRlcl9leWX4AhgBbWluZWNyYWZ0OmZsb3dpbmdfd2F0ZXIIEAJtaW5lY3JhZnQ6Y29jb2F/DgFt" +
        "aW5lY3JhZnQ6aWNlTxkCbWluZWNyYWZ0OmNvb2tlZF9jaGlja2Vu7gIQAW1pbmVjcmFmdDpyZWVkc1MVAm1pbmVjcmFmdDpxdWFydHpfb3JlmQEWAm1pbmVjcmFmdDpsYXZhX2J1Y2tldMcCDwFtaW" +
        "5lY3JhZnQ6Y2xheVIVAm1pbmVjcmFmdDppcm9uX3N3b3JkiwIYAm1pbmVjcmFmdDpzdG9uZV9waWNrYXhlkgIQAm1pbmVjcmFmdDpzdG9uZQETAm1pbmVjcmFmdDppcm9uX29yZQ8SAW1pbmVjcmFm" +
        "dDpzYXBsaW5nBhkBbWluZWNyYWZ0OmJyb3duX211c2hyb29tJxYCbWluZWNyYWZ0OnJlY29yZF9tYWxs1REbAW1pbmVjcmFmdDplbmNoYW50aW5nX3RhYmxldBECbWluZWNyYWZ0OnBpc3RvbiESAm" +
        "1pbmVjcmFmdDpkaWFtb25kiAIRAm1pbmVjcmFmdDpxdWFydHqWAxEBbWluZWNyYWZ0OmdyYXZlbA0TAW1pbmVjcmFmdDpjb2FsX29yZRAeAm1pbmVjcmFmdDpkaWFtb25kX2hvcnNlX2FybW9yowMZ" +
        "Am1pbmVjcmFmdDp3b29kZW5fcGlja2F4ZY4CIQJtaW5lY3JhZnQ6Y29tbWFuZF9ibG9ja19taW5lY2FydKYDFAFtaW5lY3JhZnQ6ZGlzcGVuc2VyFxUCbWluZWNyYWZ0OmNvbXBhcmF0b3KUAxACbW" +
        "luZWNyYWZ0OmZsaW50vgIQAW1pbmVjcmFmdDp0b3JjaDIQAW1pbmVjcmFmdDpzdG9uZQEXAm1pbmVjcmFmdDpkb3VibGVfcGxhbnSvARACbWluZWNyYWZ0OnN0aWNrmAIUAm1pbmVjcmFmdDpsYXBp" +
        "c19vcmUVFAJtaW5lY3JhZnQ6aXJvbl9iYXJzZRMCbWluZWNyYWZ0Om1pbmVjYXJ0yAIPAm1pbmVjcmFmdDpmaXJlMxkBbWluZWNyYWZ0OnJlZHN0b25lX3RvcmNoTA8CbWluZWNyYWZ0OmNha2XiAh" +
        "0CbWluZWNyYWZ0OnN0YWluZWRfZ2xhc3NfcGFuZaABFwJtaW5lY3JhZnQ6cmVjb3JkX3N0cmFk2BEZAm1pbmVjcmFmdDpjcmFmdGluZ190YWJsZToTAm1pbmVjcmFmdDpyZWRzdG9uZcsCFQJtaW5l" +
        "Y3JhZnQ6Z29sZGVuX2hvZaYCHQJtaW5lY3JhZnQ6c3RvbmVfYnJpY2tfc3RhaXJzbQ8BbWluZWNyYWZ0OmNha2VcEwJtaW5lY3JhZnQ6aXJvbl9heGWCAhUCbWluZWNyYWZ0OnBhY2tlZF9pY2WuAR" +
        "YCbWluZWNyYWZ0OnJlY29yZF9zdGFs1xEVAm1pbmVjcmFmdDpyZWNvcmRfY2F00REPAW1pbmVjcmFmdDpmaXJlMw==";
}
