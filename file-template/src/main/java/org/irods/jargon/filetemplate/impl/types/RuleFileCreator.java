package org.irods.jargon.filetemplate.impl.types;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.filetemplate.FileTemplate;
import org.irods.jargon.filetemplate.TemplateCreatedFile;
import org.irods.jargon.filetemplate.exception.FileTemplateException;
import org.irods.jargon.filetemplate.impl.FileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creator for .r files
 * 
 * @author Mike Conway - DICE
 *
 */
public class RuleFileCreator extends FileCreator {

	public static final Logger log = LoggerFactory
			.getLogger(RuleFileCreator.class);

	public static final String RULE_CREATOR_ID = "file.template.default.irods.rule";

	public RuleFileCreator(IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount, FileTemplate fileTemplate) {
		super(irodsAccessObjectFactory, irodsAccount, fileTemplate);
	}

	@Override
	public TemplateCreatedFile create(String parentPath, String fileName)
			throws DuplicateDataException, FileTemplateException {
		return this.create(parentPath, fileName, "/templates/rulefile.r");
	}

}
