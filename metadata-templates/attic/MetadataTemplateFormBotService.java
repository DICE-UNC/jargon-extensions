package org.irods.jargon.metadatatemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.formbot.FormBotExecutionEnum;
import org.irods.jargon.formbot.FormBotExecutionResult;
import org.irods.jargon.formbot.FormBotField;
import org.irods.jargon.formbot.FormBotForm;
import org.irods.jargon.formbot.FormBotService;
import org.irods.jargon.formbot.FormBotValidationEnum;
import org.irods.jargon.formbot.FormBotValidationResult;
import org.irods.jargon.formbot.FormElementEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MetadataTemplateFormBotService extends AbstractJargonService
		implements FormBotService {
	static private Logger log = LoggerFactory
			.getLogger(IRODSFileFactoryImpl.class);
	static private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public FormBotForm buildFormBotForm(String json) {
		JsonNode node = null;

		try {
			node = objectMapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			log.error("IOException: Failed to parse input JSON to JsonNode");
		}

		if (!(node.has("uuid") || node.has("fqName") || (node.has("name") && node
				.has("activeDir")))) {
			log.error("Insufficient information to find metadata template: json must contain a field uuid, fqName, or both name and activeDir");
			return null;
		}

		JargonMetadataResolver resolver = null;
		MetadataTemplate template = null;
		String uuidString = null;

		FormBotForm form = new FormBotForm();

		try {
			resolver = new JargonMetadataResolver(irodsAccount,
					irodsAccessObjectFactory);
		} catch (JargonException e) {
			log.error(
					"JargonException: JargonMetadataResolver could not be created",
					e);
		}

		if (resolver == null) {
			log.error("Unable to instantiate JargonMetadataResolver");
			return null;
		}

		try {
			if (node.has("uuid")) {
				template = resolver.findTemplateByUUID(node.get("uuid")
						.asText());
			} else if (node.has("fqName")) {
				template = resolver.findTemplateByFqName(node.get("fqName")
						.asText());
			} else if (node.has("name") && node.has("activeDir")) {
				template = resolver.findTemplateByName(node.get("name")
						.asText(), node.get("activeDir").asText());
			} else {
				// This should already have been caught above, but
				// replicated here for completeness
				log.error("Insufficient information to find metadata template: json must contain a field uuid, fqName, or both name and activeDir");
				return null;
			}
		} catch (FileNotFoundException e) {
			log.error("Metadata template file not found");
			return null;
		} catch (MetadataTemplateParsingException e) {
			log.error("Error parsing metadata template file JSON");
			return null;
		} catch (MetadataTemplateProcessingException e) {
			log.error("Error when processing metadata template file");
			return null;
		} catch (IOException e) {
			log.error("IOException when trying to load metadata template file");
			return null;
		}

		uuidString = template.getUuid().toString();

		form.setName(template.getName());
		form.setDescription(template.getDescription());
		form.setUniqueId(uuidString);

		// XXX Hard cast to FormBasedMetadataTemplate
		for (MetadataElement me : ((FormBasedMetadataTemplate) template)
				.getElements()) {
			FormBotField field = new FormBotField();
			field.setName(me.getName());
			field.setDescription(me.getDescription());
			field.setType(me.getType());
			field.setCurrentValue(me.getCurrentValue());

			// uniqueId is template UUID + field name
			// i.e. 01234567-0123-0123-0123-0123456789abFieldName
			// UUID is in first 36 characters
			String uniqueId = uuidString + me.getName();
			field.setUniqueId(uniqueId);

			for (String s : me.getValidationOptions()) {
				field.getParamList().add(s);
			}

			// FormElementEnum
			// ANY, CHECK_BOX, DROP_DOWN, RADIO_BUTTONS, TEXT, TEXT_AREA,
			// DATE, RANGE, URL

			// ValidationStyleEnum
			// DEFAULT, IS, IN_LIST, IN_RANGE, IN_RANGE_EXCLUSIVE, REGEX,
			// DO_NOT_VALIDATE

			// TODO should really check me.getRenderingOptions for this info
			switch (me.getType()) {
			case STRING:
				if (me.getValidationStyle() == ValidationStyleEnum.IN_LIST) {
					field.setFormElement(FormElementEnum.DROP_DOWN);
				} else {
					field.setFormElement(FormElementEnum.TEXT);
				}

				break;
			case INT:
				if (me.getValidationStyle() == ValidationStyleEnum.IN_LIST) {
					field.setFormElement(FormElementEnum.DROP_DOWN);
				} else if (me.getValidationStyle() == ValidationStyleEnum.IN_RANGE
						|| me.getValidationStyle() == ValidationStyleEnum.IN_RANGE_EXCLUSIVE) {
					field.setFormElement(FormElementEnum.RANGE);
				} else {
					field.setFormElement(FormElementEnum.TEXT);
				}

				break;
			case FLOAT:
				if (me.getValidationStyle() == ValidationStyleEnum.IN_LIST) {
					field.setFormElement(FormElementEnum.DROP_DOWN);
				} else if (me.getValidationStyle() == ValidationStyleEnum.IN_RANGE
						|| me.getValidationStyle() == ValidationStyleEnum.IN_RANGE_EXCLUSIVE) {
					field.setFormElement(FormElementEnum.RANGE);
				} else {
					field.setFormElement(FormElementEnum.TEXT);
				}

				break;
			case BOOLEAN:
				field.setFormElement(FormElementEnum.CHECK_BOX);

				break;
			case TEXT:
				field.setFormElement(FormElementEnum.TEXT_AREA);

				break;
			case DATE:
				field.setFormElement(FormElementEnum.DATE);

				break;
			case URL:
				field.setFormElement(FormElementEnum.URL);
			default:
				field.setFormElement(FormElementEnum.TEXT);

				break;
			}

			form.getFields().add(field);
		}

		return form;
	}

	@Override
	public FormBotValidationResult validateFormBotField(String json) {
		JsonNode node = null;

		try {
			node = objectMapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			log.error("IOException: Failed to parse input JSON to JsonNode");
			return new FormBotValidationResult(FormBotValidationEnum.ERROR,
					"Bad JSON");
		}

		if (!(node.has("value") && (node.has("fieldUniqueName") || (node
				.has("formUniqueName") && node.has("fieldName"))))) {
			log.error("Insufficient information to find validate field: json must contain value and either fieldUniqueName, or formUniqueName AND fieldName");
			return new FormBotValidationResult(FormBotValidationEnum.ERROR,
					"Bad JSON");
		}

		JargonMetadataResolver resolver = null;
		MetadataTemplate template = null;

		try {
			resolver = new JargonMetadataResolver(irodsAccount,
					irodsAccessObjectFactory);
		} catch (JargonException e) {
			log.error(
					"JargonException: JargonMetadataResolver could not be created",
					e);
			return new FormBotValidationResult(FormBotValidationEnum.ERROR,
					"Could not create JargonMetadataResolver");
		}

		String value = node.get("value").asText();

		String formUuid = "";
		String fieldName = "";

		if (node.has("fieldUniqueName")) {
			String fieldUniqueName = node.get("fieldUniqueName").asText();
			formUuid = fieldUniqueName.substring(0, 36);
			fieldName = fieldUniqueName.substring(36);

		} else if (node.has("formUniqueName")) {
			formUuid = node.get("formUniqueName").asText();
			fieldName = node.get("fieldName").asText();
		} else {
			// This should already have been caught above, but
			// replicated here for completeness
			log.error("Insufficient information to find metadata template: json must contain a field uuid, fqName, or both name and activeDir");
			return new FormBotValidationResult(FormBotValidationEnum.ERROR,
					"Insufficient information to find metadata template");
		}

		try {
			template = resolver.findTemplateByUUID(formUuid);
		} catch (MetadataTemplateParsingException e) {
			log.error("MetadataTemplateParsingException: Error parsing metadata template");
			return new FormBotValidationResult(FormBotValidationEnum.ERROR,
					"Error parsing metadata template");
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException: Metadata template not found");
			return new FormBotValidationResult(FormBotValidationEnum.ERROR,
					"Metadata template not found");
		} catch (MetadataTemplateProcessingException e) {
			log.error("MetadataTemplateProcessingException: Error processing metadata template");
			return new FormBotValidationResult(FormBotValidationEnum.ERROR,
					"Error processing metadata template");
		} catch (IOException e) {
			log.error("IOException: Error reading metadata template from disk");
			return new FormBotValidationResult(FormBotValidationEnum.ERROR,
					"Error reading metadata template from disk");
		}

		// XXX Hard cast to FormBasedMetadataTemplate
		for (MetadataElement me : ((FormBasedMetadataTemplate) template)
				.getElements()) {
			if (me.getName().equalsIgnoreCase(fieldName)) {
				me.setCurrentValue(value);
				ValidationReturnEnum validationReturn = ValidatorSingleton.VALIDATOR
						.validate(me);

				FormBotValidationEnum fbv;
				if (validationReturn == ValidationReturnEnum.SUCCESS) {
					fbv = FormBotValidationEnum.SUCCESS;
				} else if ((validationReturn == ValidationReturnEnum.NOT_VALIDATED)
						|| (validationReturn == ValidationReturnEnum.REGEX_FAILED)) {
					fbv = FormBotValidationEnum.NOT_VALIDATED;
				} else {
					fbv = FormBotValidationEnum.FAILURE;
				}

				return new FormBotValidationResult(fbv,
						validationReturn.toString());
			}

		}

		return new FormBotValidationResult(FormBotValidationEnum.ERROR,
				"Field not found");
	}

	@Override
	public List<FormBotValidationResult> validateFormBotForm(String json) {
		JsonNode node = null;
		List<String> fieldNames = null;
		List<String> fieldValues = null;

		List<FormBotValidationResult> returnList = new ArrayList<FormBotValidationResult>();

		try {
			node = objectMapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			log.error("IOException: Failed to parse input JSON to JsonNode");
			returnList.add(new FormBotValidationResult(
					FormBotValidationEnum.ERROR, "Bad JSON"));
			return returnList;
		}

		if (!(node.has("formUniqueName") && node.has("fields"))) {
			log.error("Insufficient information to find metadata template and fields: json must contain formUniqueName and fields elements");
			returnList.add(new FormBotValidationResult(
					FormBotValidationEnum.ERROR, "Bad JSON"));
			return returnList;
		}

		JargonMetadataResolver resolver = null;
		MetadataTemplate template = null;

		try {
			resolver = new JargonMetadataResolver(irodsAccount,
					irodsAccessObjectFactory);
		} catch (JargonException e) {
			log.error(
					"JargonException: JargonMetadataResolver could not be created",
					e);
		}

		if (resolver == null) {
			log.error("Unable to instantiate JargonMetadataResolver");
			returnList.add(new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Unable to instantiate JargonMetadataResolver"));
			return returnList;
		}

		fieldNames = new ArrayList<String>();
		fieldValues = new ArrayList<String>();

		if (node.get("fields").isArray()) {
			for (JsonNode fieldNode : node.get("fields")) {
				// Legal for value to be empty, but if no field name, ignore
				if (fieldNode.has("fieldName")) {
					fieldNames.add(fieldNode.get("fieldName").asText());
					fieldValues.add(fieldNode.get("value").asText());
				}
			}
		}

		try {
			template = resolver.findTemplateByUUID(node.get("formUniqueName")
					.asText());
		} catch (MetadataTemplateParsingException e) {
			log.error("MetadataTemplateParsingException: Error parsing metadata template");
			returnList.add(new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Error parsing metadata template"));
			return returnList;
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException: Metadata template not found");
			returnList
					.add(new FormBotValidationResult(
							FormBotValidationEnum.ERROR,
							"Metadata template not found"));
			return returnList;
		} catch (MetadataTemplateProcessingException e) {
			log.error("MetadataTemplateProcessingException: Error processing metadata template");
			returnList.add(new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Error processing metadata template"));
			return returnList;
		} catch (IOException e) {
			log.error("IOException: Error reading metadata template from disk");
			returnList.add(new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Error reading metadata template from disk"));
			return returnList;
		}

		boolean validationFailed = false;
		// XXX Hard cast to FormBasedMetadataTemplate
		for (int i = 0; i < fieldNames.size(); i++) {
			for (MetadataElement me : ((FormBasedMetadataTemplate) template)
					.getElements()) {
				String fieldName = fieldNames.get(i);
				String value = fieldValues.get(i);
				if (me.getName().equalsIgnoreCase(fieldName)) {
					me.setCurrentValue(value);
					ValidationReturnEnum validationReturn = ValidatorSingleton.VALIDATOR
							.validate(me);
					if ((validationReturn == ValidationReturnEnum.SUCCESS)
							|| (validationReturn == ValidationReturnEnum.NOT_VALIDATED)
							|| (validationReturn == ValidationReturnEnum.REGEX_SYNTAX_ERROR)) {
						returnList.add(new FormBotValidationResult(
								FormBotValidationEnum.SUCCESS, validationReturn
										.toString()));
						break;
					} else {
						returnList.add(new FormBotValidationResult(
								FormBotValidationEnum.FAILURE, validationReturn
										.toString()));
						validationFailed = true;
						break;
					}
				}
			}
		}

		if (validationFailed) {
			returnList.add(0, new FormBotValidationResult(
					FormBotValidationEnum.FAILURE,
					"At least one field failed validation"));
		} else {
			returnList.add(0, new FormBotValidationResult(
					FormBotValidationEnum.SUCCESS,
					"All fields passed validation"));
		}

		return returnList;
	}

	@Override
	public FormBotExecutionResult executeFormBotField(String json) {
		JsonNode node = null;

		try {
			node = objectMapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			log.error("IOException: Failed to parse input JSON to JsonNode");
			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
					"Bad JSON");
		}

		if (!(node.has("value") && node.has("pathToFile") && (node
				.has("fieldUniqueName") || (node.has("formUniqueName") && node
				.has("fieldName"))))) {
			log.error("Insufficient information to find validate field: json must contain value and either fieldUniqueName, or formUniqueName AND fieldName");
			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
					"Bad JSON");
		}

		JargonMetadataResolver resolver = null;
		MetadataTemplate template = null;

		try {
			resolver = new JargonMetadataResolver(irodsAccount,
					irodsAccessObjectFactory);
		} catch (JargonException e) {
			log.error(
					"JargonException: JargonMetadataResolver could not be created",
					e);
			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
					"Could not create JargonMetadataResolver");
		}

		String value = node.get("value").asText();
		String pathToFile = node.get("pathToFile").asText();

		String formUuid = "";
		String fieldName = "";

		if (node.has("fieldUniqueName")) {
			String fieldUniqueName = node.get("fieldUniqueName").asText();
			formUuid = fieldUniqueName.substring(0, 36);
			fieldName = fieldUniqueName.substring(36);
		} else if (node.has("formUniqueName")) {
			formUuid = node.get("formUniqueName").asText();
			fieldName = node.get("fieldName").asText();
		} else {
			// This should already have been caught above, but
			// replicated here for completeness
			log.error("Insufficient information to find metadata template: json must contain a field uuid, fqName, or both name and activeDir");
			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
					"Insufficient information to find metadata template");
		}

		try {
			template = resolver.findTemplateByUUID(formUuid);
		} catch (MetadataTemplateParsingException e) {
			log.error("MetadataTemplateParsingException: Error parsing metadata template");
			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
					"Error parsing metadata template");
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException: Metadata template not found");
			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
					"Metadata template not found");
		} catch (MetadataTemplateProcessingException e) {
			log.error("MetadataTemplateProcessingException: Error processing metadata template");
			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
					"Error processing metadata template");
		} catch (IOException e) {
			log.error("IOException: Error reading metadata template from disk");
			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
					"Error reading metadata template from disk");
		}

		// XXX Hard cast to FormBasedMetadataTemplate
		for (MetadataElement me : ((FormBasedMetadataTemplate) template)
				.getElements()) {
			if (me.getName().equalsIgnoreCase(fieldName)) {
				me.setCurrentValue(value);
				ValidationReturnEnum validationReturn = ValidatorSingleton.VALIDATOR
						.validate(me);

				if (validationReturn == ValidationReturnEnum.SUCCESS
						|| validationReturn == ValidationReturnEnum.NOT_VALIDATED
						|| validationReturn == ValidationReturnEnum.REGEX_SYNTAX_ERROR) {

					String unit = JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
							+ formUuid;
					AvuData avuData;
					try {
						avuData = AvuData.instance(fieldName, value, unit);
						irodsAccessObjectFactory.getDataObjectAO(irodsAccount)
								.addAVUMetadata(pathToFile, avuData);
					} catch (JargonException e) {
						log.error("JargonException when trying to add metadata to data object");
						return new FormBotExecutionResult(
								FormBotExecutionEnum.ERROR,
								"JargonException when trying to add metadata to data object");
					}

					return new FormBotExecutionResult(
							FormBotExecutionEnum.SUCCESS,
							"Metadata added to data object");
				} else {
					String retString = "Validation failed for field "
							+ fieldName + " with value " + value;
					return new FormBotExecutionResult(
							FormBotExecutionEnum.VALIDATION_FAILED, retString);
				}
			}
		}
		return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
				"Field not found");
	}

	@Override
	public List<FormBotExecutionResult> executeFormBotForm(String json) {
		JsonNode node = null;
		List<String> fieldNames = null;
		List<String> fieldValues = null;

		List<FormBotExecutionResult> returnList = new ArrayList<FormBotExecutionResult>();

		try {
			node = objectMapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			log.error("IOException: Failed to parse input JSON to JsonNode");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR, "Bad JSON"));
			return returnList;
		}

		if (!(node.has("formUniqueName") && node.has("pathToFile") && node
				.has("fields"))) {
			log.error("Insufficient information to find metadata template and fields: json must contain formUniqueName and fields elements");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR, "Bad JSON"));
			return returnList;
		}

		JargonMetadataResolver resolver = null;
		MetadataTemplate template = null;

		try {
			resolver = new JargonMetadataResolver(irodsAccount,
					irodsAccessObjectFactory);
		} catch (JargonException e) {
			log.error(
					"JargonException: JargonMetadataResolver could not be created",
					e);
		}

		if (resolver == null) {
			log.error("Unable to instantiate JargonMetadataResolver");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR,
					"Unable to instantiate JargonMetadataResolver"));
			return returnList;
		}

		String pathToFile = node.get("pathToFile").asText();
		String formUuid = node.get("formUniqueName").asText();

		fieldNames = new ArrayList<String>();
		fieldValues = new ArrayList<String>();

		if (node.get("fields").isArray()) {
			for (JsonNode fieldNode : node.get("fields")) {
				// Legal for value to be empty, but if no field name, ignore
				if (fieldNode.has("fieldName")) {
					fieldNames.add(fieldNode.get("fieldName").asText());
					fieldValues.add(fieldNode.get("value").asText());
				}
			}
		}

		try {
			template = resolver.findTemplateByUUID(formUuid);
		} catch (MetadataTemplateParsingException e) {
			log.error("MetadataTemplateParsingException: Error parsing metadata template");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR,
					"Error parsing metadata template"));
			return returnList;
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException: Metadata template not found");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR, "Metadata template not found"));
			return returnList;
		} catch (MetadataTemplateProcessingException e) {
			log.error("MetadataTemplateProcessingException: Error processing metadata template");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR,
					"Error processing metadata template"));
			return returnList;
		} catch (IOException e) {
			log.error("IOException: Error reading metadata template from disk");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR,
					"Error reading metadata template from disk"));
			return returnList;
		}
		
		boolean validationFailed = false;
		boolean error = false;

		// XXX Hard cast to FormBasedMetadataTemplate
		for (int i = 0; i < fieldNames.size(); i++) {
			for (MetadataElement me : ((FormBasedMetadataTemplate) template)
					.getElements()) {
				String fieldName = fieldNames.get(i);
				String value = fieldValues.get(i);
				if (me.getName().equalsIgnoreCase(fieldName)) {
					me.setCurrentValue(value);
					ValidationReturnEnum validationReturn = ValidatorSingleton.VALIDATOR
							.validate(me);
					if (validationReturn == ValidationReturnEnum.SUCCESS
							|| validationReturn == ValidationReturnEnum.NOT_VALIDATED
							|| validationReturn == ValidationReturnEnum.REGEX_SYNTAX_ERROR) {

						String unit = JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
								+ formUuid;
						AvuData avuData;
						try {
							avuData = AvuData.instance(fieldName, value, unit);
							irodsAccessObjectFactory.getDataObjectAO(
									irodsAccount).addAVUMetadata(pathToFile,
									avuData);
						} catch (JargonException e) {
							log.error("JargonException when trying to add metadata to data object");
							returnList
									.add(new FormBotExecutionResult(
											FormBotExecutionEnum.ERROR,
											"JargonException when adding metadata to data obj"));
							error = true;
							break;
						}
						
						returnList.add(new FormBotExecutionResult(
								FormBotExecutionEnum.SUCCESS,
								validationReturn.toString()));
						break;
					} else {
						returnList.add(new FormBotExecutionResult(
								FormBotExecutionEnum.VALIDATION_FAILED,
								validationReturn.toString()));
						validationFailed = true;
						break;
					}
				}
			}
		}

		if (error) {
			returnList.add(0, new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR,
					"At least one field generated an error"));
		} else if (validationFailed) {
			returnList.add(0, new FormBotExecutionResult(
					FormBotExecutionEnum.VALIDATION_FAILED,
					"At least one field failed validation"));			
		} else {
			returnList.add(0, new FormBotExecutionResult(
					FormBotExecutionEnum.SUCCESS,
					"All fields passed validation"));
		}

		return returnList;
	}

	public MetadataTemplateFormBotService() {
		super();
	}

	public MetadataTemplateFormBotService(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}
}
