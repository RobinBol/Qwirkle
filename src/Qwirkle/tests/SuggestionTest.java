package qwirkle.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import qwirkle.gamelogic.Board;
import qwirkle.gamelogic.StoneType;
import qwirkle.gamelogic.Suggestion;

public class SuggestionTest {

	private Suggestion s;
	private Board board;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		List<StoneType> types = new ArrayList<>();
		types.add(new StoneType('A', 'A'));
		types.add(new StoneType('A', 'B'));
		types.add(new StoneType('A', 'C'));
		s = new Suggestion(0,0);
		s.addType(types, 4);
		System.out.println(s);
	}

	@Test
	public void addTypesTest() {
		List<StoneType> types = new ArrayList<>();
		types.add(new StoneType('A', 'A'));
		s.addType(types, 1);
		
		assertEquals(5, s.getScore());
		
		System.out.println(s);
	}
	
	@Test
	public void addSuggestionsTest() {
		board.createTestMap2();
		board.buildSuggestionMap();
		System.out.println(board);
		
		
	}

}
