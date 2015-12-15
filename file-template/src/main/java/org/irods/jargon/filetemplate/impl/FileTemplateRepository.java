/**
 * 
 */
package org.irods.jargon.filetemplate.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.irods.jargon.filetemplate.FileTemplate;
import org.irods.jargon.filetemplate.impl.types.RuleFileCreator;
import org.irods.jargon.filetemplate.impl.types.TextFileCreator;
import org.irods.jargon.filetemplate.impl.types.XmlFileCreator;

/**
 * Default repository of file templates, creates a fixed list of templates for
 * listing and retrieval
 * 
 * @author Mike Conway - DICE
 *
 */
public class FileTemplateRepository {

	private final Map<String, FileTemplate> fileTemplates;

	public FileTemplateRepository() {
		this.fileTemplates = Collections.unmodifiableMap(init());
	}

	private Map<String, FileTemplate> init() {
		Map<String, FileTemplate> tempMap = new HashMap<String, FileTemplate>();

		FileTemplate fileTemplate = null;

		/*
		 * text file
		 */

		fileTemplate = new FileTemplate();
		fileTemplate.setI18nTemplateName("file.template.name.text");
		fileTemplate.setInfoType("");
		fileTemplate.setMimeType("text/plain");
		fileTemplate.setTemplateName("Text File");
		fileTemplate.setDefaultExtension(".txt");
		fileTemplate
				.setTemplateUniqueIdentifier(TextFileCreator.TEXT_CREATOR_ID);
		tempMap.put(fileTemplate.getTemplateUniqueIdentifier(), fileTemplate);

		/*
		 * rule file
		 */

		fileTemplate = new FileTemplate();
		fileTemplate.setI18nTemplateName("file.template.name.rule");
		fileTemplate.setInfoType("irods/rule");
		fileTemplate.setMimeType("text/plain");
		fileTemplate.setTemplateName("iRODS Rule");
		fileTemplate.setDefaultExtension(".r");
		fileTemplate
				.setTemplateUniqueIdentifier(RuleFileCreator.RULE_CREATOR_ID);
		tempMap.put(fileTemplate.getTemplateUniqueIdentifier(), fileTemplate);

		/*
		 * .xml
		 */
		fileTemplate = new FileTemplate();
		fileTemplate.setI18nTemplateName("jargon.file.template.xml");
		fileTemplate.setInfoType("");
		fileTemplate.setMimeType("application/xml");
		fileTemplate.setTemplateName("XML File");
		fileTemplate.setTemplateUniqueIdentifier(XmlFileCreator.XML_CREATOR_ID);
		tempMap.put(fileTemplate.getTemplateUniqueIdentifier(), fileTemplate);

		return tempMap;

	}

	/**
	 * Get all the file templates
	 * 
	 * @return the fileTemplates
	 */
	public Map<String, FileTemplate> getFileTemplates() {
		return fileTemplates;
	}

}
