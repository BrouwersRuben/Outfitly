package be.kdg.outfitly.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OutfitSelector {
    private WeatherForecast weatherForecast;
    private User user;
    private ClothingItem.Occasion occasion;

    public OutfitSelector(WeatherForecast weatherForecast, User user, ClothingItem.Occasion occasion) {
        this.weatherForecast = weatherForecast;
        this.user = user;
        this.occasion = occasion;
    }

    private Logger logger = LoggerFactory.getLogger(OutfitSelector.class);


    public List<ClothingItem> getPossibleClothingItems() {

        List<ClothingItem> possibleItems = user.getClothes();
        double lowestTemperature = weatherForecast.getLowestTemperature();
        boolean isGoingToRain = weatherForecast.isGoingToRain();


        possibleItems = removeUnsuitableForTemperature(possibleItems, lowestTemperature);
        possibleItems = removeUnsuitableForRain(possibleItems, isGoingToRain);
        possibleItems = removeUnsuitableForOccasion(possibleItems, occasion);

        return possibleItems;
    }


    public List<ClothingItem> removeUnsuitableForRain(List<ClothingItem> clothes, boolean isGoingToRain) {
        if (isGoingToRain) {
            clothes = clothes
                    .stream()
                    .filter(item -> item.getRainProofness() != ClothingItem.RainProofness.BAD)
                    .collect(Collectors.toList());
        }
        return clothes;
    }

    public List<ClothingItem> removeUnsuitableForTemperature(List<ClothingItem> clothes, double temperature) {

        ClothingItem.Weather givenWeather;


        if (temperature < 5) {
            givenWeather = ClothingItem.Weather.COLD;
        } else if (temperature < 15) {
            givenWeather = ClothingItem.Weather.MILD;
        } else {
            givenWeather = ClothingItem.Weather.WARM;
        }

        return clothes
                .stream()
                .filter(item -> item.getWeather() == givenWeather || item.getWeather() == ClothingItem.Weather.UNIVERSAL)
                .collect(Collectors.toList());
    }

    public List<ClothingItem> removeUnsuitableForOccasion(List<ClothingItem> clothes, ClothingItem.Occasion occasion) {

        return clothes
                .stream()
                .filter(item -> item.getOccasion() == occasion || item.getOccasion() == ClothingItem.Occasion.UNIVERSAL)
                .collect(Collectors.toList());

    }

    public Map<ClothingItem.Type, List<ClothingItem>> divideClothesIntoTypes(List<ClothingItem> clothes) {

        List<ClothingItem.Type> types = new ArrayList<>(List.of(ClothingItem.Type.values()));

        Map<ClothingItem.Type, List<ClothingItem>> clothesByType = new HashMap<>();

        types.stream()
                .forEach(type -> {
                    List<ClothingItem> clothesOfType = clothes
                            .stream()
                            .filter(item -> item.getType() == type)
                            .collect(Collectors.toList());

                    clothesByType.put(type, clothesOfType);
                });

        return clothesByType;
    }

    public Map<ClothingItem.Type, List<ClothingItem>> getSuitableClothesMap() {
        Map<ClothingItem.Type, List<ClothingItem>> suitableClothes = divideClothesIntoTypes(getPossibleClothingItems());
        logger.debug(suitableClothes.toString());
        return suitableClothes;
    }

}