/**
 * 
 */
package org.irods.jargon.vircoll.types;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.vircoll.AbstractVirtualCollectionExecutor;
import org.irods.jargon.vircoll.GeneralParameterConstants;
import org.irods.jargon.vircoll.VirtualCollectionException;
import org.irods.jargon.vircoll.VirtualCollectionValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Executes a SPARQL based query via a REST service
 * 
 * @author Mike Conway - DICE
 * 
 */
public class SparqlViaRestVirtualCollectionExecutor extends
		AbstractVirtualCollectionExecutor<SparqlViaRestVirtualCollection> {

	static Logger log = LoggerFactory
			.getLogger(SparqlViaRestVirtualCollectionExecutor.class);

	@SuppressWarnings("deprecation")
	@Override
	public PagingAwareCollectionListing queryAll(int offset)
			throws JargonException {

		log.info("queryAll()");
		log.info("validating...");
		validate();

		PostMethod post = new PostMethod(this.getVirtualCollection()
				.getParameters().get(GeneralParameterConstants.ACCESS_URL));

		post.setRequestBody(this.getVirtualCollection().getQueryBody());
		// execute method and handle any error responses.

		InputStream in;
		try {
			in = new BufferedInputStream(post.getResponseBodyAsStream());
		} catch (IOException e) {
			log.error("io exception getting response body", e);
			throw new VirtualCollectionException(
					"io exception parsing SPARQL result", e);
		}

		JsonFactory factory = new JsonFactory();

		// streaming JSON see
		// http://www.studytrails.com/java/json/java-jackson-json-streaming.jsp

		// continue parsing the token till the end of input is reached
		try {
			JsonParser parser = factory.createParser(in);

			while (!parser.isClosed()) {
				// get the token
				JsonToken token;

				token = parser.nextToken();

				// if its the last token then we are done
				if (token == null)
					break;

				log.info("token:{}", token);

				// we want to look for a field that says dataset
				/*
				 * if (JsonToken.FIELD_NAME.equals(token) &&
				 * "dataset".equals(parser.getCurrentName())) { // we are
				 * entering the datasets now. The first token should // be //
				 * start of array token = parser.nextToken(); if
				 * (!JsonToken.START_ARRAY.equals(token)) { // bail out break; }
				 * // each element of the array is an album so the next token //
				 * should be { token = parser.nextToken(); if
				 * (!JsonToken.START_OBJECT.equals(token)) { break; } // we are
				 * now looking for a field that says "album_title". // We //
				 * continue looking till we find all such fields. This is //
				 * probably not a best way to parse this json, but this will //
				 * suffice for this example. while (true) { token =
				 * parser.nextToken(); if (token == null) break; if
				 * (JsonToken.FIELD_NAME.equals(token) && "album_title"
				 * .equals(parser.getCurrentName())) { token =
				 * parser.nextToken(); System.out.println(parser.getText()); }
				 * 
				 * }
				 */

			}

		} catch (JsonParseException e) {
			throw new VirtualCollectionException(
					"json exception parsing SPARQL result", e);
		} catch (IOException e) {
			throw new VirtualCollectionException(
					"io exception parsing SPARQL result", e);
		}

		IOUtils.toString(in)
		return null;

	}

	private void validate() throws VirtualCollectionValidationException {
		// TODO: add validation steps
	}

}
