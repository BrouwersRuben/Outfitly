package be.kdg.outfitly.presentation;

import be.kdg.outfitly.domain.*;
import be.kdg.outfitly.presentation.dto.ClothingDTO;
import be.kdg.outfitly.service.ArduinoSensorService;
import be.kdg.outfitly.service.ClothingService;
import be.kdg.outfitly.service.UserService;
import be.kdg.outfitly.service.WeatherForecastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/user/outfit")
public class OutfitController {

    private OutfitSelector outfitSelector;
    private final Logger logger = LoggerFactory.getLogger(OutfitController.class);
    private WeatherForecastService weatherForecastService;
    private ClothingService clothingService;
    private UserService userService;

    public OutfitController(WeatherForecastService weatherForecastService, ClothingService clothingService) {
        this.weatherForecastService = weatherForecastService;
        this.clothingService = clothingService;
    }

    @GetMapping
    public String occasionSelector(Model model, Principal principal) {
        logger.debug("principal" + principal.toString());
        logger.debug("principal name" + principal.getName());
        User user = userService.findByEmail(principal.getName());
        model.addAttribute("loggedIn", user != null);
        model.addAttribute("user", user);
        model.addAttribute("occasions", ClothingItem.Occasion.values());
        logger.debug(user.getId() + "");
        logger.debug(user.getName() + "");
        return "choose-occasion";
    }


    @PostMapping
    public String occasionForm(ClothingItem.Occasion occasion, Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        logger.debug("User for the outfit: "+user.toString());
        WeatherForecast weatherForecast = weatherForecastService.findByCountryAndCity(user.getCountryCode(), user.getCity());
        logger.debug("Weather forecast from the API: " + weatherForecast);
        ArduinoSensor arduinoSensor = new ArduinoSensor(10, 20, LocalDateTime.now());
        logger.debug("Weather forecast from the Arduino: " + arduinoSensor);
        outfitSelector = new OutfitSelector(weatherForecast, arduinoSensor, user, occasion);
        model.addAttribute("clothes", outfitSelector.getSuitableClothesMap());
        model.addAttribute("types", List.of(ClothingItem.Type.values()));
        model.addAttribute("aiDecision", outfitSelector.getAiDecision());
        logger.warn("Occasion form: " + occasion);
        model.addAttribute("occasion", occasion.getName());
        return "outfit";
    }

    /*
    @PostMapping(params = {"putInWash"})
    public String putInWash(Principal principal, Model model, @ModelAttribute("clothingDTO") ClothingDTO clothingDTO){
        User user = userService.findByEmail(principal.getName());
        ClothingItem toPutInWash = clothingService.findById(clothingDTO.getID());
        clothingService.putInWash(toPutInWash);
        return "redirect:/user/outfit";
    }*/

    @PostMapping(params = {"putInWash"})
    //TODO: Occasion is null
    public String putInWash(Principal principal, Model model, @ModelAttribute("occasion") String occasionName, @ModelAttribute("clothingDTO") ClothingDTO clothingDTO){
        User user = userService.findByEmail(principal.getName());
        logger.debug("Occasion putInWash: " + occasionName);
        ClothingItem toPutInWash = clothingService.findById(clothingDTO.getID());
        clothingService.putInWash(toPutInWash);
        WeatherForecast weatherForecast = weatherForecastService.findByCountryAndCity(user.getCountryCode(), user.getCity());
        ArduinoSensor arduinoSensor = new ArduinoSensor(10, 20, LocalDateTime.now());
        outfitSelector = new OutfitSelector(weatherForecast, arduinoSensor, user, ClothingItem.Occasion.valueOf(occasionName.toUpperCase()));
        model.addAttribute("clothes", outfitSelector.getSuitableClothesMap());
        model.addAttribute("types", List.of(ClothingItem.Type.values()));
        model.addAttribute("aiDecision", outfitSelector.getAiDecision());
        return "outfit";
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
