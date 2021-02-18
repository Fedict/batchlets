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

package be.fedict.batch.batchlets.verifyfile;

import be.fedict.batch.batchlets.test.BatchletTest;
import java.io.File;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.Period;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.batch.runtime.BatchStatus;
import org.jberet.runtime.JobExecutionImpl;
import static org.junit.Assert.assertEquals;
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
	public void testExistFile() throws Exception {
		tmp.newFile("file1.txt");
	
		Properties props = new Properties();
		props.put("file", Paths.get(tmp.getRoot().toString(), "file1.txt").toString());

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);
		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
	}

	@Test
	public void testExistFileDirectory() throws Exception {
		tmp.newFile("file2.txt");
	
		Properties props = new Properties();
		props.putAll(Map.of("file", "file2.txt", "directory", tmp.getRoot().toString()));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);
		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
	}

	@Test
	public void testFileDoesNotExist() throws Exception {
		Properties props = new Properties();
		props.put("file", "file3.txt");
		
		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.FAILED, execution.getBatchStatus());
	}

	@Test
	public void testFileMatchStart() throws Exception {
		tmp.newFile("file4.txt");
	
		Properties props = new Properties();
		props.putAll(Map.of("file", "file4.txt", 
							"directory", tmp.getRoot().toString(),
							"matchStart", "file"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
	}

	@Test
	public void testFileDoesNotMatchStart() throws Exception {
		tmp.newFile("file5.txt");
	
		Properties props = new Properties();
		props.putAll(Map.of("file", "file5.txt", 
							"directory", tmp.getRoot().toString(),
							"matchStart", "nomatch"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.FAILED, execution.getBatchStatus());
	}	
@Test
	public void testFileMatchEnd() throws Exception {
		tmp.newFile("file6.txt");
	
		Properties props = new Properties();
		props.putAll(Map.of("file", "file6.txt", 
							"directory", tmp.getRoot().toString(),
							"matchEnd", "txt"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
	}

	@Test
	public void testFileDoesNotMatchEnd() throws Exception {
		tmp.newFile("file7.txt");
	
		Properties props = new Properties();
		props.putAll(Map.of("file", "file7.txt", 
							"directory", tmp.getRoot().toString(),
							"matchEnd", "nomatch"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.FAILED, execution.getBatchStatus());
	}	

	@Test
	public void testFileMatches() throws Exception {
		tmp.newFile("file8.txt");
	
		Properties props = new Properties();
		props.putAll(Map.of("file", "file8.txt", 
							"directory", tmp.getRoot().toString(),
							"matchPattern", "^.*txt$"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
	}

	@Test
	public void testFileDoesNotMatch() throws Exception {
		tmp.newFile("file9.txt");
	
		Properties props = new Properties();
		props.putAll(Map.of("file", "file9.txt", 
							"directory", tmp.getRoot().toString(),
							"matchPattern", "^nomatch$"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.FAILED, execution.getBatchStatus());
	}

	@Test
	public void testAllFilesMatch() throws Exception {
		tmp.newFile("file10a.txt");
		tmp.newFile("file10b.txt");

		Properties props = new Properties();
		props.putAll(Map.of("directory", tmp.getRoot().toString(),
							"matchPattern", "^.*txt"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
	}

	@Test
	public void testSomeFilesDoNotMatch() throws Exception {
		tmp.newFile("file11a.txt");
		tmp.newFile("file11b.zip");

		Properties props = new Properties();
		props.putAll(Map.of("directory", tmp.getRoot().toString(),
							"matchPattern", "^.*txt"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.FAILED, execution.getBatchStatus());
	}
	
	@Test
	public void testFilesMatchFiltered() throws Exception {
		tmp.newFile("file12a.txt");
		tmp.newFile("file12b.zip");

		Properties props = new Properties();
		props.putAll(Map.of("directory", tmp.getRoot().toString(),
							"filterPattern", ".*txt$",
							"matchPattern", "file12a.*"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
	}
	
	@Test
	public void testFilesDoNotMatchFiltered() throws Exception {
		tmp.newFile("file13a.txt");
		tmp.newFile("file13b.zip");

		Properties props = new Properties();
		props.putAll(Map.of("directory", tmp.getRoot().toString(),
							"filterPattern", ".*txt$",
							"matchPattern", "nomatch*"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.FAILED, execution.getBatchStatus());
	}
	
	@Test
	public void testFilesAllModifiedOk() throws Exception {
		File f14a = tmp.newFile("file14a.txt");
		File f14b = tmp.newFile("file14b.zip");

		Date old = Date.from(Instant.parse("2001-01-01T23:00:00.00Z"));
		f14a.setLastModified(old.getTime());
		f14b.setLastModified(old.getTime());

		Properties props = new Properties();
		props.putAll(Map.of("directory", tmp.getRoot().toString(),
							"minDate",  "31/12/2000",
							"maxDate", "31/12/2001"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
	}
	
	@Test
	public void testFilesSomeModifiedNotOk() throws Exception {
		File f15a = tmp.newFile("file15a.txt");
		File f15b = tmp.newFile("file15b.zip");

		Date old = Date.from(Instant.parse("2001-01-01T23:00:00.00Z"));
		f15a.setLastModified(old.getTime());
		Date toonew = Date.from(Instant.parse("2020-01-01T23:00:00.00Z"));
		f15b.setLastModified(toonew.getTime());

		Properties props = new Properties();
		props.putAll(Map.of("directory", tmp.getRoot().toString(),
							"minDate",  "31/12/2000",
							"maxDate", "31/12/2001"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.FAILED, execution.getBatchStatus());
	}

	@Test
	public void testFilesAllAgeOk() throws Exception {
		File f16a = tmp.newFile("file16a.txt");
		File f16b = tmp.newFile("file16b.zip");

		Date old = Date.from(Instant.now().minus(Period.ofDays(60)));
		f16a.setLastModified(old.getTime());
		f16b.setLastModified(old.getTime());
		
		Properties props = new Properties();
		props.putAll(Map.of("directory", tmp.getRoot().toString(),
							"minAgeDays", "0",
							"maxAgeDays", "120"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
	}

	@Test
	public void testFilesSomeAgeNotOk() throws Exception {
		File f17a = tmp.newFile("file17a.txt");
		File f17b = tmp.newFile("file17b.zip");

		Date old = Date.from(Instant.now().minus(Period.ofDays(150)));
		f17a.setLastModified(old.getTime());
		
		Properties props = new Properties();
		props.putAll(Map.of("directory", tmp.getRoot().toString(),
							"minAgeDays", "0",
							"maxAgeDays", "120"));

		JobExecutionImpl execution = startBatchletJob("verifyFileBatchlet", props);
		execution.awaitTermination(4, TimeUnit.SECONDS);

		assertEquals(BatchStatus.FAILED, execution.getBatchStatus());
	}
}
