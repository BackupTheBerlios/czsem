package czsem.fs.query.restrictions;

import czsem.fs.query.FSQuery.QueryData;

public class Restrictioin {
	public boolean evalaute(QueryData data, int nodeID) {
		return true;
	}
	
	public static Restrictioin createRestriction(String comparartor, String arg1,	String arg2) {
		if (comparartor.equals("="))
		{
			return new EqualRestrictioin(arg1, arg2);
		}
		
		throw new RuntimeException(String.format("Restricition ont supported: %s", comparartor));
	}

	
	
	public static class AttrRestrictioin extends Restrictioin {
		protected String attr, value;

		public AttrRestrictioin(String attr, String value) {
			this.attr = attr;
			this.value = value;
		}
	}

	public static class EqualRestrictioin extends AttrRestrictioin {

		public EqualRestrictioin(String attr, String value) {
			super(attr, value);
		}

		@Override
		public boolean evalaute(QueryData data, int nodeID) {
			Object v = data.getNodeAttributes().getValue(nodeID, attr);
			if (v == null) return false;
			return value.equals(v.toString());
		}
	}

}