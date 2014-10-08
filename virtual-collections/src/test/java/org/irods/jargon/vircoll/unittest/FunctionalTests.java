package org.irods.jargon.vircoll.unittest;

import org.irods.jargon.vircoll.types.SparqlViaRestVirtualCollectionExecutorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests that require some manual setup or are functional in nature, As we
 * refactor code better they can move to unit tests
 * 
 * @author Mike Conway - DICE
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ SparqlViaRestVirtualCollectionExecutorTest.class })
public class FunctionalTests {

}
