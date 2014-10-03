/**
 * 
 */
package org.irods.jargon.vircoll.types;

import static org.junit.Assert.*;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.vircoll.GeneralParameterConstants;
import org.irods.jargon.vircoll.VirtualCollection;
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

		VirtualCollection virtualCollection = new SparqlViaRestVirtualCollection();
		virtualCollection.setUniqueName("/query/to/hive");
		virtualCollection.setQueryBody(query);
		virtualCollection
				.getParameters()
				.put(GeneralParameterConstants.ACCESS_URL,
						"http://testdfc2.renci.org:8080/hive-query-rest-1.0-SNAPSHOT/preparedQuery/");

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = Mockito.mock(IRODSAccount.class);

		SparqlViaRestVirtualCollectionExecutor executor = new SparqlViaRestVirtualCollectionExecutor(
				irodsAccessObjectFactory, irodsAccount);

	}

}
