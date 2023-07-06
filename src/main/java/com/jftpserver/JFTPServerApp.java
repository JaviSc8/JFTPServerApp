package com.jftpserver;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.swing.JFileChooser;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.*;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WriteRequest;

public class JFTPServerApp {
    public static void main(String[] args) {
        int port = 2221; // Puerto del servidor FTP
        String username = "javier"; // Nombre de usuario del servidor FTP
        String password = "jrive"; // Contraseña del servidor FTP
        String baseDirectory = selectDirectory(); // Ruta base del sistema de ficheros que se expondrá

        // Crea la fábrica del servidor FTP
        FtpServerFactory serverFactory = new FtpServerFactory();

        // Configura el listener del servidor FTP
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(port);
        listenerFactory.setServerAddress(getLocalIpAddress());
        serverFactory.addListener("default", listenerFactory.createListener());

        // Crea el gestor de usuarios
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();

        // Crea un usuario con permisos de lectura y escritura en la ruta base
        BaseUser user = new BaseUser();
        user.setName(username);
        user.setPassword(password);
        user.setHomeDirectory(baseDirectory);
        user.setEnabled(true);
        AuthorizationRequest authorizationRequest = new WriteRequest();
        user.authorize(authorizationRequest);
        try {
			userManager.save(user);
		} catch (FtpException e) {
			System.out.println("Existe algún problema al crear el usuario");
			e.printStackTrace();
		}

        // Configura el gestor de usuarios del servidor FTP
        serverFactory.setUserManager(userManager);

        FtpServer ftpServer = serverFactory.createServer();
        try {
            ftpServer.start();
            System.out.println("Servidor FTP en ejecución en " + getLocalIpAddress() +":"+ port);
        } catch (FtpException e) {
            System.out.println("Error al iniciar el servidor FTP: " + e.getMessage());
        }
    }
    
    private static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isLoopbackAddress()) {
                        continue;
                    }
                    if (address.getHostAddress().contains(":")) {
                        continue; // Ignorar direcciones IPv6
                    }
                    return address.getHostAddress();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static String selectDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        } else {
            System.exit(0);
            return null;
        }
    }
}
