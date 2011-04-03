package com.brousalis.mtm;

import java.lang.reflect.Type;
import java.util.HashMap;
/**
 * Item that can appear on the trail
 * @author ericstokes
 *
 */
public interface TrailItem {
	
	public HashMap<String, Type> getRequiredNodes();
}
