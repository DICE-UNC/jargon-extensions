package org.irods.jargon.vircoll.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.CollectionPagerAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.usertagging.domain.IRODSStarredFileOrCollection;
import org.irods.jargon.usertagging.starring.IRODSStarringService;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollection;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollection;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollectionExecutor;
import org.junit.Test;
import org.mockito.Mockito;

public class StarredFoldersVirtualCollectionImplTest {

	@Test
	public void testQueryCollectionsWithPathHint() throws Exception {

		String testPath = "/a/collection/here";
		String subColl = "subcoll";
		String dataName = "data.txt";
		IRODSAccount irodsAccount = TestingPropertiesHelper
				.buildBogusIrodsAccount();
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionBasedVirtualCollection virColl = new CollectionBasedVirtualCollection(
				"blah", testPath);

		CollectionPagerAO collectionPagerAO = Mockito
				.mock(CollectionPagerAO.class);

		PagingAwareCollectionListing listing = new PagingAwareCollectionListing();

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(1);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setParentPath(testPath);
		entry.setPathOrName(testPath + "/" + subColl);
		entry.setTotalRecords(1);
		listing.getCollectionAndDataObjectListingEntries().add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(1);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setParentPath(testPath);
		entry.setPathOrName(dataName);
		entry.setTotalRecords(1);

		listing.getCollectionAndDataObjectListingEntries().add(entry);

		Mockito.when(collectionPagerAO.retrieveFirstPageUnderParent(testPath))
				.thenReturn(listing);

		Mockito.when(
				irodsAccessObjectFactory.getCollectionPagerAO(irodsAccount))
				.thenReturn(collectionPagerAO);

		JargonProperties jargonProperties = new SettableJargonProperties();
		Mockito.when(irodsAccessObjectFactory.getJargonProperties())
				.thenReturn(jargonProperties);

		IRODSStarringService irodsStarringService = Mockito
				.mock(IRODSStarringService.class);

		StarredFoldersVirtualCollection starredVircoll = new StarredFoldersVirtualCollection();

		StarredFoldersVirtualCollectionExecutor starredExecutor = new StarredFoldersVirtualCollectionExecutor(
				starredVircoll, irodsAccessObjectFactory, irodsAccount,
				irodsStarringService);

		PagingAwareCollectionListing actual = starredExecutor.queryAll(
				testPath, 0);
		Assert.assertNotNull(actual);
		Assert.assertFalse(actual.getCollectionAndDataObjectListingEntries()
				.isEmpty());
		CollectionAndDataObjectListingEntry actualEntry = actual
				.getCollectionAndDataObjectListingEntries().get(0);

		Assert.assertEquals(testPath, actualEntry.getParentPath());

		Assert.assertEquals(
				CollectionAndDataObjectListingEntry.ObjectType.COLLECTION,
				actualEntry.getObjectType());

	}

	@Test
	public void testQueryCollections() throws Exception {

		String testPath = "/a/collection/here";
		String descr = "test";
		IRODSAccount irodsAccount = Mockito.mock(IRODSAccount.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		JargonProperties jargonProperties = new SettableJargonProperties();
		Mockito.when(irodsAccessObjectFactory.getJargonProperties())
				.thenReturn(jargonProperties);

		IRODSStarringService irodsStarringService = Mockito
				.mock(IRODSStarringService.class);

		StarredFoldersVirtualCollection virColl = new StarredFoldersVirtualCollection();

		StarredFoldersVirtualCollectionExecutor executor = new StarredFoldersVirtualCollectionExecutor(
				virColl, irodsAccessObjectFactory, irodsAccount,
				irodsStarringService);

		List<IRODSStarredFileOrCollection> results = new ArrayList<IRODSStarredFileOrCollection>();
		IRODSStarredFileOrCollection starred = new IRODSStarredFileOrCollection(
				MetadataDomain.COLLECTION, testPath, descr, "bob");
		results.add(starred);

		Mockito.when(irodsStarringService.listStarredCollections(0))
				.thenReturn(results);

		PagingAwareCollectionListing actual = executor.queryAll(0);
		Assert.assertNotNull(actual);
		Assert.assertFalse(actual.getCollectionAndDataObjectListingEntries()
				.isEmpty());
		CollectionAndDataObjectListingEntry actualEntry = actual
				.getCollectionAndDataObjectListingEntries().get(0);
		CollectionAndPath cp = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(testPath);
		Assert.assertEquals(cp.getCollectionParent(),
				actualEntry.getParentPath());
		Assert.assertEquals(testPath, actualEntry.getPathOrName());
		Assert.assertEquals(
				CollectionAndDataObjectListingEntry.ObjectType.COLLECTION,
				actualEntry.getObjectType());

	}
}
