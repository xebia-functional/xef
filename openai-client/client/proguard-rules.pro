-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }

# project specific.
-keep,includedescriptorclasses class com.xebia.functional.openai.generated.model.**$$serializer { *; }
-keepclassmembers class com.xebia.functional.openai.generated.model.** { *** Companion; }
-keepclasseswithmembers class com.xebia.functional.openai.generated.model.** { kotlinx.serialization.KSerializer serializer(...); }
