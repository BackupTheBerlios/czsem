package czsem.fs;

import java.util.Map;

public interface NodeAttributes 
{
	Iterable<Map.Entry<String, Object>> get(int node_id);
	Object getValue(int node_id, String attrName);
}