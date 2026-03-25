package com.unirfp.ceropapeleo.forms.state

sealed interface GenerateFormEvent {
    data class ApplicantNameChanged(val value: String) : GenerateFormEvent
    data class ApplicantFirstSurnameChanged(val value: String) : GenerateFormEvent
    data class ApplicantSecondSurnameChanged(val value: String) : GenerateFormEvent
    data class ApplicantDocumentIdChanged(val value: String) : GenerateFormEvent

    data class ApplicantStreetChanged(val value: String) : GenerateFormEvent
    data class ApplicantNumberChanged(val value: String) : GenerateFormEvent
    data class ApplicantPostalCodeChanged(val value: String) : GenerateFormEvent
    data class ApplicantCityChanged(val value: String) : GenerateFormEvent
    data class ApplicantProvinceChanged(val value: String) : GenerateFormEvent
    data class ApplicantCountryChanged(val value: String) : GenerateFormEvent

    data class ApplicantMobilePhoneChanged(val value: String) : GenerateFormEvent
    data class ApplicantEmailChanged(val value: String) : GenerateFormEvent

    data object SubmitClicked : GenerateFormEvent
}