package com.maxclub.android.photogallery

import com.google.gson.*
import java.lang.reflect.Type

class PhotoResponseDeserializer : JsonDeserializer<PhotoResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse {
        val jsonObject = json.asJsonObject
        val photosJsonElement = jsonObject.get("photos")
        val gson = GsonBuilder().create()
        return gson.fromJson(photosJsonElement, typeOfT)
    }
}