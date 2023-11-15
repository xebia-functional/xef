package ai.xef.openai.generator;

import org.openapitools.codegen.languages.KotlinClientCodegen;

public class KMMGeneratorConfig extends KotlinClientCodegen {

    public KMMGeneratorConfig() {
        super();
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
