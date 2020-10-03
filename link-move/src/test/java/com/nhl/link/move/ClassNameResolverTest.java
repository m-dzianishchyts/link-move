package com.nhl.link.move;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ClassNameResolverTest {

	@Test
	public void testTypeForName() throws ClassNotFoundException {

		assertSame(String.class, ClassNameResolver.typeForName("java.lang.String"));
		assertSame(Integer.class, ClassNameResolver.typeForName("java.lang.Integer"));
		assertSame(byte[].class, ClassNameResolver.typeForName("byte[]"));
		assertSame(Integer.TYPE, ClassNameResolver.typeForName("int"));
	}
}
