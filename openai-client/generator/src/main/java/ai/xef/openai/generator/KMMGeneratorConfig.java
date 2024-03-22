package ai.xef.openai.generator;

import com.google.common.collect.ImmutableMap;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.*;
import org.openapitools.codegen.languages.KotlinClientCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.model.OperationsMap;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

public class KMMGeneratorConfig extends KotlinClientCodegen {

    private final Map<String, List<String>> nonRequiredFields = new LinkedHashMap<>();

    public KMMGeneratorConfig() {
        super();

        // Generate in src/commonMain/kotlin, not /src/main/kotlin
        additionalProperties.put("sourceFolder", "src/commonMain/kotlin");
        additionalProperties.put("generateModelTests", false);
        additionalProperties.put("generateApiTests", false);
        additionalProperties.put("generateInfrastructure", false);
        setModelPackage("com.xebia.functional.openai.generated.model");
        setApiPackage("com.xebia.functional.openai.generated.api");
        additionalProperties.put("apiSuffix", "");

        // Configure OpenAI `object` to be mapped to `JsonObject`
        typeMapping.put("object", "JsonObject");
        importMapping.put("JsonObject", "kotlinx.serialization.json.JsonObject");

        typeMapping.put("java.net.URI", "kotlin.String");

        typeMapping.put("java.math.BigDecimal", "kotlin.Double");
        importMapping.put("BigDecimal", "kotlin.Double");

        typeMapping.put("java.io.File", "UploadFile");
        importMapping.put("java.io.File", "com.xebia.functional.openai.UploadFile");

        // Maps `Map<String, Any>` to `JsonObject`
        schemaMapping.put("FunctionParameters", "kotlinx.serialization.json.JsonObject");

        // Configure the template directory
        templateDir = "config";
        supportingFiles.add(new SupportingFile("openai.mustache", "src/commonMain/kotlin/" + apiPackage.replace(".", "/"), "OpenAI.kt"));

        omitGradleWrapper = true;
        serializationLibrary = SERIALIZATION_LIBRARY_TYPE.kotlinx_serialization;

        defaultIncludes.remove("io.ktor.client.request.forms.InputProvider");
        defaultIncludes.add("com.xebia.functional.openai.apis.UploadFile");

        importMapping.remove("InputProvider");
        importMapping.put("UploadFile", "com.xebia.functional.openai.apis.UploadFile");

        // Fixes for DateTime
        dateLibrary = DateLibrary.KOTLINX_DATETIME.value;
        typeMapping.put("date", "kotlinx.datetime.LocalDate");
        typeMapping.put("date-time", "kotlinx.datetime.Instant");
        typeMapping.put("DateTime", "Instant");
        importMapping.put("Instant", "kotlinx.datetime.Instant");

        specialCharReplacements.put("-", "_");
        specialCharReplacements.put(".", "_");
        // TODO PascalCase!?!
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

    @Override
    protected ImmutableMap.Builder<String, Mustache.Lambda> addMustacheLambdas() {
        return super.addMustacheLambdas()
                .put("oneOfName", new OneOfName())
                .put("capitalised", new Capitalised());
    }

    /**
     * Mechanism to do array access in mustache...
     * We need to generate names for the cases of `oneOf`,
     * where we generate a `sealed interface` with the Schema name,
     * and a `data class OneOfName.names(...)` for each of the `oneOf` cases.
     */
    public static class OneOfName implements Mustache.Lambda {
        private List<String> names = List.of(
                "First",
                "Second",
                "Third",
                "Fourth",
                "Fifth",
                "Sixth",
                "Seventh",
                "Eighth",
                "Ninth"
        );

        public void execute(Template.Fragment fragment, Writer writer) throws IOException {
            String text = fragment.execute();
            int index = Integer.parseInt(text);
            writer.write(names.get(index - 1));
        }
    }

    /** Lambda to capitalise the first letter of a string, and lowercase the rest. */
    public static class Capitalised implements Mustache.Lambda {
        public void execute(Template.Fragment fragment, Writer writer) throws IOException {
            String text = fragment.execute();
            writer.write(text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase(Locale.ROOT));
        }
    }
}
