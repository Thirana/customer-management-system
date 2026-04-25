package com.example.customermanagement.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void leavesResponseStatusFromFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/customers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                ((MockHttpServletResponse) response).setStatus(200);
            }
        });

        assertEquals(200, response.getStatus());
    }

    @Test
    void logsAndPreservesErrorStatusWhenChainThrows() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/customers/999");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);

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

        assertEquals(500, response.getStatus());
    }
}
