package org.irods.jargon.metadatatemplate;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.metadatatemplatesif.FormBasedMetadataTemplate;
import org.irods.jargon.metadatatemplatesif.MetadataTemplate;

public class MetadataMergeResult {
	private MetadataTemplate template = new FormBasedMetadataTemplate();
	private List<MetaDataAndDomainData> unmatchedAvus = new ArrayList<MetaDataAndDomainData>();
	
	public MetadataMergeResult(MetadataTemplate inTemplate, List<MetaDataAndDomainData> inAvus) {
		setTemplate(inTemplate);
		setUnmatchedAvus(inAvus);
	}
	
	public MetadataTemplate getTemplate() {
		return template;
	}
	public void setTemplate(MetadataTemplate template) {
		this.template = template.deepCopy();
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
