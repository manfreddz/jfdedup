package net.mejf.jfdedup;

public class DeviceInode extends Tuple<Long, Long> {
	public DeviceInode(Long device, Long inode) {
		super(device, inode);
	}
}
