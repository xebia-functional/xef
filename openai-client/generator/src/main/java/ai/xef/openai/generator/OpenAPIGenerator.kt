@file:JvmName("OpenAPIGenerator")
package ai.xef.openai.generator

import com.google.common.collect.ImmutableMap
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template
import org.apache.commons.lang3.NotImplementedException
import org.openapitools.codegen.CodegenConstants
import org.openapitools.codegen.CodegenOperation
import org.openapitools.codegen.CodegenProperty
import org.openapitools.codegen.SupportingFile
import org.openapitools.codegen.languages.KotlinClientCodegen
import org.openapitools.codegen.model.ModelMap
import org.openapitools.codegen.model.ModelsMap
import org.openapitools.codegen.model.OperationsMap
import java.io.Writer
import java.util.Locale

@Suppress("unused")
class OpenAPIGenerator : KotlinClientCodegen() {
  /* Map<OperationId></OperationId>, StreamedReturnType>
   * Used to generate additional code for operations that support streaming.
   *
   * Extra streaming operation will be generated for OperationId,
   * and the return type will be Flow<StreamedReturnType>.</StreamedReturnType> */
  private val streamingOps: Map<String, Pair<String, String>> = mapOf(
    "createThreadAndRun" to Pair("com.xebia.functional.openai", "AssistantEvent"),
    "createRun" to Pair("com.xebia.functional.openai", "AssistantEvent"),
    "createChatCompletion" to Pair("com.xebia.functional.openai.generated.model", "CreateChatCompletionStreamResponse"),
  )

  private val nonRequiredFields: Map<String, List<String>> = mapOf(
    "ListAssistantFilesResponse" to listOf("firstId", "lastId"),
    "ListAssistantsResponse" to listOf("firstId", "lastId"),
    "ListMessageFilesResponse" to listOf("firstId", "lastId"),
    "ListMessagesResponse" to listOf("firstId", "lastId"),
    "ListRunsResponse" to listOf("firstId", "lastId"),
    "ListRunStepsResponse" to listOf("firstId", "lastId"),
    "ListThreadsResponse" to listOf("firstId", "lastId"),
    "MessageObject" to listOf("metadata"),
    "MessageObjectContentInner" to listOf("imageFile", "text"),
    "RunObject" to listOf("expiresAt", "requiredAction"),
    "RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner" to listOf("logs", "image"),
    "RunStepDetailsToolCallsFunctionObjectFunction" to listOf("output"),
    "RunStepDetailsToolCallsObjectToolCallsInner" to listOf("codeInterpreter", "retrieval", "function"),
    "RunStepDetailsToolCallsRetrievalObject" to listOf("retrieval"),
    "RunStepObject" to listOf("expiredAt", "metadata"),
    "MessageContentTextObjectTextAnnotationsInner" to listOf("filePath", "fileCitation"),
  )

  init {
    // Generate in src/commonMain/kotlin, not /src/main/kotlin
    additionalProperties["sourceFolder"] = "src/commonMain/kotlin"
    additionalProperties["generateModelTests"] = false
    additionalProperties["generateApiTests"] = false
    additionalProperties["generateInfrastructure"] = false
    setModelPackage("com.xebia.functional.openai.generated.model")
    setApiPackage("com.xebia.functional.openai.generated.api")
    additionalProperties["apiSuffix"] = ""
    additionalProperties["modelSuffix"] = ""

    // Configure OpenAI `object` to be mapped to `JsonObject`
    typeMapping["object"] = "JsonObject"
    importMapping["JsonObject"] = "kotlinx.serialization.json.JsonObject"

    typeMapping["java.net.URI"] = "kotlin.String"

    typeMapping["java.math.BigDecimal"] = "kotlin.Double"
    importMapping["BigDecimal"] = "kotlin.Double"

    typeMapping["java.io.File"] = "UploadFile"
    importMapping["java.io.File"] = "com.xebia.functional.openai.UploadFile"

    // Maps `Map<String, Any>` to `JsonObject`
    schemaMapping["FunctionParameters"] = "kotlinx.serialization.json.JsonObject"

    // Configure the template directory
    templateDir = "config"
    supportingFiles.add(
      SupportingFile(
        "openai.mustache",
        "src/commonMain/kotlin/" + apiPackage.replace(".", "/"),
        "OpenAI.kt"
      )
    )

    omitGradleWrapper = true
    serializationLibrary = SERIALIZATION_LIBRARY_TYPE.kotlinx_serialization

    defaultIncludes.remove("io.ktor.client.request.forms.InputProvider")
    defaultIncludes.add("com.xebia.functional.openai.apis.UploadFile")

    importMapping.remove("InputProvider")
    importMapping["UploadFile"] = "com.xebia.functional.openai.apis.UploadFile"

    // Fixes for DateTime
    dateLibrary = DateLibrary.KOTLINX_DATETIME.value
    typeMapping["date"] = "kotlinx.datetime.LocalDate"
    typeMapping["date-time"] = "kotlinx.datetime.Instant"
    typeMapping["DateTime"] = "Instant"
    importMapping["Instant"] = "kotlinx.datetime.Instant"

    specialCharReplacements["-"] = "_"
    specialCharReplacements["."] = "_"
    enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.snake_case
  }

  private fun readEnumModel(all: List<CodegenProperty>): CodegenProperty? =
    if (all.size == 2) {
      val (first, second) = all
      if (first.isString && second.isEnum) second else null
    } else null

