package org.irods.jargon.metadatatemplate;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.query.MetaDataAndDomainData;

public class MetadataMergeResult {
	private List<MetadataTemplate> templates = new ArrayList<MetadataTemplate>();
	private List<MetaDataAndDomainData> unmatchedAvus = new ArrayList<MetaDataAndDomainData>();

	public MetadataMergeResult(final List<MetadataTemplate> inTemplates, final List<MetaDataAndDomainData> inAvus) {
		setTemplates(inTemplates);
		setUnmatchedAvus(inAvus);
	}

	public List<MetadataTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(final List<MetadataTemplate> templateList) {
		templates = new ArrayList<MetadataTemplate>();

		for (MetadataTemplate mt : templateList) {
			templates.add(mt);
		}
	}

	public List<MetaDataAndDomainData> getUnmatchedAvus() {
		return unmatchedAvus;
	}

	public void setUnmatchedAvus(final List<MetaDataAndDomainData> unmatchedAvus) {
		for (MetaDataAndDomainData avu : unmatchedAvus) {
			this.unmatchedAvus.add(avu);
		}
	}

}
