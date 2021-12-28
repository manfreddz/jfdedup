package net.mejf.jfdedup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;

import static net.mejf.jfdedup.Main.debug;

@Getter
@ToString
public class Entry {
	public static final int BEGINNNING_AND_END = 1024;
	private final File file;
	private final int prio;
	private final long fileSize;
	private final long inode;
	private final long device;

	@ToString.Exclude
	private final DeviceInode deviceInode;

	@ToString.Exclude
	private final EntrySet entrySet;

	@ToString.Exclude
	private ByteArray startOfFile;
	private int startOfFileHash;

	@ToString.Exclude
	private ByteArray endOfFile;
	private int endOfFileHash;

	private ByteArray contentHash;

	private ContentAndSize contentAndSize;

	public Entry(File file, int prio, EntrySet entrySet) {
		this.file = file.getAbsoluteFile();
		final Map<String, Object> attributes;
		try {
			attributes = Files.readAttributes(file.toPath(), "unix:size,ino,dev", LinkOption.NOFOLLOW_LINKS);
		} catch (IOException e) {
			throw new RuntimeException("Exception while getting attributes for " + file.getPath(), e);
		}
		this.fileSize = (long) attributes.get("size");
		this.inode = (long) attributes.get("ino");
		this.device = (long) attributes.get("dev");
		this.deviceInode = new DeviceInode(getDevice(), getInode());
		this.prio = prio;
		this.entrySet = entrySet;
		debug(this.toString());
	}

	public static void construct(File file, boolean recursive, int prio, EntrySet entrySet) {
		if (!file.canRead()) {
			Main.warn("File/dir '%s' not readable. Skipping.", file.getPath());

		} else if (Files.isSymbolicLink(file.toPath())) {
			// TODO: Handle symbolic links
			Main.debug("File/dir '%s' is a symbolic link. Skipping.", file.getPath());

		} else if (file.isFile()) {
			entrySet.add(new Entry(file, prio, entrySet));

		} else if (file.isDirectory()) {
			try {
				Files.list(file.toPath())
						.map(Path::toFile)
						.forEach(fileInDirectory -> {
							if (!fileInDirectory.isDirectory() || recursive) {
								construct(fileInDirectory, recursive, prio, entrySet);
							}
						});
			} catch (IOException e) {
				throw new RuntimeException("Exception while getting directory content of " + file.getPath(), e);
			}

		} else {
			// Just ignore other stuff...
			Main.warn("Ignoring '%s', not sure what it is...", file.getPath());

		}
	}

	public void updateStartOfFile() {
		if (entrySet.getStartOfFileCache().containsKey(getDeviceInode())) {
			startOfFile = entrySet.getStartOfFileCache().get(getDeviceInode());

		} else {
			startOfFile = readStartOfFile();
			entrySet.getStartOfFileCache().put(getDeviceInode(), startOfFile);
		}
		startOfFileHash = startOfFile.hashCode();
	}

	private ByteArray readStartOfFile() {
		return new ByteArray(readBytes(fileSize < BEGINNNING_AND_END ? (int) fileSize : BEGINNNING_AND_END, 0));
	}

	public void updateEndOfFile() {
		final Map<DeviceInode, ByteArray> cache = entrySet.getEndOfFileCache();
		if (cache.containsKey(getDeviceInode())) {
			endOfFile = cache.get(getDeviceInode());

		} else {
			endOfFile = readEndOfFile();
			cache.put(getDeviceInode(), endOfFile);
		}
		endOfFileHash = endOfFile.hashCode();
	}

	private ByteArray readEndOfFile() {
		if (fileSize < BEGINNNING_AND_END) {
			return new ByteArray(readBytes((int) fileSize, 0));
		} else {
			return new ByteArray(readBytes(BEGINNNING_AND_END, file.length() - BEGINNNING_AND_END));
		}
	}

	private byte[] readBytes(int read, long skip) {
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			raf.seek(skip);
			return getBytesFoSho(raf, read);

		} catch (IOException e) {
			throw new RuntimeException("Failed to read from file " + file.getPath(), e);
		}
	}

	private byte[] getBytesFoSho(RandomAccessFile raf, int read) throws IOException {
		byte[] buf = new byte[read];
		int done = 0;
		do {
			done += raf.read(buf, done, buf.length - done);
		} while (done < buf.length);
		return buf;
	}

	public void updateContentHash() {
		final Map<DeviceInode, ByteArray> cache = entrySet.getContentHashCache();
		if (cache.containsKey(getDeviceInode())) {
			this.contentHash = cache.get(getDeviceInode());

		} else {
			this.contentHash = readContentHash();
			cache.put(getDeviceInode(), this.contentHash);
		}
	}

	private ByteArray readContentHash() {
		try (InputStream is = Files.newInputStream(file.toPath())) {
			byte[] array;
			array = DigestUtils.md5(is);
//			array = DigestUtils.sha3_512(is);
			return new ByteArray(array);

		} catch (IOException e) {
			throw new RuntimeException("Failed to get hash of file " + file.getPath(), e);
		}
	}

	public void makeContentAndSize() {
		this.contentAndSize = new ContentAndSize(startOfFile, endOfFile, contentHash, fileSize);
	}
}
