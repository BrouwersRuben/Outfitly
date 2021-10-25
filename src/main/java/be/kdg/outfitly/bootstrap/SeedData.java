package be.kdg.outfitly.bootstrap;

import be.kdg.outfitly.domain.ArduinoSensor;
import be.kdg.outfitly.domain.ClothingItem;
import be.kdg.outfitly.domain.User;
import be.kdg.outfitly.domain.WeatherForecast;
import be.kdg.outfitly.repository.ArduinoSensorRepository;
import be.kdg.outfitly.repository.UserRepository;
import be.kdg.outfitly.repository.WeatherForecastRepository;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
public class SeedData implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(SeedData.class);
    private final ArduinoSensorRepository arduinoSensorRepository;
    private final UserRepository userRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final String apiString = "https://api.openweathermap.org/data/2.5/weather?q=Antwerp,BE&units=metric&appid=ff81fe37ad2b546130b7cbcb331aa72c";
//    private EntityRepository entityRepository;
//    private ListRepository listRepository;
//    private MainUserListRepository mainUserListRepository;
//    private ClothingItem clothingItem;

    public SeedData(ArduinoSensorRepository arduinoSensorRepository, UserRepository userRepository, WeatherForecastRepository weatherForecastRepository) {
        this.arduinoSensorRepository = arduinoSensorRepository;
        this.userRepository = userRepository;
        this.weatherForecastRepository = weatherForecastRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.debug("Seeding the repositories");

        //Test users with some clothing items
        User user1 = new User("testUser1@gmail.com","test123","John",
                List.of(new ClothingItem("Jacket", ClothingItem.Material.LEATHER, ClothingItem.RainProofness.NORMAL,ClothingItem.Occasion.CASUAL,ClothingItem.Weather.MILD),
                        new ClothingItem("Hoodie", ClothingItem.Material.COTTON, ClothingItem.RainProofness.BAD,ClothingItem.Occasion.CASUAL,ClothingItem.Weather.COLD),
                        new ClothingItem("Jeans", ClothingItem.Material.DENIM, ClothingItem.RainProofness.GOOD,ClothingItem.Occasion.UNIVERSAL,ClothingItem.Weather.UNIVERSAL),
                        new ClothingItem("T-Shirt", ClothingItem.Material.COTTON, ClothingItem.RainProofness.BAD,ClothingItem.Occasion.CASUAL,ClothingItem.Weather.WARM),
                        new ClothingItem("Suit", ClothingItem.Material.WOOL, ClothingItem.RainProofness.BAD,ClothingItem.Occasion.ELEGANT,ClothingItem.Weather.UNIVERSAL)));

        User user2 = new User("testUser2@gmail.com","test123","Bob",
                List.of(new ClothingItem("Jacket", ClothingItem.Material.LEATHER, ClothingItem.RainProofness.NORMAL,ClothingItem.Occasion.CASUAL,ClothingItem.Weather.MILD),
                        new ClothingItem("Hoodie", ClothingItem.Material.SYNTHETIC, ClothingItem.RainProofness.NORMAL,ClothingItem.Occasion.CASUAL,ClothingItem.Weather.COLD),
                        new ClothingItem("Jeans", ClothingItem.Material.DENIM, ClothingItem.RainProofness.GOOD,ClothingItem.Occasion.UNIVERSAL,ClothingItem.Weather.UNIVERSAL),
                        new ClothingItem("T-Shirt", ClothingItem.Material.COTTON, ClothingItem.RainProofness.BAD,ClothingItem.Occasion.CASUAL,ClothingItem.Weather.WARM),
                        new ClothingItem("Suit", ClothingItem.Material.SILK, ClothingItem.RainProofness.BAD,ClothingItem.Occasion.ELEGANT,ClothingItem.Weather.UNIVERSAL)));

        ArduinoSensor arduinoSensor = new ArduinoSensor(10, 50, LocalDateTime.of(2021, 10, 29, 12, 30, 30));

        userRepository.create(user1);
        userRepository.create(user2);

        arduinoSensorRepository.create(arduinoSensor);


        // Weather API


        // TODO: Where to put this ?
        URIBuilder builder = new URIBuilder(apiString);
        HttpGet get = new HttpGet(builder.build());
        CloseableHttpClient httpclient = HttpClients.createDefault();
        JSONObject jsonWeatherForecast;

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            jsonWeatherForecast = null;
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Bad response status code: {}.", response.getStatusLine().getStatusCode());
                return;
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String rawResult = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                jsonWeatherForecast = new JSONObject(rawResult);
            }

            if (jsonWeatherForecast==null) {
                logger.error("No weather forecast data retrieved!");
            } else {
                logger.debug("Forecast data successfully retrieved!");
            }
        }

        WeatherForecast forecast = new WeatherForecast(
                LocalDateTime.of(2021, 10, 29, 12, 30, 30),
                String.valueOf(jsonWeatherForecast.getJSONObject("sys").get("country")),
                String.valueOf(jsonWeatherForecast.get("name")),
                Double.parseDouble(String.valueOf(jsonWeatherForecast.getJSONObject("main").get("temp"))),
                Double.parseDouble(String.valueOf(jsonWeatherForecast.getJSONObject("main").get("feels_like"))),
                Double.parseDouble(String.valueOf(jsonWeatherForecast.getJSONObject("main").get("temp_min"))),
                Double.parseDouble(String.valueOf(jsonWeatherForecast.getJSONObject("main").get("temp_max"))),
                Double.parseDouble(String.valueOf(jsonWeatherForecast.getJSONObject("wind").get("speed"))),
                Integer.parseInt(String.valueOf(jsonWeatherForecast.getJSONObject("main").get("humidity"))),
                String.valueOf(jsonWeatherForecast.getJSONArray("weather").getJSONObject(0).get("main"))
        );

        weatherForecastRepository.create(forecast);

        // Slave and master accounts may be a bit easier to understand
    }
}
