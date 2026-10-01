package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AnimeRelation
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {
            // fallback for comma-separated if legacy
            return data.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        return list
    }

    @TypeConverter
    fun fromRelationList(list: List<AnimeRelation>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        list.forEach { rel ->
            val obj = JSONObject()
            obj.put("relationType", rel.relationType)
            obj.put("malId", rel.malId)
            obj.put("title", rel.title)
            obj.put("type", rel.type)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toRelationList(data: String?): List<AnimeRelation> {
        if (data.isNullOrBlank()) return emptyList()
        val list = mutableListOf<AnimeRelation>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AnimeRelation(
                        relationType = obj.optString("relationType", "Related"),
                        malId = obj.optInt("malId", 0),
                        title = obj.optString("title", ""),
                        type = obj.optString("type", "Anime")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
