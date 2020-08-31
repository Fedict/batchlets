/*
 * Copyright (c) 2020, Bart Hanssens <bart.hanssens@bosa.fgov.be>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package be.fedict.batch.batchlets.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.batch.api.AbstractBatchlet;
import javax.batch.api.BatchProperty;
import javax.batch.runtime.BatchStatus;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

/**
 * Download or upload a file via SFTP (not both)
 * 
 * @author Bart Hanssens
 */
@Named
public class SftpBatchlet extends AbstractBatchlet {
	private static final Logger logger = Logger.getLogger(SftpBatchlet.class.getName());

	@Inject 
	@BatchProperty
	String fromSite;
	
	@Inject 
	@BatchProperty
	@PositiveOrZero
	int fromPort;

	@Inject 
	@BatchProperty
	@NotNull
	File fromFile;

	@Inject 
	@BatchProperty
	String fromUser;
	
	@Inject 
	@BatchProperty
	String fromPass;

	@Inject 
	@BatchProperty
	String toSite;
	
	@Inject 
	@BatchProperty
	@PositiveOrZero
	int toPort;

	@Inject 
	@BatchProperty
	@NotNull
	File toFile;

	@Inject 
	@BatchProperty
	String toUser;
	
	@Inject 
	@BatchProperty
	String toPass;

	@Inject 
	@BatchProperty
	boolean insecure;

	private JSch sftp;
	private Session session;

	private boolean checkParameters() {
		if (fromFile == null || toFile == null) {
			logger.severe("Missing source or destination file");
			return false;
		}
		if (fromSite == null && toSite == null) {
			logger.severe("Source and destination site are both empty, nothing to sftp");
			return false;
		}
		if (fromSite != null && toSite != null) {
			logger.severe("Source and destination site are both set, use a temp file");
			return false;
		}
		return true;
	}

	/**
	 * Open a session
	 * 
	 * @param site
	 * @param port
	 * @param user
	 * @param pass
	 * @throws JSchException 
	 */
	private void openSession(String site, int port, String user, String pass) throws JSchException {
		session = sftp.getSession(user, site);
		session.setPassword(pass);
		if (port > 0) {
			session.setPort(port);
		}
		session.connect();
	}

	/**
	 * Download file from SFTP server
	 * 
	 * @return
	 * @throws JSchException
	 * @throws SftpException 
	 */
	private boolean download() throws JSchException, SftpException {
		logger.log(Level.INFO, "Download {0} from server {1}", new String[] { fromFile.toString(), fromSite });
			
		if (fromUser == null || fromPass == null) {
			logger.severe("User or password is empty");
			return false;
		}
		openSession(fromSite, fromPort, fromUser, fromPass);

		ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
		channel.connect();
		channel.get(fromFile.toString(), toFile.toString());
		channel.disconnect();

		session.disconnect();

		return true;
	}

	/**
	 * Upload file to SFTP server
	 * 
	 * @return
	 * @throws JSchException
	 * @throws SftpException 
	 */
	private boolean upload() throws JSchException, SftpException {
		logger.log(Level.INFO, "Upload {0} to server {1}", new String[] { fromFile.toString(), toSite });

		if (toUser == null || toPass == null) {
			logger.severe("User or password is empty");
			return false;
		}
		if (!fromFile.exists()) {
			logger.severe("File to upload not found");
			return false;
		}
		openSession(toSite, toPort, toUser, toPass);

		ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
		channel.connect();
		channel.put(fromFile.toString(), toFile.toString());
		channel.disconnect();

		session.disconnect();

		return true;
	}

	@Override
	public String process() throws Exception {
		// check parameters 
		if (! checkParameters()) {
			return BatchStatus.FAILED.toString();	
		}
		
		sftp = new JSch();
		if (insecure) {
			sftp.setConfig("StrictHostKeyChecking", "no");
		}

		try {
			if (fromSite != null && !download()) {
				return BatchStatus.FAILED.toString();
			}

			if (toSite != null && !upload()) {
				return BatchStatus.FAILED.toString();
			}
		} catch(JSchException | SftpException e) {
			logger.log(Level.SEVERE, "Exception {0}", e.getMessage());

			if (session != null) {
				session.disconnect();
			}
			return BatchStatus.FAILED.toString();
		}
		
		return BatchStatus.COMPLETED.toString();
	}
	
	@Override
	public void stop() {
		if (session != null) {
			session.disconnect();
		}
	}	
}
