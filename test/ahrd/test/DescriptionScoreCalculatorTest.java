package ahrd.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ahrd.model.BlastResult;
import ahrd.model.Protein;

public class DescriptionScoreCalculatorTest {

	public DescriptionScoreCalculatorTest() {
		super();
	}

	@Before
	public void setUp() throws IOException {
		TestUtils.initTestSettings();
	}

	@Test
	public void testRelativeBlastScore() {
		// Mock test-data
		Protein p = TestUtils.mockProtein();
		// br has bit-score 30.0
		BlastResult br = TestUtils.mockBlastResult();
		p.getDescriptionScoreCalculator().setMaxBitScore(60.0);
		// test: (0.2 * 30.0 / 60.0)
		assertEquals(0.1,
				p.getDescriptionScoreCalculator().relativeBlastScore(br), 0.0);
	}

	@Test
	public void testDescriptionScore() {
		Protein p = new Protein("sweet_sheep_protein", 200);
		// and mock LexicalScoreCalculator
		p.setLexicalScoreCalculator(new TestUtils.LexicalScoreCalculatorMock(p));
		BlastResult br = TestUtils.mockBlastResult("accession", 1.0,
				"goat sheep wool", 10, 20, 10, 20, 200, 30.0, "swissprot",
				new HashSet<String>(Arrays.asList("goat", "sheep", "wool")));
		List<BlastResult> brs = new ArrayList<BlastResult>();
		brs.add(br);
		p.getBlastResults().put("swissprot", brs);
		p.getDescriptionScoreCalculator().setMaxBitScore(30);

		// Token-Scores are not needed, as the lexical score is mocked!
		// DescriptionScore(0.9) := mockedLexicalScore(0.70) + 0.2 *
		// BitScore(30/30)
		p.getDescriptionScoreCalculator().calcDescriptionScore(br);
		assertEquals(0.9, br.getDescriptionScore(), 0.000000001);
	}

	@Test
	public void testFindHighestScoringBlastResult() {
		Protein p = TestUtils.mockProtein();
		// Sprot
		p.getBlastResults().put("swissprot",
				TestUtils.mockBlastResultsForDescCalcTest());
		// trEMBL
		p.getBlastResults().put(
				"trembl",
				Arrays.asList(TestUtils.mockBlastResult(
						"accession_5",
						5.0,
						"description_5 Fly-Wing formation",
						10,
						20,
						10,
						20,
						200,
						30.0,
						"trembl",
						new HashSet<String>(Arrays.asList("description", "5",
								"fly", "wing", "formation")))));
		p.setLexicalScoreCalculator(new TestUtils.LexicalScoreCalculatorMock(p));
		p.getDescriptionScoreCalculator().setMaxBitScore(30.0);
		// Token-Scores are not needed, as the lexical score is mocked!
		// test
		p.getDescriptionScoreCalculator().findHighestScoringBlastResult();
		// 0.7 (mocked) + 0.4 * 30/30
		assertEquals(1.1, p.getDescriptionScoreCalculator()
				.getDescriptionHighScore(), 0.0000001);
		assertEquals("description_5 Fly-Wing formation", p
				.getDescriptionScoreCalculator().getHighestScoringBlastResult()
				.getDescription());
	}
}
