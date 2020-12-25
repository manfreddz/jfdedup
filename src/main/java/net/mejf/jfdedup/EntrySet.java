package net.mejf.jfdedup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

public class EntrySet implements Iterable<Entry> {

	List<Entry> entryList = new ArrayList<>();
	Set<String> absolutePathSet = new HashSet<>();
	@Getter
	private Map<DeviceInode, ByteArray> startOfFileCache = new HashMap<>();
	@Getter
	private Map<DeviceInode, ByteArray> endOfFileCache = new HashMap<>();
	@Getter
	private Map<DeviceInode, ByteArray> contentHashCache = new HashMap<>();

	public void add(Entry entry) {
		final String absolutePath = entry.getFile().getAbsolutePath();

		if (!absolutePathSet.contains(absolutePath)) {
			entryList.add(entry);
			absolutePathSet.add(absolutePath);
		}
	}

	public int size() {
		return entryList.size();
	}

	@Override
	public Iterator<Entry> iterator() {
		return entryList.iterator();
	}

	public void removeAll(List<Entry> toRemove) {
		for (Entry entry : toRemove) {
			remove(entry);
		}
	}

	public void remove(Entry entry) {
		entryList.remove(entry);
		absolutePathSet.remove(entry.getFile().getAbsolutePath());
	}

	public void sortLargestFirst() {
		Collections.sort(entryList, (o1, o2) -> -Long.compare(o1.getFileSize(), o2.getFileSize()));
	}
}
