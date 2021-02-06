package sweetbeanjelly.project.testweather.network

object ApiEndpoint {
    var BASEURL = "http://api.openweathermap.org/data/2.5/"
    var CurrentWeather = "weather?"
    var ListWeather = "forecast?"
    var Daily = "onecall?"
    var UnitsAppid = "&units=metric&appid=52188afeb68a2f01c9368295983d512e"
    var UnitsAppidDaily = "&exclude=current,minutely,hourly,alerts&units=metric&appid=52188afeb68a2f01c9368295983d512e"
}