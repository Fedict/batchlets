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
package be.fedict.batchlets.unpack;

import be.fedict.batchlets.test.BatchletTest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.batch.runtime.BatchStatus;
import org.jberet.runtime.JobExecutionImpl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Bart.Hanssens
 */

public class UnpackBatchletTest extends BatchletTest {
	@Rule
	public final TemporaryFolder tmp = new TemporaryFolder();

	@Before
	public void setUp() throws IOException {
		File zip = tmp.newFile("test.zip");

		try(ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zip))) {
			zout.putNextEntry(new ZipEntry("file1.txt"));
			byte[] data = "content1".getBytes();
			zout.write(data);
			zout.closeEntry();
	
			zout.putNextEntry(new ZipEntry("file2.txt"));
			data = "content2".getBytes();
			zout.write(data);
			zout.closeEntry();
		}
		tmp.newFolder("extract");
	}

	@Test
	public void testUnpackZip() throws Exception {
		String root = tmp.getRoot().toString();
		File file = Paths.get(root, "test.zip").toFile();
		File dir = Paths.get(root, "extract").toFile();
		Path file1 = Paths.get(root, "extract", "file1.txt");
		Path file2 = Paths.get(root, "extract", "file2.txt");

		Properties props = new Properties();
		props.putAll(Map.of("inputFile", file.toString(),
							"outputDir", dir.toString()));

		JobExecutionImpl execution = this.startBatchletJob("unpackBatchlet", props);
		execution.awaitTermination(10, TimeUnit.SECONDS);

		assertEquals(BatchStatus.COMPLETED, execution.getBatchStatus());
		assertTrue(file1 + " does not exist", Files.exists(file1));
		assertTrue(file2 + " does not exist", Files.exists(file2));
	}
}
