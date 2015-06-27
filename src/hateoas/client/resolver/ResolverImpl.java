package hateoas.client.resolver;

import hateoas.client.converter.HateoasMessageConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class ResolverImpl<T> implements Resolver {
	
	private Link link;
	private Method method;
	private T instance;
	
	private static final RestTemplate restTemplate = new RestTemplate();
	static{
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new HateoasMessageConverter());
		restTemplate.setMessageConverters(messageConverters);
	}
	
	/**
	 * Create a simple resolver for a link.
	 * @param link location of resource to resolve the link.
	 * @param method set method to update the resolved value. This should be the setter
	 * message. It is assumed have only one parameter.
	 */
	public ResolverImpl(Link link, T instance, Method method) {
		assert(method.getParameterTypes().length == 1);
		this.link = link;
		this.method = method;		
		this.instance = instance;
	}

	@Override
	public void resolve() {
		Class<?> parameterType = method.getParameterTypes()[0];
		Object parameter = restTemplate.getForObject(link.getHref(),parameterType);
		try {
			method.invoke(instance,parameter);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
