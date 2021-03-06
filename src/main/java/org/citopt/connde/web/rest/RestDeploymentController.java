package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.parameters.Parameter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.adapter.parameters.ParameterTypes;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.componentType.ComponentType;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.ComponentRepository;
import org.citopt.connde.repository.ComponentTypeRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.citopt.connde.web.rest.util.HeaderUtil;
import org.citopt.connde.web.rest.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for deployment related REST requests.
 *
 * @author rafaelkperes, Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestDeploymentController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private ComponentDeploymentWrapper deploymentWrapper;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private ComponentTypeRepository componentTypeRepository;


    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> isRunningActuator(@PathVariable(value = "id") String id) {
        return isRunningComponent(id, actuatorRepository);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> isRunningSensor(@PathVariable(value = "id") String id) {
        return isRunningComponent(id, sensorRepository);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.POST)
    public ResponseEntity<ActionResponse> deployActuator(@PathVariable(value = "id") String id,
                                                         @RequestBody List<ParameterInstance> parameters) {
        return deployComponent(id, actuatorRepository, parameters);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.POST)
    public ResponseEntity<ActionResponse> deploySensor(@PathVariable(value = "id") String id,
                                                       @RequestBody List<ParameterInstance> parameters) {
        return deployComponent(id, sensorRepository, parameters);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ActionResponse> undeployActuator(@PathVariable(value = "id") String id) {
        return undeployComponent(id, actuatorRepository);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ActionResponse> undeploySensor(@PathVariable(value = "id") String id) {
        return undeployComponent(id, sensorRepository);
    }

    private ResponseEntity isRunningComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Check if running
        return deploymentWrapper.isRunningComponent(component);
    }

    private ResponseEntity<ActionResponse> deployComponent(String id, ComponentRepository repository, List<ParameterInstance> parameterInstances) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Do deployment
        return deploymentWrapper.deployComponent(component, parameterInstances);
    }

    private ResponseEntity<ActionResponse> undeployComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Do undeployment
        return deploymentWrapper.undeployComponent(component);
    }

    @RequestMapping(value = "/adapter/parameter-types", method = RequestMethod.GET)
    public ResponseEntity<List<ParameterTypes>> getAllParameterTypes() {
        //Get all enum objects as list
        List<ParameterTypes> parameterList = Arrays.asList(ParameterTypes.values());
        return new ResponseEntity<>(parameterList, HttpStatus.OK);
    }

    @PostMapping("/component-types")
    public ResponseEntity<?> createComponentType(@Valid @RequestBody ComponentType componentType) throws URISyntaxException {
        ComponentType type = componentTypeRepository.findByName(componentType.getName());
        if (type != null && type.getComponent().equals(componentType.getComponent())) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("Component type already exists", componentType.getName()))
                    .body(null);
        } else {
            componentTypeRepository.save(componentType);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(HeaderUtil.createAlert("Component type created successfully", componentType.getName()))
                    .body(componentType);
        }
    }

    @GetMapping("/component-types")
    public ResponseEntity<List<ComponentType>> getAllComponentTypes(Pageable pageable)
            throws URISyntaxException {
        Page<ComponentType> page = componentTypeRepository.findAll(pageable);
        List<ComponentType> componentTypes = page.getContent().stream()
                .collect(Collectors.toList());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/component-types");
        return new ResponseEntity<>(componentTypes, headers, HttpStatus.OK);
    }

    @GetMapping("/component-types/{component}")
    public ResponseEntity<List<ComponentType>> getSpecificComponentTypes(@PathVariable String component, Pageable pageable) {
        component = component.toUpperCase();
        List<ComponentType> componentTypes = componentTypeRepository.findAllByComponent(component, pageable);
        return new ResponseEntity<>(componentTypes, HttpStatus.OK);
    }

    @RequestMapping("/time")
    public ResponseEntity<String> serverDatetime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return new ResponseEntity<>(strDate, HttpStatus.OK);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        return resource;
    }
}
