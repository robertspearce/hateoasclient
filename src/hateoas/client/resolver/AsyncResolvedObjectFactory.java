package hateoas.client.resolver;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.hateoas.Link;


/**
 * For resolving objects. Link resolutions will be submitted asynchronously. 
 * However the resolve method waits until all links have completed before 
 * returning. Links should therefore be resolved by the time the resolve
 * method has returned.
 * @author robert
 */
public class AsyncResolvedObjectFactory extends ResolvedObjectFactoryTemplate implements ResolvedObjectFactory{

	@Override
	public <T> T create(String message, T item) {
		Class<?> clazz = item.getClass();
		ExecutorService es = Executors.newCachedThreadPool();
		for(Method method: clazz.getMethods()){
			if(isSettableMethod(method)){
				String rel = getRelForMethod(method);
				if(rel!=null){
					List<Link> links = linkDiscoverer.findLinksWithRel(rel,message);
					if(links.size()==1 && method.getParameterTypes().length == 1){
						Link link = links.get(0);
						new AsyncResolver<T>(es, link,item,method).resolve();
					}
				}
			}
		}
		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO: throw exception on time out.
		}
		return item;
	}
}
