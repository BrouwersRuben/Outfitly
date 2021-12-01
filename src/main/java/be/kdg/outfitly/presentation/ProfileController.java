package be.kdg.outfitly.presentation;

import be.kdg.outfitly.domain.ClothingItem;
import be.kdg.outfitly.domain.User;
import be.kdg.outfitly.presentation.dto.ClothingDTO;
import be.kdg.outfitly.presentation.dto.UserDTO;
import be.kdg.outfitly.presentation.dto.profileChanges.LocationDTO;
import be.kdg.outfitly.presentation.dto.profileChanges.PasswordDTO;
import be.kdg.outfitly.service.ClothingService;
import be.kdg.outfitly.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/profile")
@SessionAttributes("user")
public class ProfileController {
    Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private UserService userService;
    private ClothingService clothingService;

    public ProfileController(UserService userService, ClothingService clothingService) {
        this.userService = userService;
        this.clothingService = clothingService;
    }

    @GetMapping
    public String showProfile(Model model, @ModelAttribute("user") User user) {
        model.addAttribute("loggedIn", user.getId() != -1);
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/changelocation")
    public String changeLocation(Model model, @ModelAttribute("user") User user){
        model.addAttribute("locationDTO", new LocationDTO());
        model.addAttribute("loggedIn", user.getId() != -1);
        model.addAttribute("user", user);
        return "changelocation";
    }

    @PostMapping("/changelocation")
    public String processChangedLocation(@ModelAttribute("user") User user, @Valid @ModelAttribute("locationDTO") LocationDTO locationDTO, BindingResult errors){
        if (errors.hasErrors()){
            errors.getAllErrors().forEach(error -> logger.error(error.toString()));
            return "changelocation";
        } else {
//            logger.debug(user.getFirstName() + " changed their city to " + city);
            user.setCity(locationDTO.getCity());
            user.setCountry(locationDTO.getCountry());
            user.setStreetName(locationDTO.getStreetName());
            user.setStreetNumber(locationDTO.getStreetNumber());
            user.setApartmentNumber(locationDTO.getApartmentNumber());
            user.setZipcode(locationDTO.getZipcode());
            userService.update(user);
            return "redirect:/profile";
        }
    }

    @GetMapping("/changepassword")
    public String changePassword(Model model, @ModelAttribute("user") User user){
        model.addAttribute("passwordDTO", new PasswordDTO());
        model.addAttribute("loggedIn", user.getId() != -1);
        model.addAttribute("user", user);
//        logger.debug(user.getFirstName() + " wants to change their password");
        return "changepassword";
    }

    @PostMapping("/changepassword")
    public String processChangePassword(Model model, @ModelAttribute("user") User user, @Valid @ModelAttribute("passwordDTO") PasswordDTO passwordDTO, BindingResult errors){
        logger.debug("currentPassword: " + user.getPassword());
        if (errors.hasErrors()){
            errors.getAllErrors().forEach(error -> logger.error(error.toString()));
            return "changepassword";
        } else {
            logger.debug("currentPasswordDTO: " + passwordDTO.getCurrentPassword());
            logger.debug("newPasswordDTO: " + passwordDTO.getNewPassword());
            if(passwordDTO.getCurrentPassword().equals(user.getPassword())){
//                logger.debug("User correctly wrote their password");
                user.setPassword(passwordDTO.getCurrentPassword());
                userService.update(user);
                return "redirect:/profile";
            }else{
//                logger.debug("User didn't write their password correctly");
                model.addAttribute("errorMessage", "This password is incorrect");
                return "changepassword";
            }
        }
    }

/*    @PostMapping("/changepassword")
    public String processChangePassword(@ModelAttribute("user") User user, String verifyPassword, String newPassword){
        logger.debug("Verify password: " + verifyPassword + ", normal password: " + user.getPassword());
        logger.debug("New password: " + newPassword);
        if(Objects.equals(verifyPassword, user.getPassword())){
            logger.debug("User correctly wrote their password");
            user.setPassword(newPassword);
            userService.update(user);
            return "redirect:/profile";
        }else{
            logger.debug("User didn't write their password correctly");
            return "changepassword";
        }
    }*/

    @GetMapping("/changename")
    public String changeName(Model model, @ModelAttribute("user") User user){
        logger.debug(user.getFirstName() + " is trying to change their name");
        model.addAttribute("user", user);
        return "changename";
    }

    @PostMapping("/changename")
    public String processChangeName(@ModelAttribute("user") User user, String firstName, String lastName){
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userService.update(user);
        return "redirect:/profile";
    }

    @GetMapping("/changephonenumber")
    public String changePhoneNumber(Model model, @ModelAttribute("user") User user){
        logger.debug(user.getFirstName() + " is trying to change their phonenumber. It's cs currently: " + user.getPhoneNumber());
        model.addAttribute("user", user);
        return "changephonenumber";
    }

    @PostMapping("/changephonenumber")
    public String processChangePhoneNumber(@ModelAttribute("user") User user, String newPhoneNumber){
        user.setPhoneNumber(newPhoneNumber);
        logger.debug("New phone number: " + newPhoneNumber);
        logger.debug("Succesfully changed " + user.getFirstName() + "'s phone number to: " + user.getPhoneNumber());
        userService.update(user);
        return "redirect:/profile";
    }

    @GetMapping("/viewclothing")
    public String viewClothing(Model model, @ModelAttribute("user") User user){
        model.addAttribute("user", user);
        return "viewclothing";
    }

    @PostMapping("/viewclothing")
    public String processRemoveClothing(@ModelAttribute("clothingDTO") ClothingDTO clothingDTO, @ModelAttribute("user") User user){
        logger.warn(clothingDTO.toString());
        clothingService.delete(clothingDTO.getID());
        List<ClothingItem> newClothingList = clothingService.read();
        user.setClothes(newClothingList);
        userService.update(user);
        return "viewclothing";
    }
}
