package hateoas.client.resolver;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import org.springframework.hateoas.Link;

/**
 * Method resolver. When calling the resolve method it may return before
 * resolution of the method is complete.
 * @author robert
 *
 */
public class AsyncResolver<T> implements Resolver {
	private final ExecutorService service;
	private final Link link;
	private final Method method;
	private final T instance;
	
	public AsyncResolver(ExecutorService service,Link link, T instance, Method method){
		this.service = service;
		this.link = link;
		this.method = method;
		this.instance = instance;
	}

	/**
	 * When the method returns, it is not guaranteed that the value has been 
	 * resolved, but it will be attempted.
	 * To allow several properties to be resolved simultaneously, without blocking.
	 */
	@Override
	public void resolve() {
		service.submit(new Runnable(){
			@Override
			public void run() {
				new ResolverImpl<T>(link,instance,method).resolve();
			}
		});
	}

}
