package local.wifiadmin.observability;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void returnsProvidedRequestIdInResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wifi-parameter/CPE_001");
        request.addHeader(RequestLoggingFilter.REQUEST_ID_HEADER, "request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER)).isEqualTo("request-123");
    }

    @Test
    void generatesRequestIdWhenHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wifi-parameter/CPE_001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER)).isNotBlank();
    }
}
