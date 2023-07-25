package com.jftpserver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jftpserver.ftplet.CustomFtplet;
import com.jftpserver.util.JFTPServerUtil;
/**
 * JFTPServerApp
 * 
 * Anonymous server, to establish a username and password:
 * 
 * 	user.setName("User");
 * 	user.setPassword("Password");
 * 
 */
public class JFTPServerApp {
	private static final Logger log = LoggerFactory.getLogger(JFTPServerApp.class);
	private static AtomicInteger connectionCounter = new AtomicInteger(0);
	private static FtpServer ftpServer;
	  
    public static AtomicInteger getConnectionCounter() {
		return connectionCounter;
	}

	public static FtpServer getFtpServer() {
		return ftpServer;
	}

	public static void main(String[] args) {
        int port = 2121;
        String baseDirectory = JFTPServerUtil.selectDirectory();

        FtpServerFactory serverFactory = new FtpServerFactory();

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(port);
        listenerFactory.setServerAddress(JFTPServerUtil.getLocalIpAddress());
        
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);
        
        serverFactory.addListener("default", listenerFactory.createListener());
        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();

        BaseUser user = new BaseUser();
        user.setName("anonymous");
        user.setHomeDirectory(baseDirectory);
        user.setEnabled(true);
        AuthorizationRequest authorizationRequest = new WriteRequest();
        user.authorize(authorizationRequest);
        try {
			userManager.save(user);
		} catch (FtpException e) {
			log.warn("Error al crear el usuario {}", e.getMessage());
		}

        serverFactory.setUserManager(userManager);

        Map<String, Ftplet> ftpletMap = new HashMap<>();
        CustomFtplet customFtplet = new CustomFtplet();
        ftpletMap.put("CustomFtplet", customFtplet);
        serverFactory.setFtplets(ftpletMap);

        ftpServer = serverFactory.createServer();
        try {
            ftpServer.start();       
            log.info("Servidor FTP ejecutandose: {}:{}", JFTPServerUtil.getLocalIpAddress(), port);
        } catch (FtpException e) {
            log.error("Error al iniciar el servidor FTP: "+ e.getMessage(), e);
        } 
    }
}
