package com.jftpserver.ftplet;

import java.io.IOException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jftpserver.util.JFTPServerUtil;
import com.jftpserver.JFTPServerApp;
public class CustomFtplet implements Ftplet {

	private static final Logger log = LoggerFactory.getLogger(CustomFtplet.class);

	@Override
	public void init(FtpletContext ftpletContext) throws FtpException {
		JFTPServerUtil.startInactivityTimer();
	}

	@Override
	public void destroy() {	
		log.info("Servidor FTP cerrado");
	}

	@Override
	public FtpletResult beforeCommand(FtpSession session, FtpRequest request) throws FtpException, IOException {
		return FtpletResult.DEFAULT;
	}

	@Override
	public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply)
			throws FtpException, IOException {
		return FtpletResult.DEFAULT;
	}

	@Override
	public FtpletResult onConnect(FtpSession session) throws FtpException, IOException {
	    int currentConnections = JFTPServerApp.getConnectionCounter().incrementAndGet();
	    log.info("Conexiones activas: {}", currentConnections);
	    if (currentConnections != 0) JFTPServerUtil.cancelInactivityTimer();
		return FtpletResult.DEFAULT;
	}

	@Override
	public FtpletResult onDisconnect(FtpSession session) throws FtpException, IOException {
		int currentConnections = JFTPServerApp.getConnectionCounter().decrementAndGet();
		log.info("Conexiones activas: {}", currentConnections);
		if(currentConnections == 0) JFTPServerUtil.startInactivityTimer();
		return FtpletResult.DEFAULT;
	}
}
