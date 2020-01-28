/**
 * 
 */
package com.hulles.alixia.alpha;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.qa.junit5.TestController;

/**
 * @author hulles
 *
 */
@DisplayName("Testing AlixiaAlpha.AlphaRoom")
class AlphaRoomTest {
	private final static Logger LOGGER = Logger.getLogger("AlixiaAlpha.AlphaRoom");
	private AlphaRoom alphaRoom;
	private TestController controller;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	void setUpBeforeClass() throws Exception {
		
		controller = new TestController();
		alphaRoom = new AlphaRoom();
		controller.startRoom(alphaRoom);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	void tearDownAfterClass() throws Exception {
		
		alphaRoom = null;
		controller.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
		
	}

	@Test
	@DisplayName("Test WhatSememes")
	void testWhatSememes() {
		Set<SerialSememe> sememes;
		SerialSememe sememe;
		
		LOGGER.info("Test WhatSememes");
		controller.sendWhatSememes();
		while (!controller.gotWhatSememes()) {}
		sememes = controller.getSememesForRoom(alphaRoom);
		assertTrue(sememes.size() == 2, "Sememes size s/b 2");
		sememe = SerialSememe.find("sememe_analysis");
		assertTrue(sememes.contains(sememe));
		sememe = SerialSememe.find("aardvark");
		assertTrue(sememes.contains(sememe));
	}

}
