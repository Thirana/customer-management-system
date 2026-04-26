import { INPUT_CLASSES, MOBILE_NUMBER_PATTERN } from './customerFormConfig.js'

export function createEmptyForm() {
  return {
    name: '',
    dateOfBirth: '',
    nicNumber: '',
    mobileNumbers: [''],
    addresses: [createEmptyAddress()],
    familyMemberIds: [],
  }
}

export function createEmptyAddress() {
  return {
    addressLine1: '',
    addressLine2: '',
    countryId: '',
    cityId: '',
  }
}

export function createFormFromCustomer(customer) {
  return {
    name: customer.name ?? '',
    dateOfBirth: customer.dateOfBirth ?? '',
    nicNumber: customer.nicNumber ?? '',
    mobileNumbers: customer.mobileNumbers?.length ? customer.mobileNumbers : [''],
    addresses: customer.addresses?.length
      ? customer.addresses.map((address) => ({
          addressLine1: address.addressLine1 ?? '',
          addressLine2: address.addressLine2 ?? '',
          countryId: address.countryId ? String(address.countryId) : '',
          cityId: address.cityId ? String(address.cityId) : '',
        }))
      : [createEmptyAddress()],
    familyMemberIds: customer.familyMembers?.map((member) => member.id) ?? [],
  }
}

export function validateCustomerForm(form) {
  const errors = {}

  if (!form.name.trim()) {
    errors.name = 'Name is required.'
  }

  if (!form.nicNumber.trim()) {
    errors.nicNumber = 'NIC number is required.'
  }

  if (!form.dateOfBirth) {
    errors.dateOfBirth = 'Date of birth is required.'
  }

  form.addresses.forEach((address, index) => {
    if (hasAddressContent(address) && !address.countryId) {
      errors[`addresses[${index}].countryId`] = 'Country is required when an address row is used.'
    }

    if (hasAddressContent(address) && !address.cityId) {
      errors[`addresses[${index}].cityId`] = 'City is required when an address row is used.'
    }
  })

  form.mobileNumbers.forEach((mobileNumber, index) => {
    const trimmedNumber = mobileNumber.trim()
    if (!trimmedNumber) {
      return
    }

    if (!MOBILE_NUMBER_PATTERN.test(trimmedNumber)) {
      errors[`mobileNumbers[${index}]`] = 'Mobile number must contain exactly 10 digits.'
    }
  })

  return errors
}

export function toCustomerPayload(form) {
  return {
    name: form.name.trim(),
    dateOfBirth: form.dateOfBirth,
    nicNumber: form.nicNumber.trim(),
    mobileNumbers: form.mobileNumbers
      .map((value) => value.trim())
      .filter(Boolean),
    addresses: form.addresses
      .map((address) => ({
        addressLine1: address.addressLine1.trim(),
        addressLine2: address.addressLine2.trim(),
        cityId: address.cityId ? Number(address.cityId) : null,
      }))
      .filter((address) => address.addressLine1 || address.addressLine2 || address.cityId),
    familyMemberIds: Array.from(new Set(form.familyMemberIds)),
  }
}

export function hasAddressContent(address) {
  return Boolean(
    address.addressLine1.trim() || address.addressLine2.trim() || address.countryId || address.cityId,
  )
}

export function inputClasses(hasError) {
  return `${INPUT_CLASSES} ${hasError ? 'border-[rgba(139,59,43,0.45)]' : ''}`
}

export function toSelectedFamilyMembers(customer) {
  return customer.familyMembers?.map((member) => ({
    id: member.id,
    name: member.name,
    nicNumber: member.nicNumber,
  })) ?? []
}
