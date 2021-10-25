package be.kdg.outfitly.service;

import be.kdg.outfitly.domain.ArduinoSensor;
import be.kdg.outfitly.domain.WeatherForecast;
import be.kdg.outfitly.repository.WeatherForecastRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WeatherForecastServiceImpl implements WeatherForecastService {
    private final Logger logger = LoggerFactory.getLogger(WeatherForecastServiceImpl.class);
    private final WeatherForecastRepository weatherForecastRepository;

    @Autowired
    public WeatherForecastServiceImpl(WeatherForecastRepository weatherForecastRepository) {
        this.weatherForecastRepository = weatherForecastRepository;
    }

    @Override
    public WeatherForecast findByDate(LocalDateTime time) {
        return weatherForecastRepository.read().stream().filter(weatherData -> weatherData.getTimeOfReading().equals(time)).findFirst().get();
    }
}
