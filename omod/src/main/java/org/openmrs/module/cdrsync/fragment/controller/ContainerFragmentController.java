package org.openmrs.module.cdrsync.fragment.controller;

import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cdrsync.api.CdrContainerService;
import org.openmrs.module.cdrsync.api.impl.CdrContainerServiceImpl;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class ContainerFragmentController {
	
	CdrContainerService containerService = new CdrContainerServiceImpl();
	
	public void controller(FragmentModel model, @SpringBean("userService") UserService service) {
		model.addAttribute("users", service.getAllUsers());
	}
	
	public void getPatients() {
		containerService.getAllPatients();
	}
}
