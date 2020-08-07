/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fedict.batchlets.sleep;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import org.jberet.runtime.JobExecutionImpl;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Bart.Hanssens
 */

public class SleepBatchletTest {
	@Test
	public void sleep() throws Exception {
		JobOperator operator = BatchRuntime.getJobOperator();
		long id = operator.start("sleepBatchlet", new Properties());
		JobExecutionImpl execution = (JobExecutionImpl) operator.getJobExecution(id);
		execution.awaitTermination(10, TimeUnit.SECONDS);
		assertEquals(execution.getBatchStatus(), BatchStatus.COMPLETED);
	}
}
