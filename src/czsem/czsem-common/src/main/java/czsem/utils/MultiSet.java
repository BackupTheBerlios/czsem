package czsem.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MultiSet<T> implements Iterable<T>
{
	private Map<T, Integer> map = new HashMap<T, Integer>();
	
	public String toFormatedString(String separator)
	{
		StringBuilder sb = new StringBuilder();
		
		for (T k : this)
		{
			int i = get(k);
			String outstr = "null";
			if (k != null) outstr = k.toString();
			sb.append(outstr);
			sb.append(": ");
			sb.append(i);
			sb.append(separator);
		}
		
		return sb.toString();
	}

	public void print(PrintStream out)
	{
		out.print(toFormatedString(", "));
	}

	public int size() {
		return map.keySet().size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public int get(T o) {
		Integer i = map.get(o);
		if (i == null) return 0;
		else return i;
	}

	public Iterator<T> iterator() {
		return map.keySet().iterator();
	}


	public int add(T e) {
		Integer i = get(e);		
		map.put(e, ++i);
		return i;
	}

	public int remove(T o) {
		Integer i = map.get(o);
		if (--i<=0)
		{
			map.remove(o);
			return 0;
		}
		else
		{
			map.put(o,i);
			return i;
		}
	}

	public Object[] toArray() {
		return map.keySet().toArray();
	}

	public T[] toArray(T[] a) {
		return map.keySet().toArray(a);
	}

	public void clear() {
		map.clear();		
	}

	public void addAll(Iterable<T> data)
	{
		for (T t : data)
		{
			add(t);
		}
	}

	public void addAll(T[] data, int count)
	{
		for (int i = 0; (i < data.length) && (i < count) ; i++)
		{
			add(data[i]);			
		}
		
	}

	public static class TopList<T>
	{
		public static class TopListEntry<T>
		{
			public TopListEntry(T t, int count2)
			{
				data = t;
				count = count2;
			}
			T data;
			int count = Integer.MIN_VALUE;
		}

		private TopListEntry<T>[] entries;
		
		@SuppressWarnings("unchecked")
		public TopList(int capacity)
		{
			entries = new TopListEntry[capacity+1];
		}
		
		@SuppressWarnings("rawtypes")
		public void add(T t, int count)
		{
			entries[0] = new TopListEntry<T>(t, count);
			Arrays.sort(entries, new Comparator<TopListEntry>() {

				@Override
				public int compare(TopListEntry o1, TopListEntry o2) {
					if (o1 == null)
					{
						if (o2 == null) return 0;
						else return -1;
					}
					if (o2 == null) return 1;
					return new Integer(o1.count).compareTo(o2.count) ;
				}
			});
		}
		
		public List<T> getTopKeys()
		{
			List<T> ret = new ArrayList<T>(entries.length-1);
			
			for (int i=0; i<entries.length-1; i++)
			{
				if (entries[entries.length-i-1] == null) break;
				ret.add(i, entries[entries.length-i-1].data);
			}
			
			return ret;
			
		}

		
	}
	
	public List<T> getTopKeys(int top_count)
	{
		TopList<T> top_list = new TopList<T>(top_count); 
				
		for (T k : map.keySet())
		{
			top_list.add(k, map.get(k));
		}
		
		return top_list.getTopKeys();		
	}
	
	public static void main(String[] args)
	{
		TopList<String> t = new TopList<String>(5);
		
		t.add("prvni", 3);
/*
		t.add("druhy", 3);
		t.add("sto", 100);
		t.add("treti", 2);
		t.add("ctvrty", 50);
*/	
		t.add("paty", 1);
		t.add(null, 7);
		
		List<String> k = t.getTopKeys();
		
		for (int i = 0; i < k.size(); i++)
		{
			System.err.println(k.get(i));			
		}
		
		
	}

}
