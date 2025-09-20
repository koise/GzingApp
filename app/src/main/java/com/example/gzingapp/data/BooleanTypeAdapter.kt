package com.example.gzingapp.data

import com.google.gson.*
import java.lang.reflect.Type

class BooleanTypeAdapter : JsonDeserializer<Boolean> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Boolean {
        return when {
            json.isJsonPrimitive -> {
                val primitive = json.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isNumber -> primitive.asInt != 0
                    primitive.isString -> primitive.asString.toBoolean()
                    else -> false
                }
            }
            json.isJsonNull -> false
            else -> false
        }
    }
}


