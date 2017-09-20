package edu.berkeley.nlp.assignments.assign1.student;

import java.util.Arrays;


public class TrigramOpenHashMap {
	private long[] keys;
	private int[] values;
	private int size = 0;
	private final long EMPTY_KEY = -1;
	private final double MAX_LOAD_FACTOR;
	
	public TrigramOpenHashMap() {
		this(10);
	}

	public TrigramOpenHashMap(int initialCapacity_) {
		this(initialCapacity_, 0.8);
	}

	public TrigramOpenHashMap(int initialCapacity_, double loadFactor) {
		int cap = Math.max(5, (int) (initialCapacity_ / loadFactor));
		MAX_LOAD_FACTOR = loadFactor;
		values = new int[cap];
		keys = new long[cap];
		Arrays.fill(keys, EMPTY_KEY);
	}
	
	private void rehash() {
		long[] newKeys = new long[keys.length * 3 / 2];
		int[] newValues = new int[values.length * 3 / 2];
		Arrays.fill(newKeys, EMPTY_KEY);
		size = 0;
		for(int i = 0; i < keys.length; i++) {
			long curr = keys[i];
			if(curr != EMPTY_KEY) {
				int val = values[i];
				putHelp(curr, val, newKeys, newValues);
			}
		}
		
		keys = newKeys;
		values = newValues;
	}
	
	private int putHelp(long k, int v, long[] keyArray, int[] valueArray) {
		int initialPos = getInitialPos(k, keyArray);
		int finalPos = getFinalPos(k, keyArray, initialPos);
		long curr = keyArray[finalPos];

		valueArray[finalPos] = v; 
		if (curr == EMPTY_KEY) {
			size++;
			keyArray[finalPos] = k;
		}
		return finalPos;
	}
	public int insert(long k) {
		if(size/(double)keys.length > MAX_LOAD_FACTOR) {
			rehash();
		}
		
		int initialPos = getInitialPos(k, keys);
		int finalPos = getFinalPos(k, keys, initialPos);
		long curr = keys[finalPos];
		
		values[finalPos] += 1;
		if(curr == EMPTY_KEY) {
			size++;
			keys[finalPos] = k;
		}
		
		return finalPos;
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
	
	
	public int hash6432shift(long key) // source: https://gist.github.com/badboy/6267743
	{
		key = (~key) + (key << 18); // key = (key << 18) - key - 1;
		key = key ^ (key >>> 31);
		key = key * 21; // key = (key + (key << 2)) + (key << 4);
		key = key ^ (key >>> 11);
		key = key + (key << 6);
		key = key ^ (key >>> 22);
		return (int) key;
	}

	
	private int getInitialPos(long k, long[] keyArray) {
//		int hash = Long.hashCode(k);
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
