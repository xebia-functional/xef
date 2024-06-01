package ai.xef.langchain4j

import ai.xef.Chat
import com.xebia.functional.xef.ClassifierAI
import com.xebia.functional.xef.DefaultAI
import kotlinx.serialization.KSerializer
import kotlin.reflect.KType

class Langchain4JAI<A : Any>(
  target: KType,
  model: Chat,
  serializer: () -> KSerializer<A>,
  enumSerializer: ((case: String) -> A)?
) : DefaultAI<A>(target, model, serializer, enumSerializer)


class Langchain4JAIClassifier<E>(
  target: KType,
  model: Chat,
  serializer: () -> KSerializer<E>,
  enumSerializer: ((case: String) -> E)?
) : DefaultAI<E>(target, model, serializer, enumSerializer)
where E: Enum<E>, E: ClassifierAI.PromptClassifier
