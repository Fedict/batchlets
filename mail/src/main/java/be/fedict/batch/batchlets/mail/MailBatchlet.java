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
package be.fedict.batch.batchlets.mail;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.batch.api.AbstractBatchlet;
import javax.batch.api.BatchProperty;
import javax.batch.runtime.BatchStatus;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * Sends an email.
 * 
 * @author Bart Hanssens
 */
@Named
public class MailBatchlet extends AbstractBatchlet {
	private static final Logger logger = Logger.getLogger(MailBatchlet.class.getName());
	
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
	
	@Override
	public String process() throws Exception {
		logger.log(Level.INFO, "Mailing {0} to {1}", new String[] { subject, to });
			
		Properties props = new Properties();
		props.put("mail.smtp.host", (server != null) ? server : "localhost" );
		if (port != null) {
			props.put("mail.smtp.port", port);
		}
			
		Session session = Session.getInstance(props);
		try {			
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(from);
			msg.setRecipients(Message.RecipientType.TO, to);
			msg.setSubject(subject);
			msg.setText(message, StandardCharsets.UTF_8.toString());

			Transport.send(msg);
		} catch (MessagingException ex) {
			logger.log(Level.SEVERE, ex.getMessage());
			return BatchStatus.FAILED.toString();
		}

		return BatchStatus.COMPLETED.toString();
	}
}
