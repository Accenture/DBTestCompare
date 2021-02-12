package uk.co.objectivity.test.db.comparators.printer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompareResult {
	public enum Test {
		PASSED, FAILED, CHECK_ALL_COLUMNS
	};

	private static int TESTSET_COLUMN_INDEX = 1;

	private String key;
	private List<String> rowFile;
	private List<String> rowDB;
	private Test test;

	public CompareResult(String key, List<String> rowFile, List<String> rowDB, Test test) {
		this.key = key;
		this.rowFile = rowFile.stream().collect(Collectors.toList());
		this.rowDB = rowDB.stream().collect(Collectors.toList());
		this.test = test;
	}

	public Test getTest() {
		return this.test;
	}

	public String getKey() {
		return key;
	}

	public List<String> getRowFile() {
		return rowFile;
	}

	public List<String> getRowDB() {
		return rowDB;
	}

	public List<List<String>> getRows() {
		List<List<String>> allRows = new ArrayList<>();
		allRows.add(rowFile);
		allRows.add(rowDB);
		return allRows;
	}

	public void setTestSets(Map<String, String> testSets) {
		String testSet = testSets.containsKey(key) ? testSets.get(key) : "N/A";
		rowFile.add(TESTSET_COLUMN_INDEX, testSet);
		rowDB.add(TESTSET_COLUMN_INDEX, testSet);
	}

}
