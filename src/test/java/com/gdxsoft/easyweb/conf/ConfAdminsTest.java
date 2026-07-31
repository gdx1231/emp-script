package com.gdxsoft.easyweb.conf;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.ConfValueResolver;
import com.gdxsoft.easyweb.utils.ConfValueResolvers;
import com.gdxsoft.easyweb.utils.DefaultConfValueResolver;

class ConfAdminsTest {

	private final DefaultConfValueResolver resolver = new DefaultConfValueResolver();

	// --- resolve tests (end-to-end via resolver) ---

	@Test
	void testResolveEnvVariable() {
		String pathEnv = System.getenv("PATH");
		assertNotNull(pathEnv);
		String result = resolver.resolve("${env.PATH}");
		assertEquals(pathEnv, result);
	}

	@Test
	void testResolveSystemProperty() {
		String home = System.getProperty("user.home");
		String result = resolver.resolve("${user.home}");
		assertEquals(home, result);
	}

	@Test
	void testResolvePlainText() {
		assertEquals("myPassword123", resolver.resolve("myPassword123"));
	}

	@Test
	void testResolveNull() {
		assertNull(resolver.resolve(null));
	}

	@Test
	void testResolveMixedContent() {
		String home = System.getProperty("user.home");
		String result = resolver.resolve("prefix-${user.home}-suffix");
		assertEquals("prefix-" + home + "-suffix", result);
	}

	@Test
	void testResolveUnknownVariableLeftAsIs() {
		String result = resolver.resolve("${nonexistent.xyz}");
		assertEquals("${nonexistent.xyz}", result);
	}

	// --- resolver loading ---

	@Test
	void testGetResolverDefault() {
		ConfValueResolvers.setResolver(null);
		ConfValueResolver r = ConfValueResolvers.getResolver();
		assertTrue(r instanceof DefaultConfValueResolver);
	}

	@Test
	void testSetCustomResolver() {
		ConfValueResolver custom = raw -> "custom-value";
		ConfValueResolvers.setResolver(custom);
		try {
			assertEquals("custom-value", ConfValueResolvers.getResolver().resolve("anything"));
		} finally {
			ConfValueResolvers.setResolver(null);
		}
	}

	@Test
	void testGetResolverInvalidClass() {
		String original = System.getProperty(ConfValueResolvers.PROP_RESOLVER);
		try {
			System.setProperty(ConfValueResolvers.PROP_RESOLVER, "com.nonexistent.Resolver");
			ConfValueResolvers.setResolver(null);
			ConfValueResolver r = ConfValueResolvers.getResolver();
			assertTrue(r instanceof DefaultConfValueResolver);
		} finally {
			if (original != null) {
				System.setProperty(ConfValueResolvers.PROP_RESOLVER, original);
			} else {
				System.clearProperty(ConfValueResolvers.PROP_RESOLVER);
			}
			ConfValueResolvers.setResolver(null);
		}
	}
}
