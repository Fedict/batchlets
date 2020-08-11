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
package be.fedict.batchlets.sleep;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.batch.api.AbstractBatchlet;
import javax.batch.api.BatchProperty;
import javax.batch.runtime.BatchStatus;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.Min;

/**
 * Sleeps for a number of seconds.
 * 
 * @author Bart Hanssens
 */
@Named
public class SleepBatchlet extends AbstractBatchlet {
	private static final Logger logger = Logger.getLogger(SleepBatchlet.class.getName());

	private ScheduledExecutorService scheduler;

	@Inject
	@BatchProperty
	@Min(1)
	int delay;

	@Override
	public String process() throws Exception {
		logger.log(Level.INFO, "Sleeping for {0} seconds", delay);
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.awaitTermination(delay, TimeUnit.SECONDS);
		scheduler.shutdown();
		return BatchStatus.COMPLETED.toString();
	}
	
	@Override
	public void stop() {
		scheduler.shutdownNow();
	}
}
