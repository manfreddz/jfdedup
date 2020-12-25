package net.mejf.jfdedup;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ContentAndSize {
	private final ByteArray startOfFile;
	private final ByteArray contentHash;
	private final ByteArray endOfFile;
	private final long fileSize;

	public ContentAndSize(ByteArray startOfFile, ByteArray endOfFile, ByteArray contentHash, long fileSize) {
		this.startOfFile = startOfFile;
		this.endOfFile = endOfFile;
		this.contentHash = contentHash;
		this.fileSize = fileSize;
	}
}
