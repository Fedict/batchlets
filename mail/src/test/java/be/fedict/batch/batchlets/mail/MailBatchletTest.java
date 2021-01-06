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

import be.fedict.batch.batchlets.test.BatchletTest;
import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.batch.runtime.BatchStatus;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import org.jberet.runtime.JobExecutionImpl;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Bart Hanssens
 */

public class MailBatchletTest extends BatchletTest {
	@Rule
	public GreenMailRule smtpServer = new GreenMailRule(ServerSetupTest.SMTP_POP3);

	@Test
	public void testMail() throws Exception {
		Properties props = new Properties();
		props.putAll(Map.of("server", "localhost",
							"port", "3025",
							"from", "test@example.com",
							"to", "recipient@example.com",
							"message", "hello",
							"subject", "OK"));

		JobExecutionImpl execution = startBatchletJob("mailBatchlet", props);
		execution.awaitTermination(10, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
	
		MimeMessage[] msgs = smtpServer.getReceivedMessages();
		assertEquals(1, msgs.length);
		assertEquals("OK", msgs[0].getSubject());
		assertEquals("recipient@example.com", msgs[0].getRecipients(Message.RecipientType.TO)[0].toString());
		assertEquals("test@example.com", msgs[0].getFrom()[0].toString());

	}
}
