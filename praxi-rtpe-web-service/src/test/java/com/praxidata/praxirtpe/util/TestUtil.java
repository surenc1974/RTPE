/**
 * 
 */
package com.praxidata.praxirtpe.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.praxidata.praxirtpe.repo.ESRepository;

/**
 * @author SURENDRANATH
 *
 */
public class TestUtil {
	ObjectMapper mapper;
	String assetIndex = "praxiasset";
	Integer[] allIds = new Integer[] { 0, 345, 245, 589, 89, 34, 789 };
	ESRepository esRepo;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		esRepo = new ESRepository("oem01.praxidata.cloud", 9200, 9201, "http", "praxiasset");
		mapper = new ObjectMapper();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		mapper = null;
		esRepo = null;
	}

	/**
	 * Test method for
	 * {@link com.praxidata.praxirtpe.util.Util#getAllCombinationsOfIds(java.lang.Integer[])}.
	 */
	@Test
	public void testGetAllCombinationsOfIds() {
		for (Integer[] aCombo : Util.getAllCombinationsOfIds(allIds)) {
			String aComboStr = "";
			for (Integer anID : aCombo) {
				if (!aComboStr.equals("")) {
					aComboStr = aComboStr + "," + anID;
				} else {
					aComboStr = aComboStr + anID;
				}

			}
			System.out.println(aComboStr.trim());
		}
	}

	/**
	 * Test method for
	 * {@link com.praxidata.praxirtpe.util.Util#getAllComplexAssetsAndConstituentTermsForBusinessTermSet(com.fasterxml.jackson.databind.node.ArrayNode, com.fasterxml.jackson.databind.ObjectMapper, com.praxidata.praxirtpe.repo.ESRepository)}.
	 */
	@Test
	public void testGetAllComplexAssetsAndConstituentTermsForBusinessTermSet() {
		try {
			ArrayNode bTermsArray = (ArrayNode) mapper
					.readTree("[{\"id\":7189715,\"uid\":\"94f42f64-7da6-11ea-84b9-02927b5bea00\"}]");
			System.out.println(
					Util.getAllComplexAssetsAndConstituentTermsForBusinessTermSet(bTermsArray, mapper, esRepo));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for
	 * {@link com.praxidata.praxirtpe.util.Util#buildComplexAssetSearchQueryBody(java.lang.Long, com.fasterxml.jackson.databind.ObjectMapper)}.
	 */
	@Test
	public void testComplexAssetSearchBuildQueryBody() {
		try {
			System.out.println(Util.buildComplexAssetSearchQueryBody(Long.parseLong("1"), mapper));
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for
	 * {@link com.praxidata.praxirtpe.util.Util#buildAssetSearchQueryForMultipleIDs(com.fasterxml.jackson.databind.ObjectMapper,java.util.List,String)}.
	 */
	@Test
	public void testBuildAssetSearchQueryForMultipleIDs() {
		try {
			List<Integer> assetIds = new ArrayList<Integer>();
			assetIds.add(7236812);
			System.out.println(Util.buildAssetSearchQueryForMultipleIDs(mapper, assetIds, assetIndex));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for
	 * {@link com.praxidata.praxirtpe.util.Util#buildAndAddBTermToArray(com.fasterxml.jackson.databind.node.ArrayNode,com.fasterxml.jackson.databind.ObjectMapper,com.fasterxml.jackson.databind.JsonNode)}.
	 */
	@Test
	public void testBuildAndAddBTermToArray() {
		try {
			ArrayNode bTermsArray = mapper.createArrayNode();
			JsonNode idSearchResponse = mapper.readTree(new File(
					"C://Users/SURENDRANATH/eclipse-workspace/praxi-rtpe-web-service/src/test/resources/id-search-response.json"));
			if (idSearchResponse.has("docs")) {
				for (JsonNode aNode : idSearchResponse.get("docs")) {
					if (null != aNode.get("_source")) {
						if (null != aNode.get("_source").get("recommendations")) {
							for (JsonNode aRecommendationNode : aNode.get("_source").get("recommendations")) {
								System.out.println(Util
										.buildAndAddBTermToArray(bTermsArray, mapper, aRecommendationNode).toString());
							}
						}
						if (null != aNode.get("_source").get("curations")) {
							for (JsonNode aCurationNode : aNode.get("_source").get("curations")) {
								if (null != aCurationNode.get("relatedbusinessassetid")) {
									if (aCurationNode.get("relatedbusinessassetid").asInt() > 0) {
										System.out.println(
												Util.buildAndAddBTermToArray(bTermsArray, mapper, aCurationNode)
														.toString());
									}
								}
							}
						}
					}
				}
			}
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
}
