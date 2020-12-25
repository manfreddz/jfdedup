package net.mejf.jfdedup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class Eliminations {
	private static void eliminate(EntrySet entrySet, Function<Entry, Boolean> ifTrue) {
		List<Entry> toRemove = new LinkedList<>();
		for (Entry entry : entrySet) {
			if (ifTrue.apply(entry)) {
				toRemove.add(entry);
			}
		}

		Main.debug("Will remove: %s", toRemove.toString());

		entrySet.removeAll(toRemove);
	}

	static void eliminateWithZeroSize(EntrySet entrySet) {
		eliminate(entrySet, (Entry entry) -> entry.getFileSize() == 0);
	}

	static void eliminateWithUniqueSize(EntrySet entrySet) {
		Map<Long, Set<DeviceInode>> sizeInodeMapList = new HashMap<>();
		int done = 0;
		for (Entry entry : entrySet) {
			Main.progress(done++, entrySet.size());
			Set<DeviceInode> deviceInodeSet = sizeInodeMapList.get(entry.getFileSize());

			if (deviceInodeSet == null) {
				deviceInodeSet = new HashSet<>();
				sizeInodeMapList.put(entry.getFileSize(), deviceInodeSet);
			}

			deviceInodeSet.add(entry.getDeviceInode());
		}

		eliminate(entrySet, entry -> sizeInodeMapList.get(entry.getFileSize()).size() == 1);
	}

	static void eliminateWithUniqueStartOfFile(EntrySet entrySet) {
		eliminateUniqueInodesBasedOn(entrySet, Entry::updateStartOfFile, Entry::getStartOfFile);
	}

	static void eliminateWithUniqueEndOfFile(EntrySet entrySet) {
		eliminateUniqueInodesBasedOn(entrySet, Entry::updateEndOfFile, Entry::getEndOfFile);
	}

	public static void eliminateWithUniqueHash(EntrySet entrySet) {
		eliminateUniqueInodesBasedOn(entrySet, Entry::updateContentHash, Entry::getContentHash);
	}

	private static <T> void eliminateUniqueInodesBasedOn(EntrySet entrySet, Consumer<Entry> entryPrepper, Function<Entry, T> basedOn) {
		Map<T, Set<DeviceInode>> inodeMap = new HashMap<>();
		int done = 0;
		for (Entry entry : entrySet) {
			Main.progress(done++, entrySet.size());
			entryPrepper.accept(entry);

			inodeMap
					.computeIfAbsent(basedOn.apply(entry), k -> new HashSet<>())
					.add(entry.getDeviceInode());
		}

		eliminate(entrySet, entry -> inodeMap.get(basedOn.apply(entry)).size() == 1);
	}

	public static void eliminateOnContentAndSize(EntrySet entryList) {
		eliminateUniqueInodesBasedOn(entryList, Entry::makeContentAndSize, Entry::getContentAndSize);
	}
}
