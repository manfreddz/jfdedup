package net.mejf.jfdedup;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ByteArray {
	byte[] array;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ByteArray byteArray = (ByteArray) o;
		return Arrays.equals(array, byteArray.array);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(array);
	}
}