  /**
   * Add the `x-streaming` vendor extension to the operations that are streaming,
   * and add `x-streaming-return` of the return type of the operation.
   *
   * This is used in the mustache template to generate additional code for operations that support streaming.
   */
  override fun postProcessOperationsWithModels(objs: OperationsMap, allModels: List<ModelMap>): OperationsMap {
    objs.operations.operation.forEach { op: CodegenOperation ->
      if (streamingOps.containsKey(op.operationId)) {
        op.vendorExtensions["x-streaming"] = true
        val (import, type) = streamingOps[op.operationId]!!
        objs.imports.add(mapOf("import" to "$import.$type", "classname" to type))
        op.vendorExtensions["x-streaming-return"] = type
      }
    }
    return super.postProcessOperationsWithModels(objs, allModels)
  }

  override fun postProcessModels(objs: ModelsMap): ModelsMap {
    for (mo in objs.models) {
      val cm = mo.model
      if (cm.anyOf != null && cm.anyOf.isNotEmpty()) {
        val codegenProperty = readEnumModel(cm.composedSchemas.anyOf)
        codegenProperty?.let { enumProp: CodegenProperty ->
          cm.modelJson = enumProp.jsonSchema
          cm.interfaces = null
          cm.anyOf = HashSet()
          cm.dataType = enumProp.dataType
          cm.isString = enumProp.isString
          cm.allowableValues = enumProp.allowableValues
          cm.isEnum = enumProp.isEnum
          cm.isAnyType = enumProp.isAnyType
          cm.composedSchemas = null
        }
      } else if (nonRequiredFields.containsKey(cm.classname)) {
        val fields = nonRequiredFields.getOrDefault(cm.classname, emptyList())
        cm.allVars
          .filter { p -> fields.contains(p.name) }
          .forEach { p -> p.setRequired(false) }
      }
    }
    return super.postProcessModels(objs)
  }

  override fun addMustacheLambdas(): ImmutableMap.Builder<String, Mustache.Lambda> =
    super.addMustacheLambdas()
      .put("oneOfName", OneOfName())
      .put("capitalised", Capitalised())
      .put("unquote", Unquote())
      .put("jsname", JsName())
      .put("serializer", Serializer())
      .put("dropslash", DropSlash())

  /* Mechanism to do array access in mustache...
   * We need to generate names for the cases of `oneOf`,
   * where we generate a `sealed interface` with the Schema name,
   * and a `data class CaseInnerType(val value: InnerType)` for each of the `oneOf` cases. */
  class OneOfName : Mustache.Lambda {
    override fun execute(fragment: Template.Fragment, writer: Writer) =
      serializer(writer, fragment.execute().trim { it <= ' ' }, 0)

    private fun serializer(
      buffer: Writer,
      text: String,
      depth: Int
    ) {
      if (text.startsWith("kotlin.collections.List<")) {
        val inner: String = text.substring(24, text.length - 1)
        serializer(buffer, inner, depth + 1)
      } else if (text.startsWith("kotlin.collections.")) {
        throw NotImplementedException("$text collection serialization not supported **yet**")
      } else {
        buffer.write(if (text.startsWith("kotlin.")) text.substring(7) else text)
        if (depth > 0) buffer.write("s") // Append `s` for plural
        if (depth - 1 > 0) buffer.write("List".repeat(depth - 1)) // Add List for per nested List
      }
    }
  }

  /* Lambda to generate the `@JsName` annotation for the `length` property,
     * can be generalised to other properties/names if needed */
  class JsName : Mustache.Lambda {
    override fun execute(fragment: Template.Fragment, writer: Writer) {
      val text = fragment.execute()
      if (text == "length") writer.write("@JsName(\"length_type\") length")
      else writer.write(text)
    }
  }

  /* Lambda to capitalise the first letter of a string, and lowercase the rest */
  class Capitalised : Mustache.Lambda {
    override fun execute(fragment: Template.Fragment, writer: Writer) {
      val text = fragment.execute()
      writer.write(text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1).lowercase())
    }
  }

  /* Drop first `/` from a path */
  class DropSlash : Mustache.Lambda {
    override fun execute(fragment: Template.Fragment, writer: Writer) {
      val text = fragment.execute()
      if (text.startsWith("/")) writer.write(text.substring(1))
      else writer.write(text)
    }
  }

  /* enum with `gpt-` are trimmed by `gpt_`, the raw value needs to trim the surrounding `"` */
  class Unquote : Mustache.Lambda {
    override fun execute(fragment: Template.Fragment, writer: Writer) {
      val text = fragment.execute().snakeCase()
      if (text.startsWith("\"") && text.endsWith("\"")) {
        writer.write(text.substring(1, text.length - 1))
      } else {
        writer.write(text)
      }
    }

    private fun String.snakeCase(): String =
      replace("-", "_").replace(".", "_")
  }

  /* Most advanced lambda, to generate the `serializer()` call for the `kotlinx.serialization` library.
     * This works for ListSerializer, but can work for much more!! */
  class Serializer : Mustache.Lambda {
    override fun execute(fragment: Template.Fragment, writer: Writer) =
      serializer(writer, fragment.execute().trim(), 0)

    private fun serializer(
      buffer: Writer,
      text: String,
      depth: Int
    ) {
      if (text.startsWith("kotlin.collections.List<")) {
        val inner = text.substring(24, text.length - 1)
        buffer.write("ListSerializer(")
        serializer(buffer, inner, depth + 1)
      } else if (text.startsWith("kotlin.collections.")) {
        throw NotImplementedException("Map serialization not supported **yet**. Please report an issue on GitHub.")
      } else {
        buffer.write(text)
        buffer.write(".serializer()")
        buffer.write(")".repeat(depth))
      }
    }
  }
}