package net.mejf.jfdedup;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Tuple<T1, T2> {
	T1 a;
	T2 b;
}
