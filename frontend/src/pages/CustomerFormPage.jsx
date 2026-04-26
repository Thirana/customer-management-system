import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { CustomerAddressesSection } from "../components/customer-form/CustomerAddressesSection.jsx";
import { CustomerFamilySection } from "../components/customer-form/CustomerFamilySection.jsx";
import { CustomerFormHeader } from "../components/customer-form/CustomerFormHeader.jsx";
import { CustomerMobileSection } from "../components/customer-form/CustomerMobileSection.jsx";
import { CustomerProfileSection } from "../components/customer-form/CustomerProfileSection.jsx";
import {
  FAMILY_MEMBER_SEARCH_DEBOUNCE_MS,
  FAMILY_MEMBER_SEARCH_MIN_LENGTH,
  FAMILY_MEMBER_SEARCH_PAGE_SIZE,
} from "../components/customer-form/customerFormConfig.js";
import {
  createEmptyAddress,
  createEmptyForm,
  createFormFromCustomer,
  toSelectedFamilyMembers,
  validateCustomerForm,
  toCustomerPayload,
} from "../components/customer-form/customerFormUtils.js";
import { Alert } from "../components/ui/Alert.jsx";
import { Button } from "../components/ui/Button.jsx";
import { LoadingState } from "../components/ui/LoadingState.jsx";
import { PageSection } from "../components/ui/PageSection.jsx";
import {
  createCustomer,
  getCities,
  getCustomer,
  searchCustomerSummaries,
  updateCustomer,
} from "../features/customers/customer-api.js";

