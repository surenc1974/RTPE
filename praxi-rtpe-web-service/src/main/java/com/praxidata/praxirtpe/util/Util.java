package com.praxidata.praxirtpe.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.math3.util.CombinatoricsUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.praxidata.praxirtpe.repo.ESRepository;
import com.praxidata.util.hashGenerator.ComplexHashGenerator;

public class Util {

	public Util() {
	}

	public static String buildAssetSearchQueryForMultipleIDs(ObjectMapper mapper, List<Integer> assetIds,
			String assetIndex) {
		if ((null != assetIds) && (!assetIds.isEmpty())) {
			ObjectNode baseNode = mapper.createObjectNode();
			ArrayNode docsNode = mapper.createArrayNode();
			for (Integer assetId : assetIds) {
				ObjectNode anAssetIdNode = mapper.createObjectNode();
				anAssetIdNode.put("_index", assetIndex);
				anAssetIdNode.put("_id", assetId);
				docsNode.add(anAssetIdNode);
			}
			baseNode.put("docs", docsNode);
			return baseNode.toString();
		}
		return "";
	}

	public static List<Integer[]> getAllCombinationsOfIds(Integer[] allIds) {
		List<Integer[]> allCombos = new ArrayList<Integer[]>();
		Arrays.parallelSort(allIds);
		for (int i = 1; i <= allIds.length; i++) {
			Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(allIds.length, i);
			while (iterator.hasNext()) {
				int[] combination = iterator.next();
				Arrays.parallelSort(combination);
				Integer[] anIDCombo = new Integer[i];
				for (int j = 0; j < combination.length; j++) {
					anIDCombo[j] = allIds[combination[j]];
				}
				allCombos.add(anIDCombo);
			}
		}
		return allCombos;
	}

	public static JsonNode buildComplexAssetSearchQueryBody(Long hash, ObjectMapper mapper) throws IOException {
		JsonNode baseNode = mapper.readTree(Constants.QUERY_TEMPLATE);
		ArrayNode hashArray = mapper.createArrayNode();
		hashArray.add(hash);
		ObjectNode matchNode = ((ObjectNode) baseNode.get("query").get("bool").get("must").get("match"));
		matchNode.put("businessassethash", hash);
		ObjectNode termsNode = ((ObjectNode) baseNode.get("query").get("bool").get("filter").get("terms"));
		termsNode.put("businessassethash", hashArray);
		return baseNode;
	}

	public static ArrayNode buildAndAddBTermToArray(ArrayNode bTermsArray, ObjectMapper mapper, JsonNode aBTermNode) {
		ObjectNode bTerm = mapper.createObjectNode();
		bTerm.put("id", aBTermNode.get("relatedbusinessassetid").asInt());
		bTerm.put("uid", aBTermNode.get("relatedbusinessassetuid").asText());
		bTermsArray.add(bTerm);
		return bTermsArray;
	}

	public static String getAllComplexAssetsAndConstituentTermsForBusinessTermSet(ArrayNode bTermsArray,
			ObjectMapper mapper, ESRepository esRepo) throws IOException {
		ArrayNode complexAssetsAndConstituentTerms = mapper.createArrayNode();
		Map<Integer, UUID> bTermIDsAndUUIDs = new HashMap<Integer, UUID>();
		Integer[] allIds = new Integer[bTermsArray.size()];
		int count = 0;
		for (JsonNode aBtermNode : bTermsArray) {
			bTermIDsAndUUIDs.put(aBtermNode.get("id").asInt(), UUID.fromString(aBtermNode.get("uid").asText()));
			allIds[count] = aBtermNode.get("id").asInt();
			count++;
		}
		for (Integer[] anIdCombo : getAllCombinationsOfIds(allIds)) {
			Long businessassethash = new ComplexHashGenerator().generateHashForSetOfIntegers(anIdCombo);
			// From Repository, get the complex assets link business
			// terms to that complex asset and add it to the complex asset array
			ArrayNode complexAssetsMatched = esRepo.findAllComplexAssets(mapper, businessassethash);
			for (JsonNode aComplexAssetMatched : complexAssetsMatched) {
				ObjectNode aComplexAssetAndConstituents = mapper.createObjectNode();
				aComplexAssetAndConstituents.put("complexAsset", aComplexAssetMatched);
				ArrayNode constituentTerms = mapper.createArrayNode();
				for (Integer aBtermId : anIdCombo) {
					ObjectNode aConstituent = mapper.createObjectNode();
					aConstituent.put("businessTermId", aBtermId);
					aConstituent.put("businessTermUID", bTermIDsAndUUIDs.get(aBtermId).toString());
					constituentTerms.add(aConstituent);
				}
				aComplexAssetAndConstituents.put("constituentTerms", constituentTerms);
				complexAssetsAndConstituentTerms.add(aComplexAssetAndConstituents);
			}
		}
		return complexAssetsAndConstituentTerms.toString();
	}
}
