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

package be.fedict.batchlets.verifyfile;

import be.fedict.batchlets.test.BatchletTest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class VerifyFileBatchletTest extends BatchletTest {
	@Rule
	public final TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void download() throws Exception {
		Path file = Paths.get(tmp.getRoot().toString(), "file.txt");
	
		Properties props = new Properties();
		props.putAll(Map.of("fromSite", "localhost",
							"fromPort", "1234",
							"fromFile", "/dir/file.txt",
							"fromUser", "joe",
							"fromPass", "pass",
							"insecure", "true",
							"toFile", file.toString()));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(10, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
		assertTrue(file + " does not exist", Files.exists(file));
	}
	
	
	public void upload() throws Exception {
		server.putFile("/dir/file.txt", "dummy", StandardCharsets.UTF_8);
		
	}
}
