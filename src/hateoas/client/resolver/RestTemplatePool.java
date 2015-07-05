package hateoas.client.resolver;

import hateoas.client.converter.HateoasMessageConverter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class RestTemplatePool {

	private static final RestTemplate restTemplate = new RestTemplate();
	static{
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new HateoasMessageConverter());
		restTemplate.setMessageConverters(messageConverters);
	}
	
	/**
	 * Get a rest template.
	 * @return a rest template suitable for parsing hateoas messages.
	 */
	public static RestTemplate get(){
		return restTemplate;
	}
	
}
