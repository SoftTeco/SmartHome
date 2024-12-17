package com.softteco.template.utils

import org.json.JSONArray
import org.json.JSONObject

data class ClusterBindings(
    val input: List<String>,
    val output: List<String>
)

data class Endpoint(
    val bindings: List<ClusterBinding>?,
    val clusters: ClusterBindings?,
    val configuredReportings: List<ConfiguredReporting>?,
    val scenes: List<Any>?
)

data class ClusterBinding(
    val cluster: String,
    val target: Target?
)

data class Target(
    val endpoint: Int,
    val ieeeAddress: String,
    val type: String
)

data class ConfiguredReporting(
    val attribute: String,
    val cluster: String,
    val minimumReportInterval: Int,
    val maximumReportInterval: Int,
    val reportableChange: Int
)

data class ZigbeeDeviceDefinition(
    val description: String,
    val exposes: List<ExposedFeature>,
    val supportsOta: Boolean,
    val vendor: String,
    val model: String
)

data class ExposedFeature(
    val access: Int,
    val category: String?,
    val description: String,
    val label: String,
    val name: String,
    val property: String,
    val type: String,
    val unit: String?,
    val valueMax: Int?,
    val valueMin: Int?,
    val valueStep: Double?,
    val valueOff: String?,
    val valueOn: String?,
    val values: List<String>?
)

data class ZigbeeDevice(
    val ieeeAddress: String,
    val friendlyName: String,
    val networkAddress: Int,
    val type: ZigbeeDeviceType,
    val disabled: Boolean,
    val endpoints: Map<String, Endpoint>?,
    val manufacturer: String?,
    val modelId: String?,
    val definition: ZigbeeDeviceDefinition?,
    val interviewCompleted: Boolean,
    val interviewing: Boolean,
    val softwareBuildId: String?,
    val powerSource: String?,
    val supported: Boolean
)

enum class ZigbeeDeviceType {
    Coordinator,
    EndDevice,
    Router
}

enum class ZigbeeTopic(val value: String) {
    ZIGBEE_DATA_TOPIC("zigbee2mqtt/"),
    ZIGBEE_DEVICE_TOPIC("zigbee2mqtt/bridge/devices")
}

fun parseZigbeeDevices(json: String): List<ZigbeeDevice> {
    val devices = mutableListOf<ZigbeeDevice>()
    val jsonArray = JSONArray(json)

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val device = ZigbeeDevice(
            ieeeAddress = jsonObject.getString("ieee_address"),
            friendlyName = jsonObject.getString("friendly_name"),
            networkAddress = jsonObject.getInt("network_address"),
            type = ZigbeeDeviceType.valueOf(jsonObject.getString("type")),
            disabled = jsonObject.getBoolean("disabled"),
            endpoints = jsonObject.optJSONObject("endpoints")?.let { parseEndpoints(it) },
            manufacturer = jsonObject.optString("manufacturer", null),
            modelId = jsonObject.optString("model_id", null),
            definition = jsonObject.optJSONObject("definition")?.let { parseDefinition(it) },
            interviewCompleted = jsonObject.getBoolean("interview_completed"),
            interviewing = jsonObject.getBoolean("interviewing"),
            softwareBuildId = jsonObject.optString("software_build_id", null),
            powerSource = jsonObject.optString("power_source", null),
            supported = jsonObject.getBoolean("supported")
        )
        devices.add(device)
    }

    return devices
}

fun parseEndpoints(jsonObject: JSONObject): Map<String, Endpoint> {
    val endpoints = mutableMapOf<String, Endpoint>()
    jsonObject.keys().forEach { key ->
        val endpointObject = jsonObject.getJSONObject(key)
        endpoints[key] = Endpoint(
            bindings = endpointObject.optJSONArray("bindings")?.let { parseBindings(it) },
            clusters = endpointObject.optJSONObject("clusters")?.let { parseClusters(it) },
            configuredReportings = endpointObject.optJSONArray("configured_reportings")?.let { parseReportings(it) },
            scenes = endpointObject.optJSONArray("scenes")?.toList()
        )
    }
    return endpoints
}

fun parseBindings(jsonArray: JSONArray): List<ClusterBinding> {
    return List(jsonArray.length()) { index ->
        val jsonObject = jsonArray.getJSONObject(index)
        ClusterBinding(
            cluster = jsonObject.getString("cluster"),
            target = jsonObject.optJSONObject("target")?.let {
                Target(
                    endpoint = it.getInt("endpoint"),
                    ieeeAddress = it.getString("ieee_address"),
                    type = it.getString("type")
                )
            }
        )
    }
}

fun parseClusters(jsonObject: JSONObject): ClusterBindings {
    return ClusterBindings(
        input = jsonObject.optJSONArray("input")?.toList() ?: emptyList(),
        output = jsonObject.optJSONArray("output")?.toList() ?: emptyList()
    )
}

fun parseReportings(jsonArray: JSONArray): List<ConfiguredReporting> {
    return List(jsonArray.length()) { index ->
        val jsonObject = jsonArray.getJSONObject(index)
        ConfiguredReporting(
            attribute = jsonObject.getString("attribute"),
            cluster = jsonObject.getString("cluster"),
            minimumReportInterval = jsonObject.getInt("minimum_report_interval"),
            maximumReportInterval = jsonObject.getInt("maximum_report_interval"),
            reportableChange = jsonObject.getInt("reportable_change")
        )
    }
}

fun parseDefinition(jsonObject: JSONObject): ZigbeeDeviceDefinition {
    return ZigbeeDeviceDefinition(
        description = jsonObject.getString("description"),
        exposes = jsonObject.optJSONArray("exposes")?.let { parseExposes(it) } ?: emptyList(),
        supportsOta = jsonObject.getBoolean("supports_ota"),
        vendor = jsonObject.getString("vendor"),
        model = jsonObject.getString("model")
    )
}

fun parseExposes(jsonArray: JSONArray): List<ExposedFeature> {
    return List(jsonArray.length()) { index ->
        val jsonObject = jsonArray.getJSONObject(index)
        ExposedFeature(
            access = jsonObject.getInt("access"),
            category = jsonObject.optString("category", null),
            description = jsonObject.getString("description"),
            label = jsonObject.getString("label"),
            name = jsonObject.getString("name"),
            property = jsonObject.getString("property"),
            type = jsonObject.getString("type"),
            unit = jsonObject.optString("unit", null),
            valueMax = jsonObject.optInt("value_max"),
            valueMin = jsonObject.optInt("value_min"),
            valueStep = jsonObject.optDouble("value_step", 0.0),
            valueOff = jsonObject.optString("value_off", null),
            valueOn = jsonObject.optString("value_on", null),
            values = jsonObject.optJSONArray("values")?.toList()
        )
    }
}

fun JSONArray.toList(): List<String> {
    return List(this.length()) { index -> this.getString(index) }
}
