/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package project.controller;

import javax.validation.Valid;
import java.util.Collection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project.model.Owner;
import project.model.Pet;
import project.model.PetType;
import project.model.PetValidator;
import project.repository.OwnerRepository;
import project.repository.PetRepository;
import project.repository.PetTypeRepository;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	private final PetRepository petRepository;
    private final PetTypeRepository petTypeRepository;
	private final OwnerRepository ownerRepository;

	public PetController(PetRepository petRepository,
                         OwnerRepository ownerRepository,
                         PetTypeRepository petTypeRepository) {
		this.petRepository = petRepository;
		this.ownerRepository = ownerRepository;
        this.petTypeRepository = petTypeRepository;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		// return this.petRepository.findPetTypes();
        return this.petTypeRepository.findAll();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {
		return this.ownerRepository.findById(ownerId);
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
	}

	@GetMapping("/pets/new")
	public String initCreationForm(Owner owner, ModelMap model) {
		Pet pet = new Pet();
		owner.addPet(pet);
		model.put("pet", pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/new")
	public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
		if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}
		// if (!StringUtils.hasLength(pet.getType())) {
		// 	result.rejectValue("type", "notFound", "error");
		// }
        
        if (result.hasErrors()) {
            logger.error("addPet error");
			model.put("pet", pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}
        
        logger.debug("addPet result.hasErrors()="+(result.hasErrors()?"error":"ok"));
        logger.debug("addPet pet.getId()="+valueOf(pet.getId()));
        logger.debug("addPet pet.getName()="+valueOf(pet.getName()));
        logger.debug("addPet pet.getType()="+valueOf(pet.getType()));
        logger.debug("addPet pet.getType().getId()="+valueOf(pet.getType().getId()));
        logger.debug("addPet pet.getType().getName()="+valueOf(pet.getType().getName()));
        
		owner.addPet(pet);
		if (result.hasErrors()) {
            logger.error("addPet error2");
			model.put("pet", pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}
		else {
			this.petRepository.save(pet);
			return "redirect:/owners/{ownerId}";
		}
	}

	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm(@PathVariable("petId") int petId, ModelMap model) {
		Pet pet = this.petRepository.findById(petId);
		model.put("pet", pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {
		if (result.hasErrors()) {
			pet.setOwner(owner);
			model.put("pet", pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}
		else {
			owner.addPet(pet);
			this.petRepository.save(pet);
			return "redirect:/owners/{ownerId}";
		}
	}
    
    public static String valueOf(Object obj) {
    return (obj == null) ? "null" : obj.toString();
    }

}
