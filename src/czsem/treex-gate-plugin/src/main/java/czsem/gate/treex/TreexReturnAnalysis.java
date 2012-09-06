package czsem.gate.treex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class TreexReturnAnalysis {

	protected List<Map<String,Object>> zones;
	protected Map<String, Map<String, Object>> nodeMap;
	protected Set<String> idAttributes;
	protected Set<String> listAttributes;

	@SuppressWarnings("unchecked")
	public TreexReturnAnalysis(Object treex_ret_param) {		
		zones = (List<Map<String, Object>>) treex_ret_param;
		
		nodeMap = new HashMap<String, Map<String,Object>>();

		for (Map<String, Object> zone : zones) {
			extractNodesFromList((List<Map<String, Object>>) zone.get("roots"), true);
			extractNodesFromList((List<Map<String, Object>>) zone.get("nodes"), false);			
		}
		
		idAttributes = findIdAttributes();
		listAttributes = findListAttributes();
		

	}

	public List<Map<String, Object>> getZones() {
		return zones;
	}

	public Map<String, Map<String, Object>> getNodeMap() {
		return nodeMap;
	}

	public Set<String> getIdAttributes() {
		return idAttributes;
	}

	public Set<String> getListAttributes() {
		return listAttributes;
	}

	public Set<String> getExcludeAttributes() {
		Set<String> exclude_attrs = new HashSet<String>(getListAttributes());
		exclude_attrs.addAll(getIdAttributes());
		
		exclude_attrs.add("czsemIsRoot");
		exclude_attrs.add("nodes");
		exclude_attrs.add("roots");
		exclude_attrs.add("wild_dump");
		
		return exclude_attrs;
	}

	protected Set<String> findListAttributes() {
		Set<String> keys = new HashSet<String>();
		
		for (Map<String, Object> node : nodeMap.values())
		{
			for (Entry<String, Object> entry : node.entrySet())
			{
				if (entry.getValue() instanceof List)
				{
					keys.add(entry.getKey());
				}
			}
		}		
		return keys;		
	}

	protected Set<String> findIdAttributes() {
		Set<String> keys = new HashSet<String>();
		
		for (Map<String, Object> node : nodeMap.values())
		{
			for (Entry<String, Object> entry : node.entrySet())
			{
				if (nodeMap.get(entry.getValue()) != null)
				{
					keys.add(entry.getKey());
				}
			}
		}		
		return keys;				
	}
	
	protected void extractNodesFromList(List<Map<String, Object>> node_list, boolean isRoot) {
		for (Map<String, Object> node : node_list)
		{
			node.put("czsemIsRoot", isRoot);
			nodeMap.put((String) node.get("id"), node);			
		}
		
	}


}
