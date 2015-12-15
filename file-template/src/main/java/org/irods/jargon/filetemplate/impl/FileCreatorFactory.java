/**
 * 
 */
package org.irods.jargon.filetemplate.impl;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.filetemplate.FileTemplate;
import org.irods.jargon.filetemplate.exception.FileTemplateNotFoundException;
import org.irods.jargon.filetemplate.impl.types.RuleFileCreator;
import org.irods.jargon.filetemplate.impl.types.TextFileCreator;
import org.irods.jargon.filetemplate.impl.types.XmlFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates an instance of the {@link FileCreator} based on various indicators,
 * such as unique id. These returned creators can build a file given a template.
 * 
 * @author Mike Conway - DICE
 *
 */
public class FileCreatorFactory extends AbstractJargonService {

	public static final Logger log = LoggerFactory
			.getLogger(FileCreatorFactory.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public FileCreatorFactory(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * Get the {@link FileCreator} to build a file given a template
	 * 
	 * @param fileTemplate
	 *            {@link FileTemplate} that defines the type of file to be
	 *            created
	 * @return {@link FileCreator}
	 * @throws FileTemplateNotFoundException
	 */
	public FileCreator instanceCreatorForFileTemplate(
			final FileTemplate fileTemplate)
			throws FileTemplateNotFoundException {
		log.info("instanceCreatorByUniqueId()");
		if (fileTemplate == null) {
			throw new IllegalArgumentException("null or empty fileTemplate");
		}
		log.info("fileTemplate:{}", fileTemplate);

		if (fileTemplate.getTemplateUniqueIdentifier().equals(
				TextFileCreator.TEXT_CREATOR_ID)) {
			return new TextFileCreator(this.getIrodsAccessObjectFactory(),
					this.getIrodsAccount(), fileTemplate);
		} else if (fileTemplate.getTemplateUniqueIdentifier().equals(
				XmlFileCreator.XML_CREATOR_ID)) {
			return new XmlFileCreator(this.getIrodsAccessObjectFactory(),
					this.getIrodsAccount(), fileTemplate);
		} else if (fileTemplate.getTemplateUniqueIdentifier().equals(
				RuleFileCreator.RULE_CREATOR_ID)) {
			return new RuleFileCreator(this.getIrodsAccessObjectFactory(),
					this.getIrodsAccount(), fileTemplate);
		} else {
			log.error("cannot file creator for template:{}", fileTemplate);
			throw new FileTemplateNotFoundException(
					"no creator found for template");
		}

	}
}
