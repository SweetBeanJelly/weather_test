package sweetbeanjelly.project.testweather.model

import java.io.Serializable

class ModelNextDay : Serializable {
    var nameDate: String? = null
    var descWeather: String? = null
    var tempMax = 0.0
    var tempMin = 0.0
}