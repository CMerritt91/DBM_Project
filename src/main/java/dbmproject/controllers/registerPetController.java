package dbmproject.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dbmproject.models.Pet;

@Controller
public class registerPetController {
    
    @GetMapping("/petRegistration")
    public String showForm(Model model) {
        model.addAttribute("pet", new Pet());
        return "petRegistration";
    }

    

}
