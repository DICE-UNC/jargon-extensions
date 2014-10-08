/**
 * 
 */
package org.irods.jargon.vircoll.types;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.vircoll.GeneralParameterConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mike Conway - DICE
 * 
 */
public class SparqlViaRestVirtualCollectionExecutorTest {

	@Test
	public void testQueryAll() throws Exception {
		String query = LocalFileUtils
				.getClasspathResourceFileAsString("/sparql-templates/baseVocabQuery.txt");

		SparqlViaRestVirtualCollection virtualCollection = new SparqlViaRestVirtualCollection();
		virtualCollection.setUniqueName("SPARQL HIVE");
		virtualCollection.setQueryBody(query);

		virtualCollection
				.getParameters()
				.put(GeneralParameterConstants.ACCESS_URL,
						"http://testdfc2.renci.org:8080/hive-query-rest-1.0-SNAPSHOT/sparql/");

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = Mockito.mock(IRODSAccount.class);

		SparqlViaRestVirtualCollectionExecutor executor = new SparqlViaRestVirtualCollectionExecutor(
				virtualCollection, irodsAccessObjectFactory, irodsAccount);
		PagingAwareCollectionListing list = executor.queryAll(0);
		Assert.assertNotNull("null list returned", list);

	}
}
