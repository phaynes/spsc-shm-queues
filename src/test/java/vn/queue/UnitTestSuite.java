package vn.queue;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	TestSignalCatch.class,
	QueueConfigurationTest.class,
	SPSCMemoryMappedQueueTest.class
})


public class UnitTestSuite {

}
