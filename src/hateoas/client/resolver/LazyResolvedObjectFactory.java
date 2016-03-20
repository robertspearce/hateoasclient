package hateoas.client.resolver;

import java.lang.reflect.Method;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.springframework.hateoas.Link;


/**
 * For resolving objects. Link resolutions will be lazily evaluated.
 * That is any linked method will not be resolved until, or unless
 * the method is called.
 * @author robert
 */
public class LazyResolvedObjectFactory extends ResolvedObjectFactoryTemplate implements ResolvedObjectFactory{

	@Override
	public <T> T create(String message, T item) {
		ClassPool classpool = ClassPool.getDefault();
		try {
			CtClass ctClass = classpool.get(item.getClass().getName());
			CtMethod resolve = CtMethod.make("resolve(){}",ctClass);
			resolve.setBody("{System.out.println(\"resolving...\"}");
			ctClass.addMethod(resolve);
		
			Class<?> clazz = item.getClass();
			for(Method method: clazz.getMethods()){
				if(isSettableMethod(method)){
					String rel = getRelForMethod(method);
					if(rel!=null){
						List<Link> links = linkDiscoverer.findLinksWithRel(rel,message);
						if(links.size()==1 && method.getParameterTypes().length == 1){
							Link link = links.get(0);
							CtMethod m = ctClass.getDeclaredMethod(method.getName());
							Resolver resolver = new ResolverImpl<T>(link,item,method);
							m.insertBefore("{this.resolve();}");
						}
					}
				}
			}
			return (T) ctClass.toClass().newInstance();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Probably want to throw an error here instead.
		return null;
		
	}
}
