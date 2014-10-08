/**
 * 
 */
package org.irods.jargon.vircoll.types;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.query.PagingAwareCollectionListing.PagingStyle;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.MiscIRODSUtils;
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

		String accessUrl = this.getVirtualCollection().getParameters()
				.get(GeneralParameterConstants.ACCESS_URL);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		log.info("accessURL:{}", accessUrl);

		// streaming JSON see
		// http://www.studytrails.com/java/json/java-jackson-json-streaming.jsp

		// continue parsing the token till the end of input is reached
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost(accessUrl);

			String requestBody = this.getVirtualCollection().getQueryBody();
			log.info("requestBody:{}", requestBody);
			httpost.setEntity(new StringEntity(requestBody));
			// execute method and handle any error responses.

			HttpResponse response = httpclient.execute(httpost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(rd);

			boolean atBindings = false;
			CollectionAndDataObjectListingEntry entry = null;
			CollectionAndPath collectionAndPath;
			int count = 1;

			DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy",
					Locale.ENGLISH);

			while (!parser.isClosed()) {
				// get the token
				JsonToken token;

				token = parser.nextToken();

				// if its the last token then we are done
				if (token == null)
					break;

				log.info("currentName:{}", parser.getCurrentName());
				log.info("token:{}", token);

				if (atBindings) {
					// continue
				} else if (parser.getText().equals("bindings")) {
					atBindings = true;
				} else {
					continue;
				}

				// log.info("found bindings");

				if (parser.getCurrentName() == null) {
					continue;
				}

				if (parser.getCurrentName().equals("absPath")
						&& token.toString().equals("START_OBJECT")) {
					// see if previous rec
					if (entry != null) {
						entries.add(entry);
						entry = null;
					}

					while (!parser.getCurrentName().equals("value")) {
						token = parser.nextToken();
						log.info("token:{}", token);
						log.info("currentName:{}", parser.getCurrentName());
					}

					token = parser.nextToken();
					// parser.getText();
					// token = parser.nextToken();

					entry = new CollectionAndDataObjectListingEntry();
					entry.setCount(count++);
					collectionAndPath = MiscIRODSUtils
							.separateCollectionAndPathFromGivenAbsolutePath(parser
									.getText());
					entry.setParentPath(collectionAndPath.getCollectionParent());
					entry.setPathOrName(collectionAndPath.getChildName());

					// FIXME: hack for determining file, need to look at
					// ontology
					if (collectionAndPath.getChildName().indexOf('.') > -1) {
						entry.setObjectType(ObjectType.DATA_OBJECT);
					} else {
						entry.setObjectType(ObjectType.COLLECTION);
					}

					continue;
				}

				if (parser.getCurrentName().equals("size")
						&& token.toString().equals("START_OBJECT")) {
					while (!parser.getCurrentName().equals("value")) {
						token = parser.nextToken();
						log.info("token:{}", token);
						log.info("currentName:{}", parser.getCurrentName());
					}

					token = parser.nextToken();
					log.info("token:{}", token);
					log.info("currentName:{}", parser.getCurrentName());
					log.info("currentValue:{}", parser.getText());
					Long sizeAsLong = Long.parseLong(parser.getText());

					entry.setDataSize(sizeAsLong);
					continue;
				}

				if (parser.getCurrentName().equals("created")
						&& token.toString().equals("START_OBJECT")) {
					while (!parser.getCurrentName().equals("value")) {
						token = parser.nextToken();
						log.info("token:{}", token);
						log.info("currentName:{}", parser.getCurrentName());
					}

					token = parser.nextToken();
					try {
						Date result = df.parse(parser.getText());
						entry.setCreatedAt(result);
					} catch (Exception e) {
						// parse exception...ignore
					}

				}

			}

			log.info("last entry?");
			if (entry != null) {
				entries.add(entry);
			}

		} catch (JsonParseException e) {
			throw new VirtualCollectionException(
					"json exception parsing SPARQL result", e);
		} catch (IOException e) {
			throw new VirtualCollectionException(
					"io exception parsing SPARQL result", e);
		}

		PagingAwareCollectionListing listing = new PagingAwareCollectionListing();
		listing.setCollectionAndDataObjectListingEntries(entries);
		listing.setCollectionsComplete(true);
		listing.setDataObjectsComplete(true);
		listing.setPagingStyle(PagingStyle.NONE);
		log.info("listing:{}", listing);
		return listing;

	}

	private void validate() throws VirtualCollectionValidationException {
		// TODO: add validation steps
	}

	/**
	 * 
	 */
	SparqlViaRestVirtualCollectionExecutor() {
		super();
	}

	/**
	 * @param virtualCollection
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public SparqlViaRestVirtualCollectionExecutor(
			SparqlViaRestVirtualCollection virtualCollection,
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(virtualCollection, irodsAccessObjectFactory, irodsAccount);
	}

}
