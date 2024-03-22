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
        setModelPackage("com.xebia.functional.openai.generated.model");
        setApiPackage("com.xebia.functional.openai.generated.api");
        setApiNameSuffix("");

        // Generate in src/commonMain/kotlin, not /src/main/kotlin
        additionalProperties.put("sourceFolder", "src/commonMain/kotlin");
        additionalProperties.put("generateModelTests", false);
        additionalProperties.put("generateApiTests", false);
        additionalProperties.put("generateInfrastructure", false);

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
//                cm
//                        .allVars
//                        .stream()
//                        .filter(p -> p.name.equalsIgnoreCase("model"))
//                        .findFirst()
//                        .filter(p -> !p.dataType.equals("kotlin.String"))
//                        .ifPresent(p -> p.dataType = String.format("ai.xef.openai.OpenAIModel<%s>", p.dataType));
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
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        super.postProcessOperationsWithModels(objs, allModels);
        OperationMap operations = objs.getOperations();
        if (operations != null) {
            List<CodegenOperation> ops = operations.getOperation();
            for (CodegenOperation operation : ops) {
                // modify the data type of binary form parameters to a more friendly type for ktor builds
                if ((JVM_KTOR.equals(getLibrary()) || MULTIPLATFORM.equals(getLibrary())) && operation.allParams != null) {
                    for (CodegenParameter param : operation.allParams) {
                        if (param.dataFormat != null && param.dataFormat.equals("binary")) {
                            param.baseType = param.dataType = "com.xebia.functional.openai.apis.UploadFile";
                        }
                    }
                }
            }
        }
        return objs;
    }

    private final Pattern enumArrayPattern = Pattern.compile("arrayListOf\\((.+)\\)");

    public String toDefaultValue(CodegenProperty codegenProperty, Schema schema) {
        String defaultValue = super.toDefaultValue(codegenProperty, schema);
        if (defaultValue != null && codegenProperty.isEnum) {
            Matcher matcher = enumArrayPattern.matcher(defaultValue);
            if (matcher.find()) {
                /*
                 * Replace the default value of an enum property from `arrayListOf(defaultEnumValue)` to `defaultEnumValue.asListOfOne()`
                 * This is because the `api.mustache` template prefixes the default value with the Enum type:
                 *   - `EnumType.arrayListOf(defaultEnumValue)` -> `EnumType.defaultEnumValue.asListOfOne()`
                 */
                return matcher.replaceFirst("$1.asListOfOne()");
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    protected ImmutableMap.Builder<String, Mustache.Lambda> addMustacheLambdas() {
        // TODO rename lambda {{#lamda.oneOfName}}
        return super.addMustacheLambdas()
                .put("uniqueName", new OneOfName())
                .put("test2", new Test2())
                .put("capitalised", new Capitalised());
    }

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

    // TODO rename to something someting inject " to the start -or end of a edgecase string
    // Should actually use enum for these cases??
    public static class Test2 implements Mustache.Lambda {

        public void execute(Template.Fragment fragment, Writer writer) throws IOException {
            String text = fragment.execute();
            try {
                Double.parseDouble(text);
            } catch (NumberFormatException e) {
                if (text.equals("url") || text.equals("json") || text.equals("1024x1024")) {
                    writer.write("\"" );
                }
            }
        }
    }

    public static class Capitalised implements Mustache.Lambda {
        public void execute(Template.Fragment fragment, Writer writer) throws IOException {
            String text = fragment.execute();
            writer.write(text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase(Locale.ROOT));
        }
    }

}
