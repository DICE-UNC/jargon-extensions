package org.irods.jargon.metadatatemplate;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 
 * Filter to identify metadatatemplate ".mdtemplate" files
 * 
 * @author rskarbez
 *
 */

public class MetadataTemplateFileFilter implements FilenameFilter {

	@Override
	public boolean accept(File file, String fileName) {
		if (fileName.endsWith(MetadataTemplateConstants.METADATA_TEMPLATE_EXT)) {
			return true;
		}
		else {	
			return false;
		}
	}

}
