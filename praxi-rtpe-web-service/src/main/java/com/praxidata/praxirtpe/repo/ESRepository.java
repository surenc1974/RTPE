package com.praxidata.praxirtpe.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.praxidata.praxirtpe.util.Util;

public class ESRepository {
	RestClient client;
	String assetIndex;

	public ESRepository() {
		client = RestClient.builder(new HttpHost("localhost", 9200, "http"), new HttpHost("localhost", 9201, "http"))
				.build();
		assetIndex = "praxiasset";
	}

	public ESRepository(String host, int port, int port2, String protocol, String assetIndexToSearch) {
		client = RestClient.builder(new HttpHost(host, port, protocol), new HttpHost(host, port2, protocol)).build();
		assetIndex = assetIndexToSearch;
	}

	public ArrayNode findAllRelatedBusinessTerms(ObjectMapper mapper, ArrayNode dataAssetsArray) throws IOException {
		ArrayNode bTermsArray = mapper.createArrayNode();
		if ((null != dataAssetsArray)) {
			List<Integer> assetIds = new ArrayList<Integer>();
			for (JsonNode aDataAssetJSON : dataAssetsArray) {
				assetIds.add(aDataAssetJSON.get("id").asInt());
			}
			String responseBody = findAssetsByAssetIds(mapper, assetIds);
			if (null != responseBody) {
				JsonNode responseTree = mapper.readTree(responseBody);
				List<Integer> bTermAssetIds = new ArrayList<Integer>();
				if (responseTree.has("docs")) {
					for (JsonNode aNode : responseTree.get("docs")) {
						if (null != aNode.get("_source")) {
							if (null != aNode.get("_source").get("recommendations")) {
								for (JsonNode aRecommendationNode : aNode.get("_source").get("recommendations")) {
									if (null != aRecommendationNode.get("relatedbusinessassetid")) {
										if (aRecommendationNode.get("relatedbusinessassetid").asInt() > 0) {
											bTermsArray = Util.buildAndAddBTermToArray(bTermsArray, mapper,
													aRecommendationNode);
											bTermAssetIds
													.add(aRecommendationNode.get("relatedbusinessassetid").asInt());
										}
									}
								}
							}
							if (null != aNode.get("_source").get("curations")) {
								for (JsonNode aCurationNode : aNode.get("_source").get("curations")) {
									if (null != aCurationNode.get("relatedbusinessassetid")) {
										if (aCurationNode.get("relatedbusinessassetid").asInt() > 0) {
											bTermsArray = Util.buildAndAddBTermToArray(bTermsArray, mapper,
													aCurationNode);
											bTermAssetIds.add(aCurationNode.get("relatedbusinessassetid").asInt());
										}
									}
								}
							}
						}
					}
				}
				String bTermAssetResponse = findAssetsByAssetIds(mapper, bTermAssetIds);
				if (null != bTermAssetResponse) {
					JsonNode bTermAssetResponseTree = mapper.readTree(bTermAssetResponse);
					if (bTermAssetResponseTree.has("docs")) {
						for (JsonNode aNode : bTermAssetResponseTree.get("docs")) {
							if (null != aNode.get("_source")) {
								for (JsonNode aSynonymNode : aNode.get("_source").get("synonyms")) {
									if (null != aSynonymNode.get("synonym")) {
										String synonymRaw = aSynonymNode.get("synonym").asText();
										if ((null != synonymRaw) && (!synonymRaw.equalsIgnoreCase(""))) {
											ObjectNode bTerm = mapper.createObjectNode();
											String[] synonymInfo = aSynonymNode.get("synonym").asText().split(",");
											if (synonymInfo.length > 1) {
												bTerm.put("id", Integer.parseInt(synonymInfo[0]));
												bTermsArray.add(bTerm);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return bTermsArray;
	}

	public ArrayNode findAllSynonymsOfBusinessTerms(ObjectMapper mapper, ArrayNode bTermsArray) throws IOException {
		if (null != bTermsArray) {
			List<Integer> bTermAssetIds = new ArrayList<Integer>();
			for (JsonNode aBTermJSON : bTermsArray) {
				bTermAssetIds.add(aBTermJSON.get("id").asInt());
			}
			String bTermAssetResponse = findAssetsByAssetIds(mapper, bTermAssetIds);
			if (null != bTermAssetResponse) {
				JsonNode bTermAssetResponseTree = mapper.readTree(bTermAssetResponse);
				if (bTermAssetResponseTree.has("docs")) {
					for (JsonNode aNode : bTermAssetResponseTree.get("docs")) {
						if (null != aNode.get("_source")) {
							for (JsonNode aSynonymNode : aNode.get("_source").get("synonyms")) {
								if (null != aSynonymNode.get("synonym")) {
									String synonymRaw = aSynonymNode.get("synonym").asText();
									if ((null != synonymRaw) && (!synonymRaw.equalsIgnoreCase(""))) {
										ObjectNode bTerm = mapper.createObjectNode();
										String[] synonymInfo = aSynonymNode.get("synonym").asText().split(",");
										if (synonymInfo.length > 1) {
											bTerm.put("id", Integer.parseInt(synonymInfo[0]));
											bTermsArray.add(bTerm);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return bTermsArray;
	}

	public String findAssetsByAssetIds(ObjectMapper mapper, List<Integer> assetIds) throws IOException {
		Request request = new Request("POST", "/_mget");
		request.setJsonEntity(Util.buildAssetSearchQueryForMultipleIDs(mapper, assetIds, assetIndex).toString());
		Response mgetResponse = client.performRequest(request);
		return EntityUtils.toString(mgetResponse.getEntity());
	}

	public ArrayNode findAllComplexAssets(ObjectMapper mapper, Long businessassethash) throws IOException {
		ArrayNode complexAssets = mapper.createArrayNode();
		Request request = new Request("POST", assetIndex + "/_search");
		request.setJsonEntity(Util.buildComplexAssetSearchQueryBody(businessassethash, mapper).toString());
		Response complexAssetSearchResponse = client.performRequest(request);
		String responseBody = EntityUtils.toString(complexAssetSearchResponse.getEntity());
		if (null != responseBody) {
			JsonNode responseTree = mapper.readTree(responseBody);
			for (JsonNode aNode : responseTree) {
				if (null != aNode.get("hits")) {
					for (JsonNode aHit : aNode.get("hits")) {
						if (null != aHit.get("_source")) {
							Long hashFound = aHit.get("_source").get("businessassethash").asLong();
							if (hashFound.equals(businessassethash)) {
								ObjectNode aHitFound = mapper.createObjectNode();
								aHitFound.put("id", aHit.get("_source").get("id").asInt());
								aHitFound.put("complexAssetName", aHit.get("_source").get("assetname").asText());
								complexAssets.add(aHitFound);
							}
						}
					}
				}
			}
		}
		return complexAssets;
	}

	public void close() throws IOException {
		client.close();
	}
}
