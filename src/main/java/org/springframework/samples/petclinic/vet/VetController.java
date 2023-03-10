/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vet;

import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
public class VetController {

	private final String VETS_FORM="/vets/createOrUpdateVetForm";

	private final VetService vetService;

	@Autowired
	public VetController(VetService clinicService) {
		this.vetService = clinicService;
	}

	@GetMapping(value = { "/vets" })
	public String showVetList(Map<String, Object> model) {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects
		// so it is simpler for Object-Xml mapping
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetService.findVets());
		model.put("vets", vets);
		return "vets/vetList";
	}

	@GetMapping(value = { "/vets.xml"})
	public @ResponseBody Vets showResourcesVetList() {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects
		// so it is simpler for JSon/Object mapping
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetService.findVets());
		return vets;
	}

	@Transactional(readOnly = true)
    @GetMapping("/vets/{id}/edit")
    public ModelAndView editVet(@PathVariable int id){
        Vet vet= vetService.getById(id).get();
        ModelAndView result=new ModelAndView(VETS_FORM);
        result.addObject("vet", vet);
		result.addObject("specialties", vetService.findSpecialties());
        return result;
    }

    @Transactional
    @PostMapping("/vets/{id}/edit")
    public ModelAndView saveVet(@PathVariable int id,@Valid Vet vet, BindingResult br,
				@RequestParam(value = "specialties", defaultValue = "") Set<Specialty> specialties){
        ModelAndView result=null;
        if(br.hasErrors()){
            result=new ModelAndView(VETS_FORM,br.getModel());
            result.addObject("specialties", vetService.findSpecialties());                   
            return result;
        }
        Vet vetToBeUpdated=vetService.getById(id).get();
        BeanUtils.copyProperties(vet,vetToBeUpdated,"id");
		vetToBeUpdated.setSpecialtiesInternal(specialties);
        vetService.save(vetToBeUpdated);
		result = new ModelAndView("redirect:/vets");
        return result;
    }

    @Transactional(readOnly = true)
    @GetMapping("/vets/new")
    public ModelAndView createVet(){
        Vet vet=new Vet();
        ModelAndView result=new ModelAndView(VETS_FORM);
        result.addObject("vet", vet);
        result.addObject("specialties", vetService.findSpecialties());
        return result;
    }

    @Transactional
    @PostMapping("/vets/new")
    public ModelAndView saveNewVet(@Valid Vet vet, BindingResult br,
					@RequestParam(value = "specialties", defaultValue = "") Set<Specialty> specialties){
        ModelAndView result=null;
        if(br.hasErrors()){
            result=new ModelAndView(VETS_FORM,br.getModel());
            result=new ModelAndView(VETS_FORM,br.getModel());  
            result.addObject("specialties", vetService.findSpecialties());                  
            return result;
        }
		vet.setSpecialtiesInternal(specialties);
        vetService.save(vet);
        result = new ModelAndView("redirect:/vets");
        return result;
    }

    @GetMapping(value = "/vets/{vetId}/delete")
    public String deleteOwner(@PathVariable("vetId") int vetId, ModelAndView model) {
        vetService.deleteVet(vetId);
        return "redirect:/vets" ;
    }

}
