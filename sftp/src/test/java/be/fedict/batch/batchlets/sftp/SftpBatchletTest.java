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

import be.fedict.batch.batchlets.test.BatchletTest;
import com.github.stefanbirkner.fakesftpserver.rule.FakeSftpServerRule;
import java.io.BufferedReader;
import java.io.File;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.batch.runtime.BatchStatus;
import org.jberet.runtime.JobExecutionImpl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Bart.Hanssens
 */

public class SftpBatchletTest extends BatchletTest {
	@Rule
	public final FakeSftpServerRule server = new FakeSftpServerRule().setPort(1234).addUser("joe", "pass");

	@Rule
	public final TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void testDownload() throws Exception {
		server.putFile("/file.txt", "dummy", StandardCharsets.UTF_8);

		Path file = Paths.get(tmp.getRoot().toString(), "file.txt");

		Properties props = new Properties();
		props.putAll(Map.of("fromSite", "localhost",
							"fromPort", "1234",
							"fromFile", "/file.txt",
							"fromUser", "joe",
							"fromPass", "pass",
							"insecure", "true",
							"toFile", file.toString()));

		JobExecutionImpl execution = startBatchletJob("sftpBatchlet", props);
		execution.awaitTermination(10, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
		assertTrue(file + " does not exist", Files.exists(file));
	}
	
	@Test
	public void testUpload() throws Exception {
		File file = tmp.newFile("file.txt");
		try (OutputStream os = Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE)) {
			os.write("dummy".getBytes(StandardCharsets.UTF_8));
		}

		Properties props = new Properties();
		props.putAll(Map.of("toSite", "localhost",
							"toPort", "1234",
							"toFile", "/file.txt",
							"toUser", "joe",
							"toPass", "pass",
							"insecure", "true",
							"fromFile", file.toString()));

		JobExecutionImpl execution = startBatchletJob("sftpBatchlet", props);
		execution.awaitTermination(10, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
		assertTrue("File.txt does not exist", server.existsFile("/file.txt"));		
	}
}
