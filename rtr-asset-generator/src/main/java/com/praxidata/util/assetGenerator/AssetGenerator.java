package com.praxidata.util.assetGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class AssetGenerator {
	public static void main(String[] args) {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		PrintWriter printWriter = null;
		try {
			Properties props = new Properties();
			FileReader reader;
			reader = new FileReader(args[0]);
			props.load(reader);
			long numberOfAssetsToGenerate = Long.parseLong(props.getProperty("total-number-of-assets")) + 1;
			File outputFile = new File(props.getProperty("output-file-name"));
			String host = props.getProperty("es-host");
			int port = Integer.parseInt(props.getProperty("es-port"));
			int port2 = Integer.parseInt(props.getProperty("es-port-2"));
			String protocol = props.getProperty("es-protocol");
			RestClient client = RestClient
					.builder(new HttpHost(host, port, protocol), new HttpHost(host, port2, protocol)).build();
			String assetIndex = props.getProperty("asset-index");
			Long[] businessAssetsToBeLinked = new Long[] { (long) 0, (long) 0 };
			ObjectMapper mapper = new ObjectMapper();
			for (long i = Long.parseLong(props.getProperty("start-point")); i <= numberOfAssetsToGenerate; i++) {
				/*
				 * StringBuffer strBuffer = new StringBuffer(""); if (outputFile.exists()) {
				 * fileWriter = new FileWriter(outputFile, true); } else { fileWriter = new
				 * FileWriter(props.getProperty("output-file-name")); }
				 */
				JsonNode assetNode = generateBody(i, mapper, businessAssetsToBeLinked);
				Request request = new Request("POST", "/" + assetIndex + "/_create/" + new Long(i).toString());
				request.setJsonEntity(assetNode.toPrettyString());
				Response aResponse = client.performRequest(request);
				String responseBody = EntityUtils.toString(aResponse.getEntity());
				// System.out.println("Index update-->" + responseBody);
				/*
				 * strBuffer.append(assetNode); strBuffer.append("\n"); bufferedWriter = new
				 * BufferedWriter(fileWriter); printWriter = new PrintWriter(bufferedWriter);
				 * printWriter.write(strBuffer.toString()); printWriter.flush();
				 */
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != fileWriter) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != bufferedWriter) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != printWriter) {
				printWriter.close();
			}
		}
	}

	private static JsonNode generateBody(long assetID, ObjectMapper mapper, Long[] businessAssetsToBeLinked) {
		ObjectNode assetBody = mapper.createObjectNode();
		assetBody.put("id", new Long(assetID).toString());
		assetBody.put("assetname", "asset no" + new Long(assetID).toString());
		if (assetID % 5 == 0) {
			assetBody.put("relatedbusinessasset", new Long(assetID + 1).toString());
			assetBody.put("matchScore", 0.9);
			if (businessAssetsToBeLinked[0] == 0) {
				businessAssetsToBeLinked[0] = assetID + 1;
				businessAssetsToBeLinked[1] = assetID + 6;
			}
		}
		if (assetID % 12 == 0) {
			HashFunction hf = Hashing.murmur3_128();
			Hasher hasher = hf.newHasher();
			for (Long businessAssetId : businessAssetsToBeLinked) {
				hasher = hasher.putLong(businessAssetId);
			}
			HashCode hc = hasher.hash();
			assetBody.put("businessassethash", hc.asLong());
			businessAssetsToBeLinked = new Long[] { (long) 0, (long) 0 };
		}
		return assetBody;
	}
}