export function CustomerFormPage({ mode }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = mode === "edit";

  const [bootStatus, setBootStatus] = useState("loading");
  const [cities, setCities] = useState([]);
  const [form, setForm] = useState(createEmptyForm());
  const [familySearchQuery, setFamilySearchQuery] = useState("");
  const [familySearchResults, setFamilySearchResults] = useState([]);
  const [familySearchStatus, setFamilySearchStatus] = useState("idle");
  const [selectedFamilyMembers, setSelectedFamilyMembers] = useState([]);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [validationErrors, setValidationErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const cityOptions = useMemo(
    () =>
      cities.map((city) => ({
        value: String(city.id),
        countryId: String(city.countryId),
        label: `${city.name}, ${city.countryName}`,
      })),
    [cities],
  );

  const countryOptions = useMemo(() => {
    const seenCountryIds = new Set();

    return cities.reduce((options, city) => {
      const countryId = String(city.countryId);
      if (seenCountryIds.has(countryId)) {
        return options;
      }

      seenCountryIds.add(countryId);
      options.push({
        value: countryId,
        label: city.countryName,
      });
      return options;
    }, []);
  }, [cities]);

  const trimmedFamilySearchQuery = familySearchQuery.trim();

  useEffect(() => {
    let cancelled = false;

    // Load all dependent form data together so the create and edit flows share one bootstrap path.
    async function loadFormData() {
      setBootStatus("loading");
      setErrorMessage("");

      try {
        const [cityResponse, customerResponse] = await Promise.all([
          getCities(),
          isEdit ? getCustomer(id) : Promise.resolve(null),
        ]);

        if (cancelled) {
          return;
        }

        setCities(cityResponse);
        setSelectedFamilyMembers(
          customerResponse ? toSelectedFamilyMembers(customerResponse) : [],
        );
        setForm(
          customerResponse
            ? createFormFromCustomer(customerResponse)
            : createEmptyForm(),
        );
        setFamilySearchQuery("");
        setFamilySearchResults([]);
        setFamilySearchStatus("idle");
        setBootStatus("ready");
      } catch (error) {
        if (cancelled) {
          return;
        }

        setBootStatus("error");
        setErrorMessage(
          error.message || "The customer form could not be prepared.",
        );
      }
    }

    loadFormData();

    return () => {
      cancelled = true;
    };
  }, [id, isEdit]);

  useEffect(() => {
    let cancelled = false;

    if (bootStatus !== "ready") {
      return undefined;
    }

    if (trimmedFamilySearchQuery.length < FAMILY_MEMBER_SEARCH_MIN_LENGTH) {
      return undefined;
    }

    const timeoutId = window.setTimeout(async () => {
      try {
        const result = await searchCustomerSummaries(trimmedFamilySearchQuery, {
          size: FAMILY_MEMBER_SEARCH_PAGE_SIZE,
        });

        if (cancelled) {
          return;
        }

        const selectedIds = new Set(form.familyMemberIds);
        const currentCustomerId = isEdit ? Number(id) : null;
        const nextResults = result.content.filter((customer) => {
          if (customer.id === currentCustomerId) {
            return false;
          }

          return !selectedIds.has(customer.id);
        });

        setFamilySearchResults(nextResults);
        setFamilySearchStatus("success");
      } catch {
        if (cancelled) {
          return;
        }

        setFamilySearchResults([]);
        setFamilySearchStatus("error");
      }
    }, FAMILY_MEMBER_SEARCH_DEBOUNCE_MS);

    return () => {
      cancelled = true;
      window.clearTimeout(timeoutId);
    };
  }, [bootStatus, form.familyMemberIds, id, isEdit, trimmedFamilySearchQuery]);

  async function handleSubmit(event) {
    event.preventDefault();
    const clientValidationErrors = validateCustomerForm(form);

    if (Object.keys(clientValidationErrors).length > 0) {
      setValidationErrors(clientValidationErrors);
      setErrorMessage("Please correct the highlighted fields before saving.");
      return;
    }

    const payload = toCustomerPayload(form);
    setIsSubmitting(true);
    setErrorMessage("");
    setSuccessMessage("");
    setValidationErrors({});

    try {
      // Keep create and edit on one route component so validation and payload shaping stay in one place.
      if (isEdit) {
        await updateCustomer(id, payload);
        navigate("/customers", {
          state: {
            flash: {
              title: "Customer updated",
              message: "The customer changes were saved successfully.",
              tone: "success",
            },
          },
        });
        return;
      }

      await createCustomer(payload);
      setForm(createEmptyForm());
      setSelectedFamilyMembers([]);
      setFamilySearchQuery("");
      setFamilySearchResults([]);
      setFamilySearchStatus("idle");
      setSuccessMessage("Successfully created.");
    } catch (error) {
      setErrorMessage(error.message || "The customer could not be saved.");
      setValidationErrors(error.validationErrors ?? {});
    } finally {
      setIsSubmitting(false);
    }
  }

  function updateField(field, value) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
    clearErrors();
  }

  function updateMobileNumber(index, value) {
    setForm((current) => ({
      ...current,
      mobileNumbers: current.mobileNumbers.map((number, numberIndex) =>
        numberIndex === index ? value : number,
      ),
    }));
    clearErrors();
  }

  function addMobileNumber() {
    setForm((current) => ({
      ...current,
      mobileNumbers: [...current.mobileNumbers, ""],
    }));
    clearErrors();
  }

  function removeMobileNumber(index) {
    setForm((current) => {
      const nextNumbers = current.mobileNumbers.filter(
        (_, numberIndex) => numberIndex !== index,
      );
      return {
        ...current,
        mobileNumbers: nextNumbers.length > 0 ? nextNumbers : [""],
      };
    });
    clearErrors();
  }

  function updateAddress(index, field, value) {
    setForm((current) => ({
      ...current,
      addresses: current.addresses.map((address, addressIndex) =>
        addressIndex === index
          ? field === "countryId"
            ? {
                ...address,
                countryId: value,
                cityId: "",
              }
            : {
                ...address,
                [field]: value,
              }
          : address,
      ),
    }));
    clearErrors();
  }

  function addAddress() {
    setForm((current) => ({
      ...current,
      addresses: [...current.addresses, createEmptyAddress()],
    }));
    clearErrors();
  }

  function removeAddress(index) {
    setForm((current) => {
      const nextAddresses = current.addresses.filter(
        (_, addressIndex) => addressIndex !== index,
      );
      return {
        ...current,
        addresses:
          nextAddresses.length > 0 ? nextAddresses : [createEmptyAddress()],
      };
    });
    clearErrors();
  }

  function handleFamilySearchChange(value) {
    setFamilySearchQuery(value);
    if (value.trim().length < FAMILY_MEMBER_SEARCH_MIN_LENGTH) {
      setFamilySearchResults([]);
      setFamilySearchStatus("idle");
    } else {
      setFamilySearchStatus("loading");
    }
  }

  function handleSelectFamilyMember(customer) {
    setSelectedFamilyMembers((current) => {
      if (current.some((member) => member.id === customer.id)) {
        return current;
      }

      return [...current, customer];
    });
    setForm((current) => ({
      ...current,
      familyMemberIds: current.familyMemberIds.includes(customer.id)
        ? current.familyMemberIds
        : [...current.familyMemberIds, customer.id],
    }));
    setFamilySearchResults((current) =>
      current.filter((candidate) => candidate.id !== customer.id),
    );
    clearErrors();
  }

  function handleRemoveFamilyMember(familyMemberId) {
    setSelectedFamilyMembers((current) =>
      current.filter((member) => member.id !== familyMemberId),
    );
    setForm((current) => ({
      ...current,
      familyMemberIds: current.familyMemberIds.filter(
        (idValue) => idValue !== familyMemberId,
      ),
    }));
    clearErrors();
  }

  function clearErrors() {
    // Clear stale feedback as soon as the user edits the form so errors reflect current input only.
    if (errorMessage) {
      setErrorMessage("");
    }
    if (successMessage) {
      setSuccessMessage("");
    }
    if (Object.keys(validationErrors).length > 0) {
      setValidationErrors({});
    }
  }

  if (bootStatus === "loading") {
    return (
      <PageSection
        description="Loading city options, family-member choices, and any existing customer data required for the form."
        eyebrow={isEdit ? "Edit customer" : "Create customer"}
        title={
          isEdit
            ? "Preparing customer edit form"
            : "Preparing customer create form"
        }
      >
        <LoadingState label="Preparing form workspace..." />
      </PageSection>
    );
  }

  if (bootStatus === "error") {
    return (
      <div className="space-y-6">
        <Alert
          title="Form setup failed"
          message={errorMessage || "The customer form could not be prepared."}
          tone="error"
        />

        <PageSection
          actions={
            <Button as={Link} tone="secondary" to="/customers">
              Return to customers
            </Button>
          }
          description="The form route is available, but the required master data or customer data could not be loaded."
          eyebrow="Customer form"
          title="Unable to prepare form"
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <CustomerFormHeader
        cityCount={cities.length}
        isEdit={isEdit}
        isSubmitting={isSubmitting}
        mobileNumberCount={
          form.mobileNumbers.filter((value) => value.trim()).length
        }
        selectedFamilyCount={selectedFamilyMembers.length}
      />

      {errorMessage ? (
        <Alert title="Save failed" message={errorMessage} tone="error" />
      ) : null}

      <form className="space-y-6" id="customer-form" onSubmit={handleSubmit}>
        <CustomerProfileSection
          form={form}
          updateField={updateField}
          validationErrors={validationErrors}
        />

        <CustomerMobileSection
          addMobileNumber={addMobileNumber}
          mobileNumbers={form.mobileNumbers}
          removeMobileNumber={removeMobileNumber}
          updateMobileNumber={updateMobileNumber}
          validationErrors={validationErrors}
        />

        <CustomerAddressesSection
          addAddress={addAddress}
          addresses={form.addresses}
          cityOptions={cityOptions}
          countryOptions={countryOptions}
          removeAddress={removeAddress}
          updateAddress={updateAddress}
          validationErrors={validationErrors}
        />

        <CustomerFamilySection
          familySearchQuery={familySearchQuery}
          onFamilySearchChange={handleFamilySearchChange}
          onRemoveFamilyMember={handleRemoveFamilyMember}
          onSelectFamilyMember={handleSelectFamilyMember}
          searchResults={familySearchResults}
          searchStatus={familySearchStatus}
          selectedFamilyMembers={selectedFamilyMembers}
          validationError={validationErrors.familyMemberIds}
        />

        <div className="flex items-center gap-3">
          <Button disabled={isSubmitting} type="submit">
            {isSubmitting
              ? isEdit
                ? "Updating customer..."
                : "Creating customer..."
              : isEdit
                ? "Update customer"
                : "Create customer"}
          </Button>
          {!isEdit && successMessage ? (
            <span className="text-sm font-medium text-[var(--color-success)]">
              {successMessage}
            </span>
          ) : null}
        </div>
      </form>
    </div>
  );
}
