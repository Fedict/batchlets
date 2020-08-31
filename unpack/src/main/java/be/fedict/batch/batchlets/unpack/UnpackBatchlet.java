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
package be.fedict.batch.batchlets.unpack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.batch.api.AbstractBatchlet;
import javax.batch.api.BatchProperty;
import javax.batch.runtime.BatchStatus;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Unpack a ZIP (or other compressed file)
 * 
 * @author Bart Hanssens
 */
@Named
public class UnpackBatchlet extends AbstractBatchlet {
	private static final Logger logger = Logger.getLogger(UnpackBatchlet.class.getName());

	@Inject
	@BatchProperty
	@NotNull
	File inputFile;

	@Inject
	@BatchProperty
	@NotNull
	File outputDir;

	@Override
	public String process() throws Exception {
		logger.log(Level.INFO, "Unpack {0} to {1}", new File[] { inputFile, outputDir } );

		try (InputStream in = new BufferedInputStream(Files.newInputStream(inputFile.toPath()), 16 * 1024);
			ArchiveInputStream archive = new ArchiveStreamFactory().createArchiveInputStream(in)) {

			ArchiveEntry entry = archive.getNextEntry();
			while (entry != null) {
				File outfile = Paths.get(outputDir.toString(), entry.getName()).toFile();
				logger.log(Level.FINER, "Extracting {0} to {1}", new String[] { entry.getName(), outfile.toString() });

				if (entry.isDirectory()) {
					if (!outfile.isDirectory() && ! outfile.mkdirs()) {
						logger.log(Level.SEVERE, "Could not create {0}", outfile);
						return BatchStatus.FAILED.toString();
					} 
				} else {
					File parent = outfile.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						logger.log(Level.SEVERE, "Could not create {0}", parent);
						return BatchStatus.FAILED.toString();
					}
					try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(outfile.toPath()), 16 * 1024)) {
						IOUtils.copy(archive, out);
					}
				}
				entry = archive.getNextEntry();
			}
		}
		return BatchStatus.COMPLETED.toString();
	}
}
