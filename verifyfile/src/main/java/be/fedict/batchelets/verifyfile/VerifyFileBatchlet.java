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
package be.fedict.batchelets.verifyfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.batch.api.AbstractBatchlet;
import javax.batch.api.BatchProperty;
import javax.batch.runtime.BatchStatus;
import javax.inject.Inject;
import javax.inject.Named;


/**
 * Verify a file name / file size
 * 
 * @author Bart Hanssens
 */
@Named
public class VerifyFileBatchlet extends AbstractBatchlet {
	private static final Logger logger = Logger.getLogger(VerifyFileBatchlet.class.getName());

	@Inject
	@BatchProperty
	File file;

	@Inject
	@BatchProperty
	Pattern filterPattern;

	@Inject
	@BatchProperty
	Pattern matchPattern;

	@Inject
	@BatchProperty
	File directory;

	@Inject
	@BatchProperty
	long minSize;

	@Inject
	@BatchProperty
	long maxSize;

	@Inject
	@BatchProperty
	Date minDate;

	@Inject
	@BatchProperty
	Date maxDate;

	/**
	 * Get the file or list of files to validate
	 * 
	 * @return array of files or null
	 */
	private File[] getFiles() {
		if (directory == null && file == null) {
			logger.log(Level.SEVERE, "No file or directory to check");
			return null;
		}

		if (file != null) {
			if (directory == null) {
				return new File[] { file };
			}
			return new File[] { Paths.get(directory.toString(), file.toString()).toFile() };
		}
		if (directory != null) {
			if (filterPattern == null) {
				return directory.listFiles(f -> f.isFile());
			}
			return directory.listFiles(f -> f.isFile() && filterPattern.matcher(f.getName()).matches());
		}
		return null;
	}

	/**
	 * Verify if a file exists
	 */
	private boolean checkExists(File f)  {
		if (!file.exists()) {
			logger.log(Level.SEVERE, "File {0} does not exist", file);
			return false;
		}
		return true;
	}

	/**
	 * Verify the size of a file
	 * 
	 * @param f
	 * @return 
	 */
	private boolean checkSize(File f)  {
		if (minSize == 0 && maxSize == 0) {
			return true;
		}

		try {
			long size = Files.size(f.toPath());
			if (size >= minSize && size <= maxSize) {
				return true;
			} else {
				logger.log(Level.SEVERE, "Incorrect file size {0} for {1}", 
											new String[] { String.valueOf(size), file.toString() });
				return false;
			}
		} catch (IOException ioe) {
			logger.log(Level.SEVERE, "Error getting filesize for {0}", file);
			return false;
		}
	}

	private boolean checkName(File f) {
		if (matchPattern == null) {
			return true;
		}
		if (!matchPattern.matcher(f.toString()).matches()) {
			logger.log(Level.SEVERE, "No match for {0}", file); 
			return false;
		}
		return true;
	}

	private boolean checkDate(File f) {
		if (minDate == null && maxDate == null) {
			return true;
		}
		long modified = f.lastModified();

		if (minDate != null && modified < minDate.getTime()) {
			logger.log(Level.SEVERE, "File {0} too old", file); 
			return false;
		}
		if (maxDate != null && modified > maxDate.getTime()) {
			logger.log(Level.SEVERE, "File {0} too new", file); 
			return false;
		}
		return true;
	}

	@Override
	public String process() throws Exception {
		logger.log(Level.INFO, "Start file verification");
		
		File[] files = getFiles();

		if (files == null || files.length == 0) {
			logger.log(Level.SEVERE, "No files found");
			return BatchStatus.FAILED.toString();
		}

		for (File f: files) {
			if (!checkSize(f)) {
				return BatchStatus.FAILED.toString();
			}
			if (!checkName(f)) {
				return BatchStatus.FAILED.toString();
			}
			if (!checkDate(f)) {
				return BatchStatus.FAILED.toString();
			}
		}
		return BatchStatus.COMPLETED.toString();
	}
}
