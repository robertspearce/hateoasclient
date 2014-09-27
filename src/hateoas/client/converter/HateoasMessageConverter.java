package hateoas.client.converter;

import hateoas.client.resolver.LinkResolver;
import hateoas.client.resolver.SimpleLinkResolver;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Message converter for extracting a model object from a standard HAL response
 * message.
 * @author robert
 *
 */
public class HateoasMessageConverter extends AbstractHttpMessageConverter<Object> 
	implements GenericHttpMessageConverter<Object>{

	private final LinkResolver linkResolver = new SimpleLinkResolver();

	public HateoasMessageConverter() {
		super(new MediaType("application", "hal+json", Charset.forName("UTF-8")));
	}

	private final static ObjectMapper objectMapper = new ObjectMapper();

	static{
		// Links and embedded elements are not directly present in the object. We therefore
		// want to ignore these in the message.
		// TODO: check whether there is a way to only ignore certain fields (_link and _embed).
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return canRead(clazz, null, mediaType);
	}

	@Override
	public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
		JavaType javaType = getJavaType(type, contextClass);
		return (objectMapper.canDeserialize(javaType) && canRead(mediaType));
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return (objectMapper.canSerialize(clazz) && canWrite(mediaType));
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		// should not be called, since we override canRead/Write instead
		throw new UnsupportedOperationException();
	}

	private Object readJavaType(Class<?> type, String inputMessage) {
		try {
			return objectMapper.readValue(inputMessage, type);
		}
		catch (IOException ex) {
			throw new HttpMessageNotReadableException("Could not read HAL+JSON: " + ex.getMessage(), ex);
		}
	}

	@Override
	protected Object readInternal(Class<?> clazz,
			HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
		String json = convertStreamToString(inputMessage.getBody());
		return readJavaType(clazz, json);
	}

	@Override
	protected void writeInternal(Object object, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		JsonGenerator jsonGenerator =
			objectMapper.getFactory().createGenerator(outputMessage.getBody(),JsonEncoding.UTF8);
		// A workaround for JsonGenerators not applying serialization features
		// https://github.com/FasterXML/jackson-databind/issues/12
		if (objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
			jsonGenerator.useDefaultPrettyPrinter();
		}
		try {
			objectMapper.writeValue(jsonGenerator, object);
		}
		catch (JsonProcessingException ex) {
			throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
		}
	}

	private static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}

	@Override
	public Object read(Type type, Class<?> contextClass,
			HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
		String json = convertStreamToString(inputMessage.getBody());
		Object item = readJavaType((Class<?>)type,json);
		if(linkResolver!=null){
			item = linkResolver.resolve(json,item);
		}
		return item;
	}

	protected JavaType getJavaType(Type type, Class<?> contextClass) {
		return objectMapper.getTypeFactory().constructType(type, contextClass);
	}
}