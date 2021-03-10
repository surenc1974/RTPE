/**
 * 
 */
package com.praxidata.praxirtpe.repo;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author SURENDRANATH
 *
 */
public class TestESRepository {
	ESRepository esRepo;
	ObjectMapper mapper;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		esRepo = new ESRepository("http://24.23.210.141/", 9200, 9201, "http", "praxiasset");
		mapper = new ObjectMapper();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		esRepo = null;
		mapper = null;
	}

	/**
	 * Test method for
	 * {@link com.praxidata.praxirtpe.repo.ESRepository#findAllRelatedBusinessTerms(com.fasterxml.jackson.databind.ObjectMapper, com.fasterxml.jackson.databind.node.ArrayNode)}.
	 */
	@Test
	public void testFindAllRelatedBusinessTerms() {
		try {
			ArrayNode dataAssetsArray = (ArrayNode) mapper.readTree(
					"[{\"id\":7236812,\"uid\":\"aa189a9a-c8d4-3c9d-a38d-b88b7f8b0c1a\"},{\"id\":7236820,\"uid\":\"bd5a6b75-e11d-380e-b0ce-8748becf846c\"},{\"id\":7236819,\"uid\":\"d9bc109c-e0f3-3a4b-80d8-436be40c69fd\"}]");
			System.out.println(esRepo.findAllRelatedBusinessTerms(mapper, dataAssetsArray).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for
	 * {@link com.praxidata.praxirtpe.repo.ESRepository#findAllComplexAssets(com.fasterxml.jackson.databind.ObjectMapper, java.lang.Long)}.
	 */
	@Test
	public void testFindAllComplexAssets() {
		try {
			System.out.println(esRepo.findAllComplexAssets(mapper, (long) 123456).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for
	 * {@link com.praxidata.praxirtpe.repo.ESRepository#findAllSynonymsOfBusinessTerms(com.fasterxml.jackson.databind.ObjectMapper, com.fasterxml.jackson.databind.node.ArrayNode)}.
	 */
	@Test
	public void testFindAllSynonymsOfBusinessTerms() {
		try {
			ArrayNode bTermsArray = (ArrayNode) mapper.readTree(
					"[{\"id\":7226709,\"uid\":\"aa189a9a-c8d4-3c9d-a38d-b88b7f8b0c1a\"},{\"id\":98037,\"uid\":\"bd5a6b75-e11d-380e-b0ce-8748becf846c\"},{\"id\":7236819,\"uid\":\"d9bc109c-e0f3-3a4b-80d8-436be40c69fd\"}]");
			System.out.println(esRepo.findAllSynonymsOfBusinessTerms(mapper, bTermsArray));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
