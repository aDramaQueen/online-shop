package com.acme.onlineshop.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IPLoader {

    private static final Logger logger = LoggerFactory.getLogger(IPLoader.class);

    private IPLoader() { }

    /**
     * <p><b>Trys</b> to give you the IP address of current server machine/platform.</p>
     * <p>The problem here is, a server can have multiple network interfaces. Which one is the right one? Even so,
     * if this application runs behind a proxy server, there is no way to get the addresses from your server
     * (e.g.: NGINX) programmatically.</p>
     * <p>Therefore there is a simple fallback for this method: Return the loop back address, a.k.a. "local host"</p>
     *
     * @param serverProperties {@link ServerProperties} with pre-configured settings from properties file for local server
     * @return The IP address of local machine/platform
     */
    public static String getRootURL(ServerProperties serverProperties) {
        InetAddress inetAddress = serverProperties.getAddress();
        if(inetAddress != null) {
            // Only look further if found address is "any address" e.g.: in Ipv4 = "0.0.0.0" / IPv6 = "0.0.0.0.0.0.0.0" or "::"
            if (inetAddress.isAnyLocalAddress()) {
//                try {
//                    Set<InetAddress> allNetworkInterfaces = getAllNetworkInterfaces();
//                } catch (SocketException e) {
//                    logger.error("Couldn't find any network interface, this is odd...");
//                }
                try {
                    inetAddress = InetAddress.getLocalHost();
                } catch (UnknownHostException exc) {
                    logger.error("Couldn't use network interface. Using fallback mechanism...");
                    inetAddress = InetAddress.getLoopbackAddress();
                }
            }
            if (serverProperties.getSsl().isEnabled()) {
                return "https://%s:%d".formatted(inetAddress.getHostAddress(), serverProperties.getPort());
            } else {
                return "http://%s:%d".formatted(inetAddress.getHostAddress(), serverProperties.getPort());
            }
        } else {
            //For internal tests
            return "http://127.0.0.1:8080";
        }
    }

    /**
     * Gives addresses to <b>ALL</b> <a href="https://docs.oracle.com/javase/tutorial/networking/nifs/definition.html">
     * network interfaces</a> of local machine/platform
     *
     * @return A {@link Set} of {@link InetAddress}es of all {@link NetworkInterface}s
     */
    private static Set<InetAddress> getAllNetworkInterfaces() {
        Iterator<NetworkInterface> it = null;
        try {
            it = NetworkInterface.getNetworkInterfaces().asIterator();
        } catch (SocketException exc) {
            logger.error("Couldn't load addresses for network interfaces from local machine/platform", exc);
        }
        if(it != null) {
            HashSet<InetAddress> result = new HashSet<>();
            while (it.hasNext()) {
                Enumeration<InetAddress> networkAddress = it.next().getInetAddresses();
                while (networkAddress.hasMoreElements()) {
                    result.add(networkAddress.nextElement());
                }
            }
            return result;
        } else {
            return new HashSet<>();
        }
    }
}
