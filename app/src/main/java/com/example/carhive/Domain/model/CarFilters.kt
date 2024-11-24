data class CarFilters(
    val selectedBrands: MutableSet<String> = mutableSetOf(),
    val selectedModel: String? = null,
    val yearRange: Pair<Int, Int>? = null,
    val priceRange: Pair<Int, Int?> = 0 to null,
    val mileageRange: Pair<Int, Int?> = 0 to null,
    val selectedColors: MutableSet<String> = mutableSetOf(),
    val selectedTransmission: String? = null,
    val selectedFuelType: String? = null,
    val engineCapacityRange: Pair<Double?, Double?> = null to null,
    val selectedCondition: String? = null,
    val selectedLocation: String? = null
)
