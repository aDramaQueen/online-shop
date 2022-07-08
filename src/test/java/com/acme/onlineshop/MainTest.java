package com.acme.onlineshop;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MainTest {

	@Test
	void mainTest() {
		Assertions.assertFalse(Constants.DEBUG, "Debug flag is forbidden in production mode!");
	}

}