hateoasclient
=============

Client for consuming hateoas using JSON.

Use:
====

Designed for use with Spring core and Spring Hateoas.

To use.

The interface to the library consists of the HateousMessageConverter. This is designed as a direct replacement for the Spring MessageConverter, to be used in a spring RestTemplate.

The difference between the HateoasMessageConverter and a standard message converter is that the former is Haetoas aware. That is it is designed to access services using the Hateoas standard. Using this message converter, it should be possible, once the web service has been accessed at it's entry point for the client to behave as though the web service were not there.

Set up a model for the JSON web service that is being called with the links inlined.

Set up a Spring restTemplate in the calling code:

    private final static RestTemplate restTemplate = new RestTemplate();
    static{
    	List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
    	messageConverters.add(new HateoasMessageConverter());
    	restTemplate.setMessageConverters(messageConverters);	
    }


Call the restTemplate passing the url, and the response type:

    Foo foo = 
    	restTemplate.getForObject("http://localhost:8080/path/to/resource", 
    		Foo.class);

