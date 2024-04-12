package ai.xef.openai.generator;

import com.google.common.collect.ImmutableMap;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.openapitools.codegen.*;
import org.openapitools.codegen.languages.KotlinClientCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.model.OperationsMap;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static java.util.Map.entry;

@SuppressWarnings("unused")
public class KMMGeneratorConfig extends KotlinClientCodegen {


    private final Map<String, List<String>> nonRequiredFields = new LinkedHashMap<>();

    @Override
    public String getIgnoreFilePathOverride() {
        return ".openapi-generator-ignore";
    }

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
        additionalProperties.put("modelSuffix", "");

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
                        entry("RunStepDetailsToolCallsFunctionObjectFunction", List.of("name", "arguments", "output")),
                        entry("RunStepDetailsToolCallsFunctionObject", List.of("id")),
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

    /**
     * Map<OperationId, StreamedReturnType>
     * Used to generate additional code for operations that support streaming.
     * <p>
     * Extra streaming operation will be generated for OperationId, and the return type will be Flow<StreamedReturnType>.
     */
    private final static Map<String, Pair<String, String>> streamingOps = Map.of(
            "createThreadAndRun", Pair.of("com.xebia.functional.openai", "ServerSentEvent"),
            "createRun", Pair.of("com.xebia.functional.openai", "ServerSentEvent"),
            "submitToolOuputsToRun", Pair.of("com.xebia.functional.openai", "ServerSentEvent"),
            "createChatCompletion", Pair.of("com.xebia.functional.openai.generated.model", "CreateChatCompletionStreamResponse")
    );

    /**
     * Add the `x-streaming` vendor extension to the operations that are streaming,
     * and add `x-streaming-return` of the return type of the operation.
     * <p>
     * This is used in the mustache template to generate additional code for operations that support streaming.
     */
    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        objs.getOperations()
                .getOperation()
                .forEach((op) -> {
                            if (streamingOps.containsKey(op.operationId)) {
                                op.vendorExtensions.put("x-streaming", true);
                                Pair<String, String> returnType = streamingOps.get(op.operationId);
                                Map<String, String> imports = Map.of(
                                        "import", returnType.getKey() + "." + returnType.getValue(),
                                        "classname", returnType.getValue()
                                );
                                if(!objs.getImports().contains(imports)) {
                                    objs.getImports().add(imports);
                                }
                                op.vendorExtensions.put("x-streaming-return", returnType.getValue());
                            }
                        }
                );
        return super.postProcessOperationsWithModels(objs, allModels);
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
    protected ImmutableMap.Builder<String, Mustache.Lambda> addMustacheLambdas() {
        return super.addMustacheLambdas()
                .put("oneOfName", new OneOfName())
                .put("capitalised", new Capitalised())
                .put("unquote", new Unquote())
                .put("jsname", new JsName())
                .put("serializer", new Serializer())
                .put("dropslash", new DropSlash());
    }

    /* Mechanism to do array access in mustache...
     * We need to generate names for the cases of `oneOf`,
     * where we generate a `sealed interface` with the Schema name,
     * and a `data class CaseInnerType(val value: InnerType)` for each of the `oneOf` cases. */
    public static class OneOfName implements Mustache.Lambda {
        public void execute(Template.Fragment fragment, Writer writer) throws IOException {
            serializer(writer, fragment.execute().trim(), 0);
        }

        private void serializer(
                Writer buffer,
                String text,
                int depth
        ) throws IOException {
            if (text.startsWith("kotlin.collections.List<")) {
                String inner = text.substring(24, text.length() - 1);
                serializer(buffer, inner, depth + 1);
            } else if (text.startsWith("kotlin.collections.")) {
                throw new NotImplementedException(text + " collection serialization not supported **yet**");
            } else {
                if (text.startsWith("kotlin.")) {
                    text = text.substring(7);
                }
                buffer.write(text);
                if (depth > 0) {
                    buffer.write("s");
                    depth--;
                }
                while (depth-- > 0) {
                    buffer.write("List");
                }
            }
        }
    }

    /* Lambda to generate the `@JsName` annotation for the `length` property,
     * can be generalised to other properties/names if needed */
    public static class JsName implements Mustache.Lambda {
        public void execute(Template.Fragment fragment, Writer writer) throws IOException {
            String text = fragment.execute();
            if (text.equals("length")) {
                writer.write("@JsName(\"length_type\") length");
            } else {
                writer.write(text);
            }
        }
    }

    /* Lambda to capitalise the first letter of a string, and lowercase the rest */
    public static class Capitalised implements Mustache.Lambda {
        public void execute(Template.Fragment fragment, Writer writer) throws IOException {
            String text = fragment.execute();
            writer.write(text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase(Locale.ROOT));
        }
    }

    /* Drop first `/` from a path */
    public static class DropSlash implements Mustache.Lambda {
        public void execute(Template.Fragment fragment, Writer writer) throws IOException {
            String text = fragment.execute();
            if (text.startsWith("/")) {
                writer.write(text.substring(1));
            } else {
                writer.write(text);
            }
        }
    }

    /* enum with `gpt-` are trimmed by `gpt_`, the raw value needs to trim the surrounding `"` */
    public static class Unquote implements Mustache.Lambda {
        public void execute(Template.Fragment fragment, Writer writer) throws IOException {
            String text = snakeCase(fragment.execute());
            if (text.startsWith("\"") && text.endsWith("\"")) {
                writer.write(text.substring(1, text.length() - 1));
            } else {
                writer.write(text);
            }
        }

        // Poor man lower snakeCase
        private String snakeCase(String text) {
            return text.replace("-", "_").replace(".", "_");
        }
    }

    /* Most advanced lambda, to generate the `serializer()` call for the `kotlinx.serialization` library.
     * This works for ListSerializer, but can work for much more!! */
    public static class Serializer implements Mustache.Lambda {
        public void execute(Template.Fragment fragment, Writer writer) throws IOException {
            serializer(writer, fragment.execute().trim(), 0);
        }

        private void serializer(
                Writer buffer,
                String text,
                int depth
        ) throws IOException {
            if (text.startsWith("kotlin.collections.List<")) {
                String inner = text.substring(24, text.length() - 1);
                buffer.write("ListSerializer(");
                serializer(buffer, inner, depth + 1);
            } else if (text.startsWith("Map<")) {
                throw new NotImplementedException("Map serialization not supported **yet**");
            } else {
                buffer.write(text);
                buffer.write(".serializer()");
                buffer.write(")".repeat(Math.max(0, depth)));
            }
        }
    }
}
