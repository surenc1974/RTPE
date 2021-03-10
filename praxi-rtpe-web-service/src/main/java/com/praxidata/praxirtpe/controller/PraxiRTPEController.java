package com.praxidata.praxirtpe.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.praxidata.praxirtpe.repo.ESRepository;
import com.praxidata.praxirtpe.util.Util;
import com.praxidata.util.hashGenerator.ComplexHashGenerator;

@RestController
public class PraxiRTPEController {
	private static final Logger logger = Logger.getLogger(PraxiRTPEController.class.getName());
	@Autowired
	Environment env;

	/**
	 * Handles a POST request. Extracts the complex term for a set of business terms
	 * 
	 * @param request     The HttpServletRequest
	 * @param requestBody String in the body of HttpServletRequest
	 * @return complexAssetsAndConstituentTerms A Stringified JSON that is an array
	 *         of complex assets and the combination of business terms that makes up
	 *         the complex asset
	 */
	@CrossOrigin
	@RequestMapping(value = "/get-complex-term-for-bterms", headers = "Accept=*/*", method = RequestMethod.POST)
	@ResponseBody
	public String getComplexTermsRepresentingBusinessTermSet(@RequestBody String requestBody,
			HttpServletRequest request)
			throws URISyntaxException, IOException, KeyManagementException, NoSuchAlgorithmException {
		ESRepository esRepo = new ESRepository(env.getProperty("elasticsearch-host"),
				Integer.parseInt(env.getProperty("elasticsearch-port")),
				Integer.parseInt(env.getProperty("elasticsearch-second-port")),
				env.getProperty("elasticsearch-protocol"), env.getProperty("elasticsearch-index"));
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode bTermsArray = (ArrayNode) mapper.readTree(requestBody);
		bTermsArray = esRepo.findAllSynonymsOfBusinessTerms(mapper, bTermsArray);
		return Util.getAllComplexAssetsAndConstituentTermsForBusinessTermSet(bTermsArray, mapper, esRepo);
	}

	/**
	 * Handles a POST request. Extracts the complex term for a set of physical data
	 * assets
	 * 
	 * @param request     The HttpServletRequest
	 * @param requestBody String in the body of HttpServletRequest
	 * @return complexAssetsAndConstituentTerms A Stringified JSON that is an array
	 *         of complex assets and the combination of business terms that makes up
	 *         the complex asset
	 */
	@CrossOrigin
	@RequestMapping(value = "/get-complex-term-for-data-assets", headers = "Accept=*/*", method = RequestMethod.POST)
	@ResponseBody
	public String getComplexTermsRepresentingPhysicalAssetSet(@RequestBody String requestBody,
			HttpServletRequest request)
			throws URISyntaxException, IOException, KeyManagementException, NoSuchAlgorithmException {
		ESRepository esRepo = new ESRepository(env.getProperty("elasticsearch-host"),
				Integer.parseInt(env.getProperty("elasticsearch-port")),
				Integer.parseInt(env.getProperty("elasticsearch-second-port")),
				env.getProperty("elasticsearch-protocol"), env.getProperty("elasticsearch-index"));
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode dataAssetsArray = (ArrayNode) mapper.readTree(requestBody);
		ArrayNode bTermsArray = esRepo.findAllRelatedBusinessTerms(mapper, dataAssetsArray);
		return Util.getAllComplexAssetsAndConstituentTermsForBusinessTermSet(bTermsArray, mapper, esRepo);
	}
}
