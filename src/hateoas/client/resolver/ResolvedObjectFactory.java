package hateoas.client.resolver;

/**
 * Resolve links. There are 2 different ways of doing this:
 * <ol>
 * <li>Resolve the links after the object has been parsed. Used by simple
 * link resolvers</li>
 * <li>Prime the class to parse it's own links. Used by eager link resolvers</li>
 * </ol>
 * @author robert
 *
 */
public interface ResolvedObjectFactory {
	public <T> T create(String message, T item);
}
