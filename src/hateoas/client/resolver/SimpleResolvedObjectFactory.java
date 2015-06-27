package hateoas.client.resolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hateoas.client.converter.HateoasMessageConverter;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Resolved object factory that resolves all links eagerly.
 * @author robert
 */
public class SimpleResolvedObjectFactory implements ResolvedObjectFactory {

	private static final LinkDiscoverers DEFAULT_LINK_DISCOVERERS;
	private static LinkDiscoverer linkDiscoverer;

	static {
		LinkDiscoverer discoverer = new HalLinkDiscoverer();
		DEFAULT_LINK_DISCOVERERS = new LinkDiscoverers(OrderAwarePluginRegistry.create(Arrays.asList(discoverer)));
		linkDiscoverer = DEFAULT_LINK_DISCOVERERS.getLinkDiscovererFor(MediaTypes.HAL_JSON);
	}

	private static final RestTemplate restTemplate = new RestTemplate();
	static{
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new HateoasMessageConverter());
		restTemplate.setMessageConverters(messageConverters);
	}
	
	private String getRelForMethod(Method method){
		JsonProperty jsonProperty = method.getAnnotation(JsonProperty.class);
		if(jsonProperty!=null){
			return jsonProperty.value();
		}
		return null;
	}

	@Override
	public <T> T create(String message, T item) {
		Class<?> clazz = item.getClass();
		for(Method method: clazz.getMethods()){
			if(isSettableMethod(method)){
				String rel = getRelForMethod(method);
				if(rel!=null){
					List<Link> links = linkDiscoverer.findLinksWithRel(rel,message);
					if(links.size()==1 && method.getParameterTypes().length == 1){
						Link link = links.get(0);
						new ResolverImpl<T>(link,item,method).resolve();
					}
				}
			}
		}
		return item;
	}

	private boolean isSettableMethod(Method method) {
		JsonIgnore jsonIgnore = method.getAnnotation(JsonIgnore.class);
		if(jsonIgnore != null && jsonIgnore.value()){
			return false;
		}
		return method.getName().startsWith("set");
	}

}
