package org.irods.jargon.filetemplate.impl;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.filetemplate.FileTemplate;
import org.irods.jargon.filetemplate.FileTemplateService;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultFileTemplateServiceImplTest {

	@Test
	public void testListAvailableFileTemplates() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = TestingPropertiesHelper
				.buildBogusIrodsAccount();
		FileTemplateService templateService = new DefaultFileTemplateServiceImpl();
		templateService.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		templateService.setIrodsAccount(irodsAccount);
		List<FileTemplate> templates = templateService
				.listAvailableFileTemplates();
		Assert.assertNotNull("null templates", templates);
		Assert.assertFalse("empty templates", templates.isEmpty());

	}

}
