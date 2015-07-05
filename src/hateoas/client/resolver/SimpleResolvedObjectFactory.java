package hateoas.client.resolver;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.hateoas.Link;

/**
 * Resolved object factory that resolves all links eagerly.
 * @author robert
 */
public class SimpleResolvedObjectFactory extends ResolvedObjectFactoryTemplate  implements ResolvedObjectFactory {

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

}
