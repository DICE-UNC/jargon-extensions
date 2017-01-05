/**
 * 
 */
package org.irods.jargon.filetemplate.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.filetemplate.FileTemplate;
import org.irods.jargon.filetemplate.TemplateCreatedFile;
import org.irods.jargon.filetemplate.exception.FileTemplateException;
import org.irods.jargon.filetemplate.exception.FileTemplateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for a creator of files
 * 
 * @author Mike Conway - DICE
 *
 */
public abstract class FileCreator extends AbstractJargonService {

	private final FileTemplate fileTemplate;
	public static final Logger log = LoggerFactory.getLogger(FileCreator.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public FileCreator(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount, final FileTemplate fileTemplate) {
		super(irodsAccessObjectFactory, irodsAccount);
		if (fileTemplate == null) {
			throw new IllegalArgumentException("null fileTemplate");
		}
		this.fileTemplate = fileTemplate;
	}

	/**
	 * subclass implements this to create the new file from the template data
	 * 
	 * @param parentPath
	 *            <code>String</code> with the absolute iRODS path to the parent
	 *            collection that will contain the new file
	 * @param fileName
	 *            <code>String</code> with the file name + extension (file.txt)
	 *            to be created. Whether the implementation checks the extension
	 *            versus the MIME type of the file is implementation dependent,
	 *            but the file contents will be determined by the template
	 * @return {@link TemplateCreatedFile}
	 * @throws DuplicateDataException
	 * @throws FileTemplateException
	 */
	public abstract TemplateCreatedFile create(final String parentPath,
			final String fileName) throws DuplicateDataException,
			FileTemplateException;

	/**
	 * @return the fileTemplate
	 */
	public FileTemplate getFileTemplate() {
		return fileTemplate;
	}

	/**
	 * Handy method for subclasses to get the iRODS file for the path info and
	 * see if it is a duplicate
	 * 
	 * @param parentPath
	 *            <code>String</code> with the absolute iRODS path to the parent
	 *            collection that will contain the new file
	 * @param fileName
	 *            <code>String</code> with the file name + extension (file.txt)
	 *            to be created. Whether the implementation checks the extension
	 *            versus the MIME type of the file is implementation dependent,
	 *            but the file contents will be determined by the template
	 * @return {@link IRODSFile}
	 * @throws DuplicateDataException
	 * @throws FileTemplateException
	 */
	protected IRODSFile irodsFileForPathAndNameThrowDuplicateExceptionIfAlreadyExists(
			final String parentPath, final String fileName)
			throws DuplicateDataException, FileTemplateException {

		log.info("irodsFileForPathAndNameThrowDuplicateExceptionIfAlreadyExists()");
		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		log.info("create the iRODS file and make sure it doesn't already exist");
		IRODSFile irodsFile = null;
		try {
			irodsFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(parentPath, fileName);
			if (irodsFile.exists()) {
				log.error("file:{} already exists", irodsFile);
				throw new DuplicateDataException(
						"irodsFile already exists, cannot create");
			}

			return irodsFile;
		} catch (JargonException e) {
			log.error("jargonException getting irods file for create:{}",
					irodsFile, e);
			throw new FileTemplateException("exception getting the file", e);

		}

	}

	/**
	 * Given a resource name, that should be a resolvable path to the template
	 * resource, return the data as a stream for copying into the iRODS location
	 * 
	 * @param resourceName
	 *            <code>String</code> with an implementation-dependent location
	 *            for the resource
	 * @return {@link InputStream} (we went ahead and buffered it) for the
	 *         resource data
	 * @throws FileTemplateNotFoundException
	 * @throws FileTemplateException
	 */
	protected InputStream obtainInputStreamOfTemplateData(
			final String resourceName) throws FileTemplateNotFoundException,
			FileTemplateException {

		log.info("obtainInputStreamOfTemplateData()");

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		log.info("resourceName:{}", resourceName);

		File resourceFile = null;
		try {
			resourceFile = LocalFileUtils
					.getClasspathResourceAsFile(resourceName);
		} catch (JargonException e) {
			log.error(
					"jargonException getting classpath resource as file for:{}",
					resourceName);
		}

		if (!resourceFile.exists()) {
			log.error("could not find the resource file:{}", resourceFile);
			throw new FileTemplateNotFoundException(
					"resource file does not exist");
		}

		try {
			return new BufferedInputStream(new FileInputStream(resourceFile));
		} catch (FileNotFoundException e) {
			log.error("file not found for resource file:{}", resourceFile);
			throw new FileTemplateNotFoundException("could not find resource",
					e);
		}

	}

	/**
	 * Central method to copy a file template to a target file, this should be
	 * called by the create() method of the implementing subclass
	 * 
	 * @param parentPath
	 * @param fileName
	 * @param resourcePath
	 * @return
	 * @throws DuplicateDataException
	 * @throws FileTemplateException
	 */
	protected TemplateCreatedFile create(String parentPath, String fileName,
			final String resourcePath) throws DuplicateDataException,
			FileTemplateException {

		log.info("create()");
		this.validateCreateParameters(parentPath, fileName);
		log.info("get the file");
		IRODSFile irodsFile = this
				.irodsFileForPathAndNameThrowDuplicateExceptionIfAlreadyExists(
						parentPath, fileName);

		try {

			Stream2StreamAO stream2StreamAO = this
					.getIrodsAccessObjectFactory().getStream2StreamAO(
							this.getIrodsAccount());
			log.info("stream copy");
			stream2StreamAO.streamClasspathResourceToIRODSFile(resourcePath,
					irodsFile.getAbsolutePath());

			log.info("stream copy completed");
			TemplateCreatedFile templateCreatedFile = new TemplateCreatedFile();
			templateCreatedFile.setFileName(fileName);
			templateCreatedFile.setFileTemplate(this.getFileTemplate());
			templateCreatedFile.setParentCollectionAbsolutePath(parentPath);
			return templateCreatedFile;

		} catch (JargonException e) {
			log.error("jargonException getting output stream for:{}",
					irodsFile, e);
			throw new FileTemplateException("exception getting output stream",
					e);
		}

	}

	protected void validateCreateParameters(final String parentPath,
			final String fileName) {

		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		log.info("parentPath:{}", parentPath);
		log.info("fileName:{}", fileName);

	}

}
