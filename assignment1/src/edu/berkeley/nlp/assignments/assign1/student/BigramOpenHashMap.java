package edu.berkeley.nlp.assignments.assign1.student;

import java.util.Arrays;

public class BigramOpenHashMap {
	private long[] keys;
	private int[] values;
	private int size = 0;
	private final long EMPTY_KEY = -1;
	private final double MAX_LOAD_FACTOR;
	
	// Record N[*vw] and N[vw*]
	private int[] x_vw;
	private int[] vw_x;
	
	public BigramOpenHashMap() {
		this(10);
	}

	public BigramOpenHashMap(int initialCapacity_) {
		this(initialCapacity_, 0.8);
	}

	public BigramOpenHashMap(int initialCapacity_, double loadFactor) {
		int cap = Math.max(5, (int) (initialCapacity_ / loadFactor));
		MAX_LOAD_FACTOR = loadFactor;
		values = new int[cap];
		keys = new long[cap];
		Arrays.fill(keys, EMPTY_KEY);
		x_vw = new int[cap];
		vw_x = new int[cap];
	}
	
	
	private void rehash() {
		long[] newKeys = new long[keys.length * 3 / 2];
		int[] newValues = new int[values.length * 3 / 2];
		int[] newx_vw = new int[x_vw.length * 3 / 2];
		int[] newvw_x = new int[vw_x.length * 3 / 2];
		
		Arrays.fill(newKeys, EMPTY_KEY);
		size = 0;
		for(int i = 0; i < keys.length; i++) {
			long curr = keys[i];
			if(curr != EMPTY_KEY) {
				int val = values[i];
				int x_vw_val = x_vw[i];
				int vw_x_val = vw_x[i];
				putHelp(curr, val, x_vw_val, vw_x_val, newKeys, newValues, newx_vw, newvw_x);
			}
		}
		keys = newKeys;
		values = newValues;
		x_vw = newx_vw;
		vw_x = newvw_x;
	}
	
	
	private int putHelp(long k, int v, int x_vw_val, int vw_x_val, long[] keyArray, int[] valueArray, int[] x_vwArray, int[] vw_xArray) {
		int initialPos = getInitialPos(k, keyArray);
		int finalPos = getFinalPos(k, keyArray, initialPos);
		long curr = keyArray[finalPos];
		
		if (curr == EMPTY_KEY) {
			size++;
			keyArray[finalPos] = k;
		}
		valueArray[finalPos] = v;
		x_vwArray[finalPos] = x_vw_val;
		vw_xArray[finalPos] = vw_x_val;
		return finalPos;
	}
	
	public int insert(long k) {
		if(size/(double)keys.length > MAX_LOAD_FACTOR) {
			rehash();
		}
		
		int initialPos = getInitialPos(k, keys);
		int finalPos = getFinalPos(k, keys, initialPos);
		long curr = keys[finalPos];
		
		
		if(curr == EMPTY_KEY) {
			size++;
			keys[finalPos] = k;
			values[finalPos] = 1;
		}else {
			values[finalPos] += 1;
		}
		return finalPos;
	}
	
	public void updateCount(long k, int xvw_pos) {
		int pos = this.getPosFromKey(k);
		vw_x[pos] += 1;
		x_vw[xvw_pos] += 1;
		
	}
	
	public int[] getXvw() {
		return x_vw;
	}
	public int[] getVwx() {
		return vw_x;
	}
	
	public long getKeyFromPos(int pos) {
		return keys[pos];
	}
	public int getPosFromKey(long k) {
		int initialPos = this.getInitialPos(k, keys);
		return getFinalPos(k, keys, initialPos);
	}
	public int getValueFromKey(long k) {
		int pos = getPosFromKey(k);
		return values[pos];
	}
	public long[] getKeys() {
		return keys;
	}
	public int[] getValues() {
		return values;
	}
	
	public int hash6432shift(long key) 
	{
		key = (~key) + (key << 18); 
		key = key ^ (key >>> 31);
		key = (key + (key << 2)) + (key << 4);
		key = key ^ (key >>> 11);
		key = key + (key << 6);
		key = key ^ (key >>> 22);
		return (int) key;
	}

	
	private int getInitialPos(long k, long[] keyArray) {
//		int hash = new Long(k).hashCode();
		int hash = hash6432shift(k);
		int pos = hash % keyArray.length;
		if (pos < 0) pos += keyArray.length;
		return pos;
	}
	private int getFinalPos(long k, long[] keyArray, int initialPos) {
		long curr = keyArray[initialPos];
		int finalPos = initialPos;
		int h = 1;
		while(curr != EMPTY_KEY && curr != k)
		{
			finalPos = ((initialPos + h) % keyArray.length);
			h = h + 1;
			curr = keyArray[finalPos];
		}
		return finalPos;
	}

	public int size() {
		return size;
	}
	
}
