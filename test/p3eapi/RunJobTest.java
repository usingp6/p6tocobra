package p3eapi;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class RunJobTest {

	public RunJobTest() {
	}

	@Test
	public void testHasRightName() {

		String[] args ={"username","password","c:\\Program Files\\Deltek\\Cobra\\p3eapi", "MyProjectId","s"};

		RunJob job = new RunJob(new Parameters(args));
		assertEquals("Run", job.name());
		assertEquals(AbstractJob.class, job.getClass().getSuperclass());
	}
}
