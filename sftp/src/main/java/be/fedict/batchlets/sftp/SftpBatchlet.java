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
package be.fedict.batchlets.sftp;

import bayern.steinbrecher.jsch.ChannelSftp;
import bayern.steinbrecher.jsch.JSch;
import bayern.steinbrecher.jsch.JSchException;
import bayern.steinbrecher.jsch.Session;
import bayern.steinbrecher.jsch.SftpException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.batch.api.AbstractBatchlet;
import javax.batch.api.BatchProperty;
import javax.batch.runtime.BatchStatus;
import javax.inject.Inject;

/**
 * Download or upload a file via SFTP
 * 
 * @author Bart Hanssens
 */
public class SftpBatchlet extends AbstractBatchlet {
	private Logger logger = Logger.getLogger(SftpBatchlet.class.getName());

	@Inject 
	@BatchProperty
	String fromSite;
	
	@Inject 
	@BatchProperty
	String fromFile;

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
	String toFile;

	@Inject 
	@BatchProperty
	String toUser;
	
	@Inject 
	@BatchProperty
	String toPass;
		
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

	private boolean download() throws JSchException, SftpException {
		logger.log(Level.INFO, "Download from server {0}", fromSite);
			
		if (fromUser == null || fromPass == null) {
			logger.severe("User or password is empty");
			return false;
		}

		session = sftp.getSession(fromSite, fromUser);
		session.setPassword(fromPass);
		session.connect();

		ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
		channel.connect();
		channel.get(fromFile, toFile);
		channel.disconnect();
		int status = channel.getExitStatus();
			
		session.disconnect();
			
		if (status != 0) {
			logger.log(Level.SEVERE, "Exit status {0}", status);
			return false;
		}
		return true;
	}

	private boolean upload() throws JSchException, SftpException {
		logger.log(Level.INFO, "Upload to server {0}", toSite);

		if (toUser == null || toPass == null) {
			logger.severe("User or password is empty");
			return false;
		}

		session = sftp.getSession(toSite, toUser);
		session.setPassword(toPass);
		session.connect();

		ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
		channel.connect();
		channel.put(fromFile, toFile);
		channel.disconnect();
		int status = channel.getExitStatus();

		session.disconnect();

		if (status != 0) {
			logger.log(Level.SEVERE, "Exit status {0}", status);
			return false;
		}
		return true;
	}

	@Override
	public String process() throws Exception {
		// check parameters 
		if (! checkParameters()) {
			return BatchStatus.FAILED.toString();	
		}
		
		sftp = new JSch();

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
