package org.irods.jargon.metadatatemplate;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.metadatatemplate.MetadataTemplate;

public class MetadataMergeResult {
	private List<MetadataTemplate> templates = new ArrayList<MetadataTemplate>();
	private List<MetaDataAndDomainData> unmatchedAvus = new ArrayList<MetaDataAndDomainData>();

	public MetadataMergeResult(List<MetadataTemplate> inTemplates,
			List<MetaDataAndDomainData> inAvus) {
		setTemplates(inTemplates);
		setUnmatchedAvus(inAvus);
	}

	public List<MetadataTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(List<MetadataTemplate> templateList) {
		this.templates = new ArrayList<MetadataTemplate>();

		for (MetadataTemplate mt : templateList) {
			this.templates.add(mt.deepCopy());
		}
	}

	public List<MetaDataAndDomainData> getUnmatchedAvus() {
		return unmatchedAvus;
	}

	public void setUnmatchedAvus(List<MetaDataAndDomainData> unmatchedAvus) {
		for (MetaDataAndDomainData avu : unmatchedAvus) {
			this.unmatchedAvus.add(avu);
		}
	}

}
