package amuse.keras;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KerasControllerTest {

	private KerasController k;
	
	@BeforeAll
	void init() {
		k = KerasController.getInstance();
	}
	
	@BeforeEach
	void loadFiles() {
		k.init();
	}
	
	@Test
	void TestShosResults() {
		assertTrue(k.showFiles());
	}
}
