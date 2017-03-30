/**
 * 
 */
package org.irods.jargon.metadatatemplate;

import javax.annotation.PostConstruct;

import org.irods.jargon.rest.configuration.RestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Metadata template specific configuration, augments {@link RestConfiguration}
 * with settings for mdtemplates
 * 
 * @author mconway
 *
 */
@Component
@PropertySource("file:///etc/irods-ext/mdtemplate.properties")
public class MetadataTemplateConfiguration {

	private String publicTemplateRepositoryLocation = "";

	@Autowired
	Environment env;

	@PostConstruct
	public void init() {
		setPublicTemplateRepositoryLocation(env.getProperty("mdtemplate.public"));

	}

	public String getPublicTemplateRepositoryLocation() {
		return publicTemplateRepositoryLocation;
	}

	public void setPublicTemplateRepositoryLocation(String publicTemplateRepositoryLocation) {
		this.publicTemplateRepositoryLocation = publicTemplateRepositoryLocation;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetadataTemplateConfiguration [");
		if (publicTemplateRepositoryLocation != null) {
			builder.append("publicTemplateRepositoryLocation=").append(publicTemplateRepositoryLocation).append(", ");
		}
		if (env != null) {
			builder.append("env=").append(env);
		}
		builder.append("]");
		return builder.toString();
	}

}
