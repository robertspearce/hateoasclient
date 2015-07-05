package hateoas.client.resolver;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.plugin.core.OrderAwarePluginRegistry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ResolvedObjectFactoryTemplate {

	protected static final LinkDiscoverers DEFAULT_LINK_DISCOVERERS;
	protected static LinkDiscoverer linkDiscoverer;
	
	static {
		LinkDiscoverer discoverer = new HalLinkDiscoverer();
		DEFAULT_LINK_DISCOVERERS = new LinkDiscoverers(OrderAwarePluginRegistry.create(Arrays.asList(discoverer)));
		linkDiscoverer = DEFAULT_LINK_DISCOVERERS.getLinkDiscovererFor(MediaTypes.HAL_JSON);
	}

	public ResolvedObjectFactoryTemplate() {
		super();
	}

	protected String getRelForMethod(Method method) {
		JsonProperty jsonProperty = method.getAnnotation(JsonProperty.class);
		if(jsonProperty!=null){
			return jsonProperty.value();
		}
		return null;
	}

	protected boolean isSettableMethod(Method method) {
		JsonIgnore jsonIgnore = method.getAnnotation(JsonIgnore.class);
		if(jsonIgnore != null && jsonIgnore.value()){
			return false;
		}
		return method.getName().startsWith("set");
	}

}