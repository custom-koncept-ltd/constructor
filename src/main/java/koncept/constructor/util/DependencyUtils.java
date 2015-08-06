package koncept.constructor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DependencyUtils {

	private DependencyUtils() {
	}
	
	public static List<String> unrollDependencies(String target, Map<String, List<String>> dependencies) {
		Map<Integer, List<String>> distances = buildDistanceMap(target, 0, dependencies, new HashMap<Integer, List<String>>());
		List<String> unrolled = new ArrayList<>();
		for(int i = distances.size() - 1; i >= 0; i--) {
			List<String> requirements = distances.get(i);
			for(String requirement: requirements) {
				if (!unrolled.contains(requirement))
					unrolled.add(requirement);
			}
		}
		return unrolled;
	}
	
	public static List<String> orderDependencies(Collection<String> targets, Map<String, List<String>> dependencies) {
		Map<Integer, List<String>> distances = new HashMap<Integer, List<String>>();
		for(String target: targets)
			buildDistanceMap(target, 0, dependencies, distances);
		List<String> unrolled = new ArrayList<>();
		for(int i = distances.size() - 1; i >= 0; i--) {
			List<String> requirements = distances.get(i);
			for(String requirement: requirements) {
				if (!unrolled.contains(requirement))
					unrolled.add(requirement);
			}
		}
		return unrolled;
	}
	
	/**
	 * Builds a dependency distance map.<br>
	 * The higher the number, the further is is from the initial target<br>
	 * <br>
	 * @param target
	 * @param distance
	 * @param dependencies
	 * @param distances
	 * @return
	 */
	private static Map<Integer, List<String>> buildDistanceMap(String target, int distance, Map<String, List<String>> dependencies, Map<Integer, List<String>> distances) {
		List<String> requirements = distances.get(distance);
		if (requirements == null) {
			requirements = new ArrayList<>();
			distances.put(distance, requirements);
		}
		requirements.add(target);

		requirements = dependencies.get(target);
		if (requirements != null)
			for(String requirement: requirements)
				buildDistanceMap(requirement, distance + 1, dependencies, distances);
		return distances;
		
	}
}
