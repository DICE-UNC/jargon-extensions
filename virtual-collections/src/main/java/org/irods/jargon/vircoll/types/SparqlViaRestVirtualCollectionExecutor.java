/**
 * 
 */
package org.irods.jargon.vircoll.types;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.vircoll.AbstractVirtualCollectionExecutor;
import org.irods.jargon.vircoll.GeneralParameterConstants;
import org.irods.jargon.vircoll.VirtualCollectionValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		InputStream in = new BufferedInputStream(post.getResponseBodyAsStream());

		IOUtils.toString(in)
		return null;

	}

	private void validate() throws VirtualCollectionValidationException {
		// TODO: add validation steps
	}

}
