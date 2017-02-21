package org.irods.jargon.formbot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
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
import org.irods.jargon.metadatatemplate.JargonMetadataExporter;
import org.irods.jargon.metadatatemplate.JargonMetadataResolver;
import org.irods.jargon.metadatatemplate.MetadataElement;
import org.irods.jargon.metadatatemplate.MetadataTemplate;
import org.irods.jargon.metadatatemplate.FormBasedMetadataTemplate;
import org.irods.jargon.metadatatemplate.MetadataTemplateParsingException;
import org.irods.jargon.metadatatemplate.MetadataTemplateProcessingException;
import org.irods.jargon.metadatatemplate.TemplateTypeEnum;
import org.irods.jargon.metadatatemplate.ValidationReturnEnum;
import org.irods.jargon.metadatatemplate.ValidationStyleEnum;
import org.irods.jargon.metadatatemplate.ValidatorSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class MetadataTemplateFormBotService extends AbstractJargonService
		implements FormBotService {
	static private Logger log = LoggerFactory
			.getLogger(IRODSFileFactoryImpl.class);
	static private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	// public FormBotForm buildFormBotForm(String json) {
	public String buildFormBotForm(String json) {

		JsonNode node = null;

		try {
			node = objectMapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			log.error("IOException: Failed to parse input JSON to JsonNode");
		}

		if (!(node.has("uuid") || node.has("fqName") || (node.has("name") && node
				.has("activeDir")))) {
			log.error("Insufficient information to find metadata template: json must contain a field uuid, fqName, or both name and activeDir");
			// return null;
			return "";
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
			// return null;
			return "";
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
				// return null;
				return "";
			}
		} catch (FileNotFoundException e) {
			log.error("Metadata template file not found");
			// return null;
			return "";
		} catch (MetadataTemplateParsingException e) {
			log.error("Error parsing metadata template file JSON");
			// return null;
			return "";
		} catch (MetadataTemplateProcessingException e) {
			log.error("Error when processing metadata template file");
			// return null;
			return "";
		} catch (IOException e) {
			log.error("IOException when trying to load metadata template file");
			// return null;
			return "";
		}

		uuidString = template.getUuid().toString();

		form.setName(template.getName());
		form.setDescription(template.getDescription());
		form.setUniqueId(uuidString);

		if (template.getType() == TemplateTypeEnum.FORM_BASED) {
			FormBasedMetadataTemplate fbmt = (FormBasedMetadataTemplate) template;
			for (MetadataElement me : fbmt.getElements()) {
				FormBotField field = new FormBotField();
				field.setName(me.getName());
				field.setDescription(me.getDescription());
				field.setType(me.getType());
				field.setCurrentValue(me.getCurrentValue());
				field.setDisplayValue(me.getDisplayValue());
				field.setDefaultValue(me.getDefaultValue());

				// uniqueId is template UUID + field name
				// i.e. 01234567-0123-0123-0123-0123456789abFieldName
				// UUID is in first 36 characters
				String uniqueId = uuidString + me.getName();
				field.setUniqueId(uniqueId);

				for (String s : me.getValidationOptions()) {
					field.getParamList().add(s);
				}

				// TODO if me.renderingOptions exists, should really look at
				// that instead. But it seems pretty redundant at this point.
				switch (me.getType()) {
				case RAW_STRING:
					if (me.getValidationStyle() == ValidationStyleEnum.IN_LIST) {
						field.setFormElement(FormElementEnum.SELECT);
					} else {
						field.setFormElement(FormElementEnum.TEXT);
					}

					break;
				case RAW_TEXT:
					field.setFormElement(FormElementEnum.TEXT_AREA);

					break;
				case RAW_URL:
					field.setFormElement(FormElementEnum.URL);

					break;
				case RAW_INT:
				case RAW_FLOAT:
					if (me.getValidationStyle() == ValidationStyleEnum.IN_LIST) {
						field.setFormElement(FormElementEnum.SELECT);
					} else if (me.getValidationStyle() == ValidationStyleEnum.IN_RANGE
							|| me.getValidationStyle() == ValidationStyleEnum.IN_RANGE_EXCLUSIVE) {
						field.setFormElement(FormElementEnum.RANGE);
					} else {
						field.setFormElement(FormElementEnum.NUMBER);
					}

					break;
				case RAW_BOOLEAN:
					field.setFormElement(FormElementEnum.CHECK_BOX);

					break;
				case RAW_DATE:
					field.setFormElement(FormElementEnum.DATE);

					break;
				case RAW_TIME:
					field.setFormElement(FormElementEnum.TIME);

					break;
				case RAW_DATETIME:
					field.setFormElement(FormElementEnum.DATETIME);

					break;
				case REF_IRODS_QUERY:
				case REF_IRODS_CATALOG:
					field.setFormElement(FormElementEnum.TEXT);

					break;
				case REF_URL:
					field.setFormElement(FormElementEnum.URL);

					break;
				case LIST_STRING:
					if (me.getValidationStyle() == ValidationStyleEnum.IN_LIST) {
						field.setFormElement(FormElementEnum.SELECT_MULTIPLE);
					} else {
						field.setFormElement(FormElementEnum.TEXT);
					}

					break;
				case LIST_INT:
				case LIST_FLOAT:
					if (me.getValidationStyle() == ValidationStyleEnum.IN_LIST) {
						field.setFormElement(FormElementEnum.SELECT_MULTIPLE);
					} else if (me.getValidationStyle() == ValidationStyleEnum.IN_RANGE
							|| me.getValidationStyle() == ValidationStyleEnum.IN_RANGE_EXCLUSIVE) {
						field.setFormElement(FormElementEnum.RANGE);
					} else {
						field.setFormElement(FormElementEnum.NUMBER);
					}

					break;
				default:
					field.setFormElement(FormElementEnum.TEXT);

					break;
				}

				form.getFields().add(field);
			}
		} // TODO else if (other type of MetadataTemplate)

		// return form;

		String returnJson = "";

		try {
			returnJson = objectMapper.writeValueAsString(form);
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException when writing form to json String");
		}

		return returnJson;
	}

	@Override
	// public FormBotValidationResult validateFormBotField(String json) {
	public String validateFormBotField(String json) {
		JsonNode node = null;
		FormBotValidationResult validationResult = null;
		String returnJson = "";

		try {
			node = objectMapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			log.error("IOException: Failed to parse input JSON to JsonNode");
			// return new FormBotValidationResult(FormBotValidationEnum.ERROR,
			// "Bad JSON");
			validationResult = new FormBotValidationResult(
					FormBotValidationEnum.ERROR, "Bad JSON");
			try {
				returnJson = objectMapper.writeValueAsString(validationResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
		}

		if (!(node.has("value") && (node.has("fieldUniqueName") || (node
				.has("formUniqueName") && node.has("fieldName"))))) {
			log.error("Insufficient information to find validate field: json must contain value and either fieldUniqueName, or formUniqueName AND fieldName");
			// return new FormBotValidationResult(FormBotValidationEnum.ERROR,
			// "Bad JSON");
			validationResult = new FormBotValidationResult(
					FormBotValidationEnum.ERROR, "Bad JSON");
			try {
				returnJson = objectMapper.writeValueAsString(validationResult);
			} catch (JsonProcessingException e) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
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
			// return new FormBotValidationResult(FormBotValidationEnum.ERROR,
			// "Could not create JargonMetadataResolver");
			validationResult = new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Could not create JargonMetadataResolver");
			try {
				returnJson = objectMapper.writeValueAsString(validationResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		}

		List<String> value = new ArrayList<String>();

		if (node.get("value").isArray()) {
			ArrayNode valuesNode = (ArrayNode) node.get("value");
			Iterator<JsonNode> valuesIterator = valuesNode.elements();
			while (valuesIterator.hasNext()) {
				JsonNode valueNode = valuesIterator.next();
				value.add(valueNode.asText());
			}
		} else {
			value.add(node.get("value").asText());
		}

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
			// return new FormBotValidationResult(FormBotValidationEnum.ERROR,
			// "Insufficient information to find metadata template");
			validationResult = new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Insufficient information to find metadata template");
			try {
				returnJson = objectMapper.writeValueAsString(validationResult);
			} catch (JsonProcessingException e) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		}

		try {
			template = resolver.findTemplateByUUID(formUuid);
		} catch (MetadataTemplateParsingException e) {
			log.error("MetadataTemplateParsingException: Error parsing metadata template");
			// return new FormBotValidationResult(FormBotValidationEnum.ERROR,
			// "Error parsing metadata template");
			validationResult = new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Error parsing metadata template");
			try {
				returnJson = objectMapper.writeValueAsString(validationResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException: Metadata template not found");
			// return new FormBotValidationResult(FormBotValidationEnum.ERROR,
			// "Metadata template not found");
			validationResult = new FormBotValidationResult(
					FormBotValidationEnum.ERROR, "Metadata template not found");
			try {
				returnJson = objectMapper.writeValueAsString(validationResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		} catch (MetadataTemplateProcessingException e) {
			log.error("MetadataTemplateProcessingException: Error processing metadata template");
			// return new FormBotValidationResult(FormBotValidationEnum.ERROR,
			// "Error processing metadata template");
			validationResult = new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Error processing metadata template");
			try {
				returnJson = objectMapper.writeValueAsString(validationResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		} catch (IOException e) {
			log.error("IOException: Error reading metadata template from disk");
			// return new FormBotValidationResult(FormBotValidationEnum.ERROR,
			// "Error reading metadata template from disk");
			validationResult = new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Error reading metadata template from disk");
			try {
				returnJson = objectMapper.writeValueAsString(validationResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		}

		if (template.getType() == TemplateTypeEnum.FORM_BASED) {
			FormBasedMetadataTemplate fbmt = (FormBasedMetadataTemplate) template;
			for (MetadataElement me : fbmt.getElements()) {
				if (me.getName().equalsIgnoreCase(fieldName)) {
					me.setCurrentValue(value);
					ValidationReturnEnum validationReturn = ValidatorSingleton.VALIDATOR
							.validate(irodsAccount, irodsAccessObjectFactory,
									me);

					FormBotValidationEnum fbv;
					if (validationReturn == ValidationReturnEnum.SUCCESS) {
						fbv = FormBotValidationEnum.SUCCESS;
					} else if ((validationReturn == ValidationReturnEnum.NOT_VALIDATED)
							|| (validationReturn == ValidationReturnEnum.REGEX_FAILED)) {
						fbv = FormBotValidationEnum.NOT_VALIDATED;
					} else {
						fbv = FormBotValidationEnum.FAILURE;
					}

					// return new FormBotValidationResult(fbv,
					// validationReturn.toString());
					validationResult = new FormBotValidationResult(fbv,
							validationReturn.toString());
					try {
						returnJson = objectMapper
								.writeValueAsString(validationResult);
					} catch (JsonProcessingException e) {
						log.error("JsonProcessingException when writing FormBotValidationResult to json String");
					}
					return returnJson;
				}
			}
		} // TODO else if (other type of MetadataTemplate)

		// return new FormBotValidationResult(FormBotValidationEnum.ERROR,
		// "Field not found");
		validationResult = new FormBotValidationResult(
				FormBotValidationEnum.ERROR, "Field not found");
		try {
			returnJson = objectMapper.writeValueAsString(validationResult);
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException when writing FormBotValidationResult to json String");
		}
		return returnJson;
	}

	@Override
	// public List<FormBotValidationResult> validateFormBotForm(String json) {
	public String validateFormBotForm(String json) {
		JsonNode node = null;
		List<String> fieldNames = null;
		List<List<String>> fieldValues = null;

		List<FormBotValidationResult> returnList = new ArrayList<FormBotValidationResult>();
		String returnJson = "";

		try {
			node = objectMapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			log.error("IOException: Failed to parse input JSON to JsonNode");
			returnList.add(new FormBotValidationResult(
					FormBotValidationEnum.ERROR, "Bad JSON"));
			// return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotValidationResult> to json String");
			}
			return returnJson;
		}

		if (!(node.has("formUniqueName") && node.has("fields"))) {
			log.error("Insufficient information to find metadata template and fields: json must contain formUniqueName and fields elements");
			returnList.add(new FormBotValidationResult(
					FormBotValidationEnum.ERROR, "Bad JSON"));
			// return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e) {
				log.error("JsonProcessingException when writing List<FormBotValidationResult> to json String");
			}
			return returnJson;
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
			// return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e) {
				log.error("JsonProcessingException when writing List<FormBotValidationResult> to json String");
			}
			return returnJson;
		}

		fieldNames = new ArrayList<String>();
		fieldValues = new ArrayList<List<String>>();

		if (node.get("fields").isArray()) {
			for (JsonNode fieldNode : node.get("fields")) {
				// Legal for value to be empty, but if no field name, ignore
				if (fieldNode.has("fieldName")) {
					List<String> value = new ArrayList<String>();

					fieldNames.add(fieldNode.get("fieldName").asText());

					if (fieldNode.get("value").isArray()) {
						ArrayNode valuesNode = (ArrayNode) fieldNode
								.get("value");
						Iterator<JsonNode> valuesIterator = valuesNode
								.elements();
						while (valuesIterator.hasNext()) {
							JsonNode valueNode = valuesIterator.next();
							value.add(valueNode.asText());
						}
					} else {
						value.add(fieldNode.get("value").asText());
					}

					fieldValues.add(value);
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
			// return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotValidationResult> to json String");
			}
			return returnJson;
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException: Metadata template not found");
			returnList
					.add(new FormBotValidationResult(
							FormBotValidationEnum.ERROR,
							"Metadata template not found"));
			// return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotValidationResult> to json String");
			}
			return returnJson;
		} catch (MetadataTemplateProcessingException e) {
			log.error("MetadataTemplateProcessingException: Error processing metadata template");
			returnList.add(new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Error processing metadata template"));
			// return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotValidationResult> to json String");
			}
			return returnJson;
		} catch (IOException e) {
			log.error("IOException: Error reading metadata template from disk");
			returnList.add(new FormBotValidationResult(
					FormBotValidationEnum.ERROR,
					"Error reading metadata template from disk"));
			// return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotValidationResult> to json String");
			}
			return returnJson;
		}

		boolean validationFailed = false;

		if (template.getType() == TemplateTypeEnum.FORM_BASED) {
			FormBasedMetadataTemplate fbmt = (FormBasedMetadataTemplate) template;
			for (int i = 0; i < fieldNames.size(); i++) {
				for (MetadataElement me : fbmt.getElements()) {
					String fieldName = fieldNames.get(i);
					List<String> value = fieldValues.get(i);
					if (me.getName().equalsIgnoreCase(fieldName)) {
						me.setCurrentValue(value);
						ValidationReturnEnum validationReturn = ValidatorSingleton.VALIDATOR
								.validate(irodsAccount,
										irodsAccessObjectFactory, me);
						if ((validationReturn == ValidationReturnEnum.SUCCESS)
								|| (validationReturn == ValidationReturnEnum.NOT_VALIDATED)
								|| (validationReturn == ValidationReturnEnum.REGEX_SYNTAX_ERROR)) {
							returnList.add(new FormBotValidationResult(
									FormBotValidationEnum.SUCCESS,
									validationReturn.toString()));
							break;
						} else {
							returnList.add(new FormBotValidationResult(
									FormBotValidationEnum.FAILURE,
									validationReturn.toString()));
							validationFailed = true;
							break;
						}
					}
				}
			}
		} // TODO else if (other type of MetadataTemplate)

		if (validationFailed) {
			returnList.add(0, new FormBotValidationResult(
					FormBotValidationEnum.FAILURE,
					"At least one field failed validation"));
		} else {
			returnList.add(0, new FormBotValidationResult(
					FormBotValidationEnum.SUCCESS,
					"All fields passed validation"));
		}

		// return returnList;
		try {
			returnJson = objectMapper.writeValueAsString(returnList);
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException when writing List<FormBotValidationResult> to json String");
		}
		return returnJson;
	}

	@Override
	// public FormBotExecutionResult executeFormBotField(String json) {
	public String executeFormBotField(String json) {
		JsonNode node = null;
		FormBotExecutionResult executionResult = null;
		String returnJson = "";

		try {
			node = objectMapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			log.error("IOException: Failed to parse input JSON to JsonNode");
//			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
//					"Bad JSON");
			executionResult = new FormBotExecutionResult(FormBotExecutionEnum.ERROR, "Bad JSON");
			try {
				returnJson = objectMapper.writeValueAsString(executionResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		}

		if (!(node.has("value") && node.has("pathToObject") && (node
				.has("fieldUniqueName") || (node.has("formUniqueName") && node
				.has("fieldName"))))) {
			log.error("Insufficient information to find validate field: json must contain value and either fieldUniqueName, or formUniqueName AND fieldName");
//			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
//					"Bad JSON");
			executionResult = new FormBotExecutionResult(FormBotExecutionEnum.ERROR, "Bad JSON");
			try {
				returnJson = objectMapper.writeValueAsString(executionResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		}

		JargonMetadataResolver resolver = null;
		JargonMetadataExporter exporter = null;
		MetadataTemplate template = null;

		try {
			resolver = new JargonMetadataResolver(irodsAccount,
					irodsAccessObjectFactory);
		} catch (JargonException e) {
			log.error(
					"JargonException: JargonMetadataResolver could not be created",
					e);
//			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
//					"Could not create JargonMetadataResolver");
			executionResult = new FormBotExecutionResult(FormBotExecutionEnum.ERROR, "Could not create JargonMetadataResolver");
			try {
				returnJson = objectMapper.writeValueAsString(executionResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		}

		exporter = new JargonMetadataExporter(irodsAccessObjectFactory,
				irodsAccount);

		List<String> value = new ArrayList<String>();

		if (node.get("value").isArray()) {
			ArrayNode valuesNode = (ArrayNode) node.get("value");
			Iterator<JsonNode> valuesIterator = valuesNode.elements();
			while (valuesIterator.hasNext()) {
				JsonNode valueNode = valuesIterator.next();
				value.add(valueNode.asText());
			}
		} else {
			value.add(node.get("value").asText());
		}

		String pathToObject = node.get("pathToObject").asText();

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
//			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
//					"Insufficient information to find metadata template");
			executionResult = new FormBotExecutionResult(FormBotExecutionEnum.ERROR, "Insufficient information to find MetadataTemplate");
			try {
				returnJson = objectMapper.writeValueAsString(executionResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		}

		try {
			template = resolver.findTemplateByUUID(formUuid);
		} catch (MetadataTemplateParsingException e) {
			log.error("MetadataTemplateParsingException: Error parsing metadata template");
//			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
//					"Error parsing metadata template");
			executionResult = new FormBotExecutionResult(FormBotExecutionEnum.ERROR, "Error parsing MetadataTemplate");
			try {
				returnJson = objectMapper.writeValueAsString(executionResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException: Metadata template not found");
//			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
//					"Metadata template not found");
			executionResult = new FormBotExecutionResult(FormBotExecutionEnum.ERROR, "MetadataTemplate not found");
			try {
				returnJson = objectMapper.writeValueAsString(executionResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		} catch (MetadataTemplateProcessingException e) {
			log.error("MetadataTemplateProcessingException: Error processing metadata template");
//			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
//					"Error processing metadata template");
			executionResult = new FormBotExecutionResult(FormBotExecutionEnum.ERROR, "Error processing MetadataTemplate");
			try {
				returnJson = objectMapper.writeValueAsString(executionResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		} catch (IOException e) {
			log.error("IOException: Error reading metadata template from disk");
//			return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
//					"Error reading metadata template from disk");
			executionResult = new FormBotExecutionResult(FormBotExecutionEnum.ERROR, "Error reading MetadataTemplate from disk");
			try {
				returnJson = objectMapper.writeValueAsString(executionResult);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing FormBotValidationResult to json String");
			}
			return returnJson;
		}

		if (template.getType() == TemplateTypeEnum.FORM_BASED) {
			FormBasedMetadataTemplate fbmt = (FormBasedMetadataTemplate) template;
			for (MetadataElement me : fbmt.getElements()) {

				if (me.getName().equalsIgnoreCase(fieldName)) {
					me.setCurrentValue(value);
					ValidationReturnEnum validationReturn = ValidatorSingleton.VALIDATOR
							.validate(irodsAccount, irodsAccessObjectFactory,
									me);

					if (validationReturn == ValidationReturnEnum.SUCCESS
							|| validationReturn == ValidationReturnEnum.NOT_VALIDATED
							|| validationReturn == ValidationReturnEnum.REGEX_SYNTAX_ERROR) {
						try {
							exporter.saveElementToSystemMetadataOnObject(me,
									pathToObject);
						} catch (JargonException e) {
							log.error("JargonException when trying to add metadata to data object");
//							return new FormBotExecutionResult(
//									FormBotExecutionEnum.ERROR,
//									"JargonException when trying to add metadata to data object");
							executionResult = new FormBotExecutionResult(FormBotExecutionEnum.ERROR, "JargonException when trying to save metadata to iRODS object");
							try {
								returnJson = objectMapper.writeValueAsString(executionResult);
							} catch (JsonProcessingException e1) {
								log.error("JsonProcessingException when writing FormBotValidationResult to json String");
							}
							return returnJson;
						}

//						return new FormBotExecutionResult(
//								FormBotExecutionEnum.SUCCESS,
//								"Metadata added to data object");
						executionResult = new FormBotExecutionResult(FormBotExecutionEnum.SUCCESS, "Metadata added to iRODS object");
						try {
							returnJson = objectMapper.writeValueAsString(executionResult);
						} catch (JsonProcessingException e1) {
							log.error("JsonProcessingException when writing FormBotValidationResult to json String");
						}
						return returnJson;
					} else {
						String retString = "Validation failed for field "
								+ fieldName + " with value " + value;
//						return new FormBotExecutionResult(
//								FormBotExecutionEnum.VALIDATION_FAILED,
//								retString);
						executionResult = new FormBotExecutionResult(FormBotExecutionEnum.VALIDATION_FAILED, retString);
						try {
							returnJson = objectMapper.writeValueAsString(executionResult);
						} catch (JsonProcessingException e1) {
							log.error("JsonProcessingException when writing FormBotValidationResult to json String");
						}
						return returnJson;
					}
				}
			}
		} // TODO else if (other type of MetadataTemplate)

//		return new FormBotExecutionResult(FormBotExecutionEnum.ERROR,
//				"Field not found");
		executionResult = new FormBotExecutionResult(FormBotExecutionEnum.ERROR, "Field not found");
		try {
			returnJson = objectMapper.writeValueAsString(executionResult);
		} catch (JsonProcessingException e1) {
			log.error("JsonProcessingException when writing FormBotValidationResult to json String");
		}
		return returnJson;
	}

	@Override
	// public List<FormBotExecutionResult> executeFormBotForm(String json) {
	public String executeFormBotForm(String json) {
		JsonNode node = null;
		List<String> fieldNames = null;
		List<List<String>> fieldValues = null;

		List<FormBotExecutionResult> returnList = new ArrayList<FormBotExecutionResult>();
		String returnJson = "";

		try {
			node = objectMapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			log.error("IOException: Failed to parse input JSON to JsonNode");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR, "Bad JSON"));
//			return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotExecutionResult> to json String");
			}
			return returnJson;
		}

		if (!(node.has("formUniqueName") && node.has("pathToObject") && node
				.has("fields"))) {
			log.error("Insufficient information to find metadata template and fields: json must contain formUniqueName and fields elements");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR, "Bad JSON"));
//			return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotExecutionResult> to json String");
			}
			return returnJson;
		}

		JargonMetadataResolver resolver = null;
		JargonMetadataExporter exporter = null;
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
//			return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotExecutionResult> to json String");
			}
			return returnJson;
		}

		exporter = new JargonMetadataExporter(irodsAccessObjectFactory,
				irodsAccount);

		String pathToObject = node.get("pathToObject").asText();
		String formUuid = node.get("formUniqueName").asText();

		fieldNames = new ArrayList<String>();
		fieldValues = new ArrayList<List<String>>();

		if (node.get("fields").isArray()) {
			for (JsonNode fieldNode : node.get("fields")) {
				// Legal for value to be empty, but if no field name, ignore
				if (fieldNode.has("fieldName")) {
					List<String> value = new ArrayList<String>();

					fieldNames.add(fieldNode.get("fieldName").asText());

					if (fieldNode.get("value").isArray()) {
						ArrayNode valuesNode = (ArrayNode) fieldNode
								.get("value");
						Iterator<JsonNode> valuesIterator = valuesNode
								.elements();
						while (valuesIterator.hasNext()) {
							JsonNode valueNode = valuesIterator.next();
							value.add(valueNode.asText());
						}
					} else {
						value.add(fieldNode.get("value").asText());
					}

					fieldValues.add(value);
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
//			return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotExecutionResult> to json String");
			}
			return returnJson;
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException: Metadata template not found");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR, "Metadata template not found"));
//			return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotExecutionResult> to json String");
			}
			return returnJson;
		} catch (MetadataTemplateProcessingException e) {
			log.error("MetadataTemplateProcessingException: Error processing metadata template");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR,
					"Error processing metadata template"));
//			return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotExecutionResult> to json String");
			}
			return returnJson;
		} catch (IOException e) {
			log.error("IOException: Error reading metadata template from disk");
			returnList.add(new FormBotExecutionResult(
					FormBotExecutionEnum.ERROR,
					"Error reading metadata template from disk"));
//			return returnList;
			try {
				returnJson = objectMapper.writeValueAsString(returnList);
			} catch (JsonProcessingException e1) {
				log.error("JsonProcessingException when writing List<FormBotExecutionResult> to json String");
			}
			return returnJson;
		}

		boolean validationFailed = false;
		boolean error = false;

		if (template.getType() == TemplateTypeEnum.FORM_BASED) {
			FormBasedMetadataTemplate fbmt = (FormBasedMetadataTemplate) template;
			for (int i = 0; i < fieldNames.size(); i++) {
				for (MetadataElement me : fbmt.getElements()) {
					String fieldName = fieldNames.get(i);
					List<String> value = fieldValues.get(i);
					if (me.getName().equalsIgnoreCase(fieldName)) {
						me.setCurrentValue(value);
						ValidationReturnEnum validationReturn = ValidatorSingleton.VALIDATOR
								.validate(irodsAccount,
										irodsAccessObjectFactory, me);
						if (validationReturn == ValidationReturnEnum.SUCCESS
								|| validationReturn == ValidationReturnEnum.NOT_VALIDATED
								|| validationReturn == ValidationReturnEnum.REGEX_SYNTAX_ERROR) {
							try {
								exporter.saveElementToSystemMetadataOnObject(
										me, pathToObject);
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
		} // TODO else if (other type of MetadataTemplate)

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
					"Metadata added to iRODS object"));
		}

//		return returnList;
		try {
			returnJson = objectMapper.writeValueAsString(returnList);
		} catch (JsonProcessingException e1) {
			log.error("JsonProcessingException when writing List<FormBotExecutionResult> to json String");
		}
		return returnJson;
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
