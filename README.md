hateoasclient
=============

Client for consuming hateoas using JSON.

Use:
====

Designed for use with Spring core and Spring Hateoas.

To use.

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

