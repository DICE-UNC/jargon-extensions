package org.irods.jargon.metadatatemplate.unittest;

import org.irods.jargon.metadatatemplate.JargonMetadataExporterTest;
import org.irods.jargon.metadatatemplate.JargonMetadataResolverTest;
import org.irods.jargon.metadatatemplate.MetadataTemplateFormBotServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ JargonMetadataResolverTest.class,
		JargonMetadataExporterTest.class,
		MetadataTemplateFormBotServiceTest.class })
public class AllTests {

}
