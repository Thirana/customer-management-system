import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { Alert } from "../components/ui/Alert.jsx";
import { CustomerListControls } from "../components/customer-list/CustomerListControls.jsx";
import { CustomerListHero } from "../components/customer-list/CustomerListHero.jsx";
import { CustomerListTable } from "../components/customer-list/CustomerListTable.jsx";
import {
  DEFAULT_PAGE,
  PAGE_SIZE_OPTIONS,
  SORTABLE_COLUMNS,
} from "../components/customer-list/customerListConfig.js";
import { EmptyState } from "../components/ui/EmptyState.jsx";
import { LoadingState } from "../components/ui/LoadingState.jsx";
import {
  createEmptyPageState,
  parsePageSize,
  parsePositiveInteger,
  parseSortDirection,
  parseSortField,
} from "../components/customer-list/customerListUtils.js";
import {
  deleteCustomer,
  getCustomers,
} from "../features/customers/customer-api.js";

export function CustomerListPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [pageState, setPageState] = useState(createEmptyPageState());
  const [status, setStatus] = useState("loading");
  const [errorMessage, setErrorMessage] = useState("");
  const [pendingDeleteId, setPendingDeleteId] = useState(null);

  const queryState = useMemo(
    () => ({
      page: parsePositiveInteger(searchParams.get("page"), DEFAULT_PAGE),
      size: parsePageSize(searchParams.get("size")),
      sortBy: parseSortField(searchParams.get("sortBy")),
      sortDir: parseSortDirection(searchParams.get("sortDir")),
    }),
    [searchParams],
  );
  const flash = location.state?.flash ?? null;

  useEffect(() => {
    // Consume one-time success state after redirects so refreshes do not replay stale alerts.
    if (!flash) {
      return;
    }

    navigate(`${location.pathname}${location.search}`, {
      replace: true,
      state: null,
    });
  }, [flash, location.pathname, location.search, navigate]);

  useEffect(() => {
    let cancelled = false;

    // Keep list loading tied to the URL-backed query state so navigation and reloads stay consistent.
    async function loadCustomers() {
      setStatus("loading");
      setErrorMessage("");

      try {
        const result = await getCustomers(queryState);
        if (cancelled) {
          return;
        }

        setPageState(result);
        setStatus(result.content.length === 0 ? "empty" : "success");
      } catch (error) {
        if (cancelled) {
          return;
        }

        setPageState(createEmptyPageState());
        setStatus("error");
        setErrorMessage(error.message || "Customers could not be loaded.");
      }
    }

    loadCustomers();

    return () => {
      cancelled = true;
    };
  }, [queryState]);

  async function handleDelete(customerId, customerName) {
    const confirmed = window.confirm(
      `Delete customer "${customerName}"? This removes the customer and related contact records.`,
    );

    if (!confirmed) {
      return;
    }

    setPendingDeleteId(customerId);
    setErrorMessage("");

    try {
      await deleteCustomer(customerId);

      const nextPage =
        pageState.content.length === 1 && queryState.page > 0
          ? queryState.page - 1
          : queryState.page;

      updateQueryState({ page: nextPage });
    } catch (error) {
      setStatus(pageState.content.length === 0 ? "empty" : "success");
      setErrorMessage(error.message || "Customer could not be deleted.");
    } finally {
      setPendingDeleteId(null);
    }
  }

  function handlePageSizeChange(nextSize) {
    updateQueryState({
      page: DEFAULT_PAGE,
      size: Number(nextSize),
    });
  }

  function handleSortChange(field) {
    if (!SORTABLE_COLUMNS[field]) {
      return;
    }

    const nextSortDir =
      queryState.sortBy === field && queryState.sortDir === "asc"
        ? "desc"
        : "asc";

    updateQueryState({
      page: DEFAULT_PAGE,
      sortBy: field,
      sortDir: nextSortDir,
    });
  }

  function handleSortFieldChange(nextValue) {
    const nextField = parseSortField(nextValue);

    updateQueryState({
      page: DEFAULT_PAGE,
      sortBy: nextField,
    });
  }

  function handleSortDirectionChange(nextValue) {
    const nextDirection = parseSortDirection(nextValue);

    updateQueryState({
      page: DEFAULT_PAGE,
      sortDir: nextDirection,
    });
  }

  function updateQueryState(partialState) {
    // The list contract stays bookmarkable by writing paging and sorting changes back into the URL.
    const nextState = {
      ...queryState,
      ...partialState,
    };

    setSearchParams({
      page: String(nextState.page),
      size: String(nextState.size),
      sortBy: nextState.sortBy,
      sortDir: nextState.sortDir,
    });
  }

  return (
    <div className="space-y-6">
      <CustomerListHero
        pageState={pageState}
        queryState={queryState}
        sortableColumns={SORTABLE_COLUMNS}
      />

      {flash ? (
        <Alert
          title={flash.title}
          message={flash.message}
          tone={flash.tone ?? "success"}
        />
      ) : null}

      <CustomerListControls
        onPageSizeChange={handlePageSizeChange}
        onSortDirectionChange={handleSortDirectionChange}
        onSortFieldChange={handleSortFieldChange}
        pageSizeOptions={PAGE_SIZE_OPTIONS}
        queryState={queryState}
        sortableColumns={SORTABLE_COLUMNS}
      />

      {errorMessage ? (
        <Alert title="Request failed" message={errorMessage} tone="error" />
      ) : null}

      {status === "loading" ? (
        <LoadingState label="Loading customers..." />
      ) : null}

      {status === "empty" ? (
        <EmptyState
          title="No customers found"
          message="Create a customer or import a workbook to start building your customer list."
        />
      ) : null}

      {status === "success" ? (
        <CustomerListTable
          onDelete={handleDelete}
          onNextPage={() =>
            updateQueryState({
              page: Math.min(
                queryState.page + 1,
                Math.max(pageState.totalPages - 1, 0),
              ),
            })
          }
          onPreviousPage={() =>
            updateQueryState({ page: Math.max(queryState.page - 1, 0) })
          }
          onSortChange={handleSortChange}
          pageState={pageState}
          pendingDeleteId={pendingDeleteId}
          queryState={queryState}
        />
      ) : null}
    </div>
  );
}
