package koncept.constructor.util;

import static java.util.Arrays.asList;
import static koncept.constructor.util.DependencyUtils.orderDependencies;
import static koncept.constructor.util.DependencyUtils.unrollDependencies;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class DependencyUtilsTest {

	Map<String, List<String>> dependencies;
	
	@Before
	public void init() {
		dependencies = dependencies();
	}
	
	@Test
	public void trivialUnroll() {
		assertThat(unrollDependencies("0", dependencies), is(asList("0")));
		assertThat(unrollDependencies("9", dependencies), is(asList("9")));
	}
	
	@Test
	public void simpleUnroll() {
		assertThat(unrollDependencies("8", dependencies), is(asList("9", "8")));
	}
	
	@Test
	public void transientUnroll() {
		assertThat(unrollDependencies("3", dependencies), is(asList("9", "5", "6", "3")));
		assertThat(unrollDependencies("4", dependencies), is(asList("9", "7", "8", "4")));
	}
	
	@Test
	public void orderSimpleDependencies() {
		List<String> expected = asList("9", "5", "6", "7", "8", "3", "4", "2");
		assertThat(orderDependencies(asList("3", "4", "2"), dependencies), is(expected));
		assertThat(orderDependencies(asList("2", "3", "4"), dependencies), is(expected));
		assertThat(orderDependencies(asList("2", "4", "3"), dependencies), is(expected));
		
		
	}
	
	private Map<String, List<String>> dependencies() {
		Map<String, List<String>> dependencies = new HashMap<>();
		dependencies.put("1", asList("2", "3"));
		dependencies.put("2", asList("3", "4"));
		dependencies.put("3", asList("5", "6"));
		dependencies.put("4", asList("7", "8"));
		dependencies.put("6", asList("9"));
		dependencies.put("8", asList("9"));
		return dependencies;
	}
}
