package com.example.safetrack.model

import com.google.gson.annotations.SerializedName

data class OrsRouteResponse(
    @SerializedName("type") val type: String?,
    @SerializedName("features") val features: List<Feature>?,
    @SerializedName("bbox") val bbox: List<Double>?
    // You might add other fields like "metadata" if needed
)

/**
 * Represents a single route Feature within the FeatureCollection.
 */
data class Feature(
    @SerializedName("type") val type: String?,
    @SerializedName("properties") val properties: Properties?,
    @SerializedName("geometry") val geometry: Geometry?,
    @SerializedName("bbox") val bbox: List<Double>?
)

/**
 * Contains properties of the route Feature, including the summary.
 */
data class Properties(
    // Add other properties if needed (segments, way_points, etc.)
    @SerializedName("summary") val summary: Summary?
)

/**
 * Contains summary information like distance and duration for the route.
 * Note: ORS provides distance in meters and duration in seconds.
 */
data class Summary(
    @SerializedName("distance") val distance: Double?, // Distance in meters
    @SerializedName("duration") val duration: Double?  // Duration in seconds
)

/**
 * Represents the geometry of the route (typically a LineString).
 * Coordinates are usually [longitude, latitude].
 */
data class Geometry(
    @SerializedName("type") val type: String?, // e.g., "LineString"
    @SerializedName("coordinates") val coordinates: List<List<Double>>? // List of [lon, lat] pairs
)

// Note: Keep your RouteRequestBody if you plan to use POST requests later,
// but it's not needed for the GET request shown below.
// data class RouteRequestBody(
//     val coordinates: List<List<Double>>
// )

