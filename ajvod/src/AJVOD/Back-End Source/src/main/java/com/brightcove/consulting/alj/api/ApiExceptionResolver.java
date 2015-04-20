package com.brightcove.consulting.alj.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import com.brightcove.consulting.alj.api.errors.ApiException;
import com.brightcove.consulting.alj.api.errors.ConflictException;
import com.brightcove.consulting.alj.api.errors.InvalidSignatureException;
import com.brightcove.consulting.alj.api.errors.MissingJsonPropertyException;
import com.brightcove.consulting.alj.api.errors.NotAuthorizedException;
import com.brightcove.consulting.alj.services.ServiceException;

/**
 * ExceptionResolver specific to handling exceptions under the api.
 *
 * @author ssayles
 */
@SuppressWarnings("unchecked")
@Component
public class ApiExceptionResolver extends AbstractHandlerExceptionResolver {
	
	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionResolver.class);

	// mapping of exception types to their associated status codes or exception handling code
	// This is essentially an adaptation of DefaultHandlerExceptionResolver to better fit
	// the REST API.
	// In general, use specific exception types only
	private Map<Class<? extends Exception>, Object> exceptionMap = new HashMap<Class<? extends Exception>, Object>();


	@Override
	public ModelAndView doResolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {

		response.setContentType("application/json; charset=utf-8");

		JsonError jsonError = resolveDefault(request, response, ex);
		if (jsonError != null) {
			response.setStatus(jsonError.getStatus());
			return jsonError.toModelAndView();
		}

		// for now, just make it a 500 error
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    jsonError = new JsonError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
				"There was an error processing your request.  Please try again.", ex.getMessage());
		if (logger.isDebugEnabled()) {
			logger.debug("An exception occurred processing a request for " 
					+ request.getRequestURI() + ": "+ ExceptionUtils.getStackTrace(ex));
		}
		return jsonError.toModelAndView();
	}
 

	/**
	 * Process any default exception handling.  This is mostly to handle the
	 * underlying framework exceptions.
	 *
	 * @param request
	 * @param response
	 * @param ex
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private JsonError resolveDefault(HttpServletRequest request, HttpServletResponse response, Exception ex) {
		JsonError jsonError = null;
		Set<Class<? extends Exception>> classes = exceptionMap.keySet();
		for (Class<? extends Exception> keyClass : classes) {
			if (keyClass.isAssignableFrom(ex.getClass())) {
				Object object = exceptionMap.get(keyClass);
				if (object instanceof ExceptionClosure) {
					jsonError = ((ExceptionClosure)object).handle(request,  response, ex);
				} else {
					jsonError = new JsonError((Integer) object, ex.getMessage(), null);
				}
				break;
			}
		}

		return jsonError;
	}

	
	@Override
	protected boolean shouldApplyTo(HttpServletRequest request, Object handler) {
		return isApiRequest(request);
	}

	private boolean isApiRequest(HttpServletRequest request) {
		return request.getServletPath().startsWith("/api/");
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	// initialize exception mapping
	{

		// framework 4xx
        exceptionMap.put(TypeMismatchException.class, HttpServletResponse.SC_BAD_REQUEST);
        exceptionMap.put(HttpMessageNotReadableException.class, new BasicExceptionHandler(HttpServletResponse.SC_BAD_REQUEST, "Request was not readable."));
        exceptionMap.put(MethodArgumentNotValidException.class, HttpServletResponse.SC_BAD_REQUEST);
        exceptionMap.put(MissingServletRequestPartException.class, HttpServletResponse.SC_BAD_REQUEST);
        exceptionMap.put(MissingServletRequestParameterException.class, HttpServletResponse.SC_BAD_REQUEST);
        exceptionMap.put(ServletRequestBindingException.class, HttpServletResponse.SC_BAD_REQUEST);
        exceptionMap.put(HttpMediaTypeNotAcceptableException.class, HttpServletResponse.SC_NOT_ACCEPTABLE);
        exceptionMap.put(NoSuchRequestHandlingMethodException.class, HttpServletResponse.SC_NOT_FOUND);
        exceptionMap.put(HttpRequestMethodNotSupportedException.class, new ExceptionClosure<HttpRequestMethodNotSupportedException>() {
            public JsonError handle(HttpServletRequest request, HttpServletResponse response, HttpRequestMethodNotSupportedException ex) {
                String[] supportedMethods = ex.getSupportedMethods();
                String details = null;
                if (supportedMethods != null) {
                    details = StringUtils.arrayToDelimitedString(supportedMethods, ", ");
                    response.setHeader("Allow", details);
                }
                JsonError jse = new JsonError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getMessage(), "Allowed methods: " + details);
                return jse;
            }
        });
        exceptionMap.put(HttpMediaTypeNotSupportedException.class, new ExceptionClosure<HttpMediaTypeNotSupportedException>() {
            public JsonError handle(HttpServletRequest request,
                    HttpServletResponse response,
                    HttpMediaTypeNotSupportedException ex) {
                List<MediaType> mediaTypes = ex.getSupportedMediaTypes();
                String details = null;
                if (!CollectionUtils.isEmpty(mediaTypes)) {
                    response.setHeader("Accept", MediaType.toString(mediaTypes));
                }
                JsonError jse = new JsonError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), "Allowed media types: " + details);
                return jse;
            }
        });
        
        
		// framework 5xx
        exceptionMap.put(ConversionNotSupportedException.class, new BasicExceptionHandler());
		exceptionMap.put(HttpMessageNotWritableException.class, new BasicExceptionHandler());

		
		// api specific exceptions
		ApiExceptionHandler handler = new ApiExceptionHandler();
		// 4xx
		exceptionMap.put(ConflictException.class, handler);
		exceptionMap.put(NotAuthorizedException.class, handler);
		exceptionMap.put(InvalidSignatureException.class, handler);
		exceptionMap.put(MissingJsonPropertyException.class, handler);
		// 5xx
		exceptionMap.put(ServiceException.class, new BasicExceptionHandler());
	}
	




	/**
	 * Simple closure to wrap any logic specific to handling a particular kind of
	 * exception.
	 *
	 * @param <T> The exception class.
	 */
	private interface ExceptionClosure<T extends Exception> {
		public JsonError handle(HttpServletRequest request, HttpServletResponse response, T ex);
	}

	private class BasicExceptionHandler implements ExceptionClosure<Exception> {
		private String message;
		private int status;

		public BasicExceptionHandler() {
			this(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		public BasicExceptionHandler(int status) {
			this(status, "An error occurred while processing your request.");
		}
		public BasicExceptionHandler(int status, String message) {
			this.status = status;
			this.message = message;
		}

		public JsonError handle(HttpServletRequest request,
				HttpServletResponse response, Exception ex) {
			JsonError jse = new JsonError(status,
					message,
					ex.getMessage());
			return jse;
		}
	}

	private class ApiExceptionHandler implements ExceptionClosure<ApiException> {
        @Override
        public JsonError handle(HttpServletRequest request,
                HttpServletResponse response, ApiException ex) {
            JsonError jse = new JsonError(ex.getStatus(), ex.getMessage(), ex.getDetails());

            Map<String, String> attributes = ex.getAttributes();
            jse.setAttributes(attributes);

            return jse;
        }
	    
	}
}
