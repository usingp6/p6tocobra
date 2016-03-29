package p3eapi;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class NullJobTest {

	public NullJobTest() {
	}

	@Test
	public void testHasRightName() {

		String[] args ={"username","c:\\Program Files\\Deltek\\Cobra\\p3eapi", "MyProjectId","s","true"};

		NullJob job = new NullJob(new Parameters(args), new P6Connection());
		assertEquals("Null", job.name());
		assertEquals(Job.class, job.getClass().getSuperclass());
	}
}
