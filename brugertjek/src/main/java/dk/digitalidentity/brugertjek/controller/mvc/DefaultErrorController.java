package dk.digitalidentity.brugertjek.controller.mvc;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import dk.digitalidentity.brugertjek.security.NoRoleRequired;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@NoRoleRequired
public class DefaultErrorController implements ErrorController {
	private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

	@RequestMapping(value = "/error", produces = "text/html")
	public String errorPage(Model model, HttpServletRequest request, HttpServletResponse response) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

		// get SAML exceptions
		Object authException = request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		if (authException == null && request.getSession() != null) {
			authException = request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
		
		if (authException instanceof Throwable) {
			exception = authException;
		}

		if (status != null && !String.valueOf(status).equals("404")) {
			request.getSession().invalidate();
			response.addHeader("Set-Cookie", "JSESSIONID=; Path=/; HttpOnly; Max-Age=0;");
		}

		// If there was a proper exception, we provide the trace
		String exceptionMessage = (exception != null) ? ((Throwable) exception).getMessage() : null;

		if (StringUtils.hasText(exceptionMessage)) {
			if (exception instanceof Saml2AuthenticationException) {
				log.warn("Exception message from controller layer: " + exceptionMessage);
			}
			else {
				log.warn("Exception message from controller layer: " + exceptionMessage, exception);
			}
		}

		model.addAttribute("status", String.valueOf(status));
		model.addAttribute("timestamp", LocalDateTime.now());
		model.addAttribute("location", request.getRequestURI());
		model.addAttribute("exception", exceptionMessage);

		return "error";
	}

	@RequestMapping(value = "/error", produces = "application/json")
	public ResponseEntity<Map<String, Object>> errorJSON(HttpServletRequest request) {
		Map<String, Object> body = getErrorAttributes(new ServletWebRequest(request));

		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		try {
			// when not logged in, they get a SAML error when attempting to call the AJAX methods, which results in this endpoint being hit
			status = HttpStatus.valueOf((int) body.get("status"));
		}
		catch (Exception ex) {
			log.warn("Exception occurred while getting status from request: " + ex.getMessage());
		}

		return new ResponseEntity<>(body, status);
	}

	private Map<String, Object> getErrorAttributes(WebRequest request) {
		return errorAttributes.getErrorAttributes(request, ErrorAttributeOptions.defaults());
	}
}
