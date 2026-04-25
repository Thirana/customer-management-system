package com.example.customermanagement.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.customermanagement.constant.ApiConstants;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void preservesIncomingRequestId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(ApiConstants.REQUEST_ID_HEADER, "request-123");

        filter.doFilter(request, response, new AssertingFilterChain("request-123"));

        assertEquals("request-123", response.getHeader(ApiConstants.REQUEST_ID_HEADER));
        assertNull(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY));
    }

    @Test
    void generatesRequestIdWhenMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                assertNotNull(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY));
                assertFalse(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY).trim().isEmpty());
            }
        });

        assertNotNull(response.getHeader(ApiConstants.REQUEST_ID_HEADER));
        assertFalse(response.getHeader(ApiConstants.REQUEST_ID_HEADER).trim().isEmpty());
        assertNull(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY));
    }

    @Test
    void clearsMdcWhenChainThrows() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            filter.doFilter(request, response, new FilterChain() {
                @Override
                public void doFilter(ServletRequest request, ServletResponse response) throws ServletException {
                    throw new ServletException("Expected test exception");
                }
            });
        } catch (Exception ignored) {
            // Expected test exception.
        }

        assertNull(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY));
    }

    private static class AssertingFilterChain implements FilterChain {

        private final String expectedRequestId;

        private AssertingFilterChain(String expectedRequestId) {
            this.expectedRequestId = expectedRequestId;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            assertEquals(expectedRequestId, MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY));
        }
    }
}
