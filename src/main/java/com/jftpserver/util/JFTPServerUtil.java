package com.jftpserver.util;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jftpserver.JFTPServerApp;

public class JFTPServerUtil {
	
	private static final Logger log = LoggerFactory.getLogger(JFTPServerUtil.class);
	private static final long INACTIVITY_TIMEOUT = 600000; 
	private static final String VIRTUAL_BOX_DISPLAYNAME = "VirtualBox";
	private static Timer inactivityTimer;
	
    private JFTPServerUtil() {
		super();
	}

	public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.getDisplayName().contains(VIRTUAL_BOX_DISPLAYNAME) || networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isLoopbackAddress()) {
                        continue;
                    }
                    if (address instanceof Inet4Address) {
                        String ipAddress = address.getHostAddress();
                        if (checkInternetAccess(ipAddress)) {
                            return ipAddress;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
	private static boolean checkInternetAccess(String ipAddress) {
	    try {
	        InetAddress inetAddress = InetAddress.getByName(ipAddress);
	        return inetAddress.isReachable(5000); 
	    } catch (IOException e) {
	        return false;
	    }
	}

	
    public static String selectDirectory() {
    	log.info("Seleccione Directorio");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
        	log.info("Directorio seleccionado");
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        } else if(result == JFileChooser.CANCEL_OPTION){
        	log.warn("Tiene que seleccionar un directorio, intente de nuevo por favor");
        	selectDirectory();
        	return null;
        }else {
            System.exit(0);
            return null;
        }
    }
    
    public static void startInactivityTimer() {
    	cancelInactivityTimer();

        inactivityTimer = new Timer();
        inactivityTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (JFTPServerApp.getConnectionCounter().get() == 0) {
                    log.info("Cerrando el servidor FTP por inactividad");
                    JFTPServerApp.getFtpServer().stop();
                    cancelInactivityTimer();
                }
            }
        }, INACTIVITY_TIMEOUT);
    }

    public static void cancelInactivityTimer() {
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
        }
    }
}
