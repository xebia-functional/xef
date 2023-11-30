package ai.xef.openai.generator;

import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.languages.KotlinClientCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

import java.util.*;
import static java.util.Map.entry;

public class KMMGeneratorConfig extends KotlinClientCodegen {

    private final Map<String, List<String>> nonRequiredFields = new LinkedHashMap<>();

    public KMMGeneratorConfig() {
        super();
        specialCharReplacements.put("-", "_");
        specialCharReplacements.put(".", "_");
        enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.snake_case;
        nonRequiredFields.putAll(
            Map.ofEntries(
                entry("ListAssistantFilesResponse", List.of("firstId", "lastId")),
                entry("ListAssistantsResponse", List.of("firstId", "lastId")),
                entry("ListMessageFilesResponse", List.of("firstId", "lastId")),
                entry("ListMessagesResponse", List.of("firstId", "lastId")),
                entry("ListRunsResponse", List.of("firstId", "lastId")),
                entry("ListRunStepsResponse", List.of("firstId", "lastId")),
                entry("ListThreadsResponse", List.of("firstId", "lastId")),
                entry("MessageObject", List.of("metadata")),
                entry("MessageObjectContentInner", List.of("imageFile", "text")),
                entry("RunObject", List.of("expiresAt", "requiredAction")),
                entry("RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner", List.of("logs", "image")),
                entry("RunStepDetailsToolCallsFunctionObjectFunction", List.of("output")),
                entry("RunStepDetailsToolCallsObjectToolCallsInner", List.of("codeInterpreter", "retrieval", "function")),
                entry("RunStepDetailsToolCallsRetrievalObject", List.of("retrieval")),
                entry("RunStepObject", List.of("expiredAt", "metadata")),
                entry("MessageContentTextObjectTextAnnotationsInner", List.of("filePath", "fileCitation"))
            )
        );
    }

    private Optional<CodegenProperty> readEnumModel(List<CodegenProperty> all) {
        if (all.size() == 2) {
            CodegenProperty first = all.get(0);
            CodegenProperty second = all.get(1);
            if (first.isString && second.isEnum) {
                return Optional.of(second);
            }
        }
        return Optional.empty();
    }

    @Override
    public ModelsMap postProcessModels(ModelsMap objs) {
        for (ModelMap mo : objs.getModels()) {
            CodegenModel cm = mo.getModel();
            if (cm.anyOf != null && !cm.anyOf.isEmpty()) {
                Optional<CodegenProperty> codegenProperty = readEnumModel(cm.getComposedSchemas().getAnyOf());
                codegenProperty.ifPresent((enumProp) -> {
                    cm.modelJson = enumProp.jsonSchema;
                    cm.interfaces = null;
                    cm.anyOf = new HashSet<>();
                    cm.dataType = enumProp.dataType;
                    cm.isString = enumProp.isString;
                    cm.allowableValues = enumProp.allowableValues;
                    cm.isEnum = enumProp.isEnum;
                    cm.setIsAnyType(enumProp.isAnyType);
                    cm.setComposedSchemas(null);
                });
            } else if (cm.name.endsWith("Request")) {
                cm
                        .allVars
                        .stream()
                        .filter(p -> p.name.equalsIgnoreCase("model"))
                        .findFirst()
                        .filter(p -> !p.dataType.equals("kotlin.String"))
                        .ifPresent(p -> p.dataType = String.format("ai.xef.openai.OpenAIModel<%s>", p.dataType));
            } else if (nonRequiredFields.containsKey(cm.classname)) {
                List<String> fields = nonRequiredFields.getOrDefault(cm.classname, Collections.emptyList());
                cm
                        .allVars
                        .stream()
                        .filter(p -> fields.contains(p.name))
                        .forEach(p -> p.setRequired(false));
            }
        }
        return super.postProcessModels(objs);
    }

    @Override
    public String toEnumVarName(String value, String datatype) {
        String varName;
        if ("length".equals(value)) {
            varName = value + "Type";
        } else {
            varName = value;
        }
        return super.toEnumVarName(varName, datatype);
    }
}
