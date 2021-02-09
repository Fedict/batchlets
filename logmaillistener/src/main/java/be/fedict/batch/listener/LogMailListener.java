/*
 * Copyright (c) 2020, FPS BOSA DG DT
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
package be.fedict.batch.listener;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.batch.api.BatchProperty;
import javax.batch.api.listener.JobListener;
import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 *
 * @author Bart Hanssens <bart.hanssens@bosa.fgov.be>
 */
public class LogMailListener implements JobListener {
	@Inject 
	@BatchProperty
	@Email
	@NotNull
	@NotBlank
	String from;
	
	@Inject
	@BatchProperty
	@Email
	@NotNull
	@NotBlank
	String to;
	
	@Inject 
	@BatchProperty
	@NotNull
	@NotBlank
	String subject;

	@Inject 
	@BatchProperty
	@NotNull
	@NotBlank
	String message;
		
	@Inject 
	@BatchProperty
	@NotBlank
	String server;
	
	@Inject 
	@BatchProperty
	@Positive
	Integer port;

	private static final String FILE = "batch.log";

	@Override
	public void beforeJob() throws Exception {
		FileHandler fh = new FileHandler(FILE);
		fh.setFormatter(new SimpleFormatter());
		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).addHandler(fh);
	}

	@Override
	public void afterJob() throws Exception {
		Properties props = new Properties();
		props.put("mail.smtp.host", (server != null) ? server : "localhost" );
		if (port != null) {
			props.put("mail.smtp.port", port);
		}
			
		Session session = Session.getInstance(props);
		
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(from);
		msg.setRecipients(Message.RecipientType.TO, to);
		msg.setSubject(subject);
		msg.setText(message, StandardCharsets.UTF_8.toString());

		BodyPart part = new MimeBodyPart();
		part.setDataHandler(new DataHandler(new FileDataSource(FILE)));
		part.setFileName(FILE);
		Multipart multi = new MimeMultipart();
		multi.addBodyPart(part);

		msg.setContent(multi);

		Transport.send(msg);
	}
}
