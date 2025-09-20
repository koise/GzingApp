package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter
import com.google.gson.*
import java.lang.reflect.Type

data class UserProfile(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String?,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("notes")
    val notes: String?,
    
    @SerializedName("last_login")
    val lastLogin: String?,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("sos_contacts_count")
    val sosContactsCount: Int
)

data class SosContact(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("relationship")
    val relationship: String,
    
    @SerializedName("is_primary")
    @JsonAdapter(BooleanDeserializer::class)
    val isPrimary: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)

data class UpdateProfileRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("first_name")
    val firstName: String? = null,
    
    @SerializedName("last_name")
    val lastName: String? = null,
    
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("action")
    val action: String? = null
)

data class AddSosContactRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("relationship")
    val relationship: String,
    
    @SerializedName("is_primary")
    val isPrimary: Boolean = false,
    
    @SerializedName("action")
    val action: String? = null
)

data class SosContactsResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<SosContact>
)

data class SosSmsRequest(
    @SerializedName("phone_numbers")
    val phoneNumbers: List<String>,
    
    @SerializedName("message")
    val message: String
)

data class SosSmsResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: SosSmsData? = null
)

data class SosSmsData(
    @SerializedName("phone_numbers")
    val phoneNumbers: List<String>,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("count")
    val count: Int,
    
    @SerializedName("status")
    val status: String
)

// Custom deserializer to handle numeric boolean values from API
class BooleanDeserializer : JsonDeserializer<Boolean> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Boolean {
        return when (json) {
            is JsonPrimitive -> {
                when {
                    json.isBoolean -> json.asBoolean
                    json.isNumber -> json.asInt != 0
                    json.isString -> json.asString.toBoolean()
                    else -> false
                }
            }
            else -> false
        }
    }
}

