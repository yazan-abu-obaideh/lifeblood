package org.otherband.lifeblood.validations;

import org.otherband.lifeblood.UserException;
import org.otherband.lifeblood.generated.model.AlertCreationRequest;
import org.otherband.lifeblood.generated.model.VolunteerRegistrationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Placeholder, will use Jakarta validations for open api generated objects later
 */
public enum SimpleValidator {
    INSTANCE;

    public void validate(Object object) {
        ArrayList<String> violationMessages = new ArrayList<>();
        if (object instanceof VolunteerRegistrationRequest volunteerRequest) {
            validate(volunteerRequest, violationMessages);
        }
        if (object instanceof AlertCreationRequest request) {
            validate(request, violationMessages);
        }
        if (!violationMessages.isEmpty()) throw new UserException(String.join(",", violationMessages));
    }

    private void validate(AlertCreationRequest request, ArrayList<String> violationMessages) {
        if (Objects.isNull(request.getAlertLevel())) {
            violationMessages.add("Alert level must be set");
        }
        if (isBlank(request.getHospitalUuid())) {
            violationMessages.add("Hospital uuid must be provided");
        }
    }

    private static void validate(VolunteerRegistrationRequest volunteerRequest, ArrayList<String> violationMessages) {
        if (isBlank(volunteerRequest.getPhoneNumber())) {
            violationMessages.add("please enter your phone number");
        }
        List<String> selectedHospitals = volunteerRequest.getSelectedHospitals();
        if (Objects.isNull(selectedHospitals) || selectedHospitals.isEmpty()) {
            violationMessages.add("please choose at least one hospital of interest");
        }
    }
}
