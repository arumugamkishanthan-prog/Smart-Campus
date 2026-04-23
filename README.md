  

             Informatics Institute of Technology
                 Client-Server Architectures
                      Coursework Report
                          5COSC022C.2
                           (2025/26)
         REST API design , development and implementation.

 
Name          :  Arumugam Kishanthan  
UoW Number    :  W2120419  
IIT Number    :  20232541  
Module Leader :  Hamed Hamzeh
Group         :  CS-19  
 
 

Part 1 — Setup & Discovery


Q: Explain the default lifecycle of a JAX-RS Resource class.

By default, JAX-RS creates a new instance of each resource class for every incoming HTTP request (per-request lifecycle). This means instance variables are not shared between requests. To safely share state across requests, this project uses a singleton DataStore backed by ConcurrentHashMap, which provides thread-safe read and write operations. This prevents race conditions when multiple requests arrive simultaneously and ensures no data is lost between requests.


Q: Why is HATEOAS considered a hallmark of advanced RESTful design?

HATEOAS (Hypermedia as the Engine of Application State) allows the API to include navigation links directly inside responses. Rather than clients relying on hardcoded or externally documented URLs, they can discover available actions and resources dynamically from the API itself. This reduces coupling between client and server — if a URL changes, only the server needs to be updated. It also makes the API self-describing, which benefits client developers by making the API explorable without consulting static documentation.






Part 2 — Room Management


Q: What are the implications of returning only IDs versus full room objects?

Returning only IDs minimises bandwidth and response size, which is beneficial for large collections. However, it forces the client to make additional HTTP requests to fetch each room's details individually, increasing latency and server load (known as the N+1 problem). Returning full objects requires more bandwidth per request but eliminates follow-up requests, resulting in better overall performance for clients that need complete data. For this use case, rooms are small objects, so returning full objects is the better trade-off.

Q: Is DELETE idempotent in your implementation?

Yes. The first DELETE request on an existing room removes it and returns 204 No Content. Any subsequent DELETE request for the same room ID also returns 204 No Content because the room no longer exists (the null check returns 204 immediately). The end state is identical regardless of how many times the request is sent — the room does not exist. This is fully consistent with the HTTP specification, which requires DELETE to be idempotent.


         
Part 3 — Sensors & Filtering

Q: Explain the technical consequences of a client sending data in the wrong format to a @Consumes(APPLICATION_JSON) endpoint.

If a client sends a request with a Content-Type of text/plain or application/xml to an endpoint annotated with @Consumes(MediaType.APPLICATION_JSON), JAX-RS automatically returns a 415 Unsupported Media Type response before the request even reaches the resource method. This provides built-in content negotiation — the framework validates the content type and rejects mismatched requests without any custom code required.

Q: Why is @QueryParam superior to encoding the filter type in the URL path?

Path parameters (e.g., /sensors/type/CO2) imply a hierarchical resource relationship, suggesting that "type" is itself a sub-resource, which is semantically incorrect. Query parameters (e.g., ?type=CO2) are the standard REST convention for filtering, searching, and sorting collections. They are optional by nature, can be combined with other parameters (e.g., ?type=CO2&status=ACTIVE), and do not alter the base resource path. Adding new filter criteria with query parameters requires no changes to the URL structure, unlike path-based filtering.



Part 4 — Sub-Resources

Q: Discuss the architectural benefits of the Sub-Resource Locator pattern.

The Sub-Resource Locator pattern delegates handling of nested paths to dedicated classes. Rather than defining every nested route (e.g., /sensors/{id}/readings, /sensors/{id}/readings/{rid}) in one large SensorResource class, readings logic is fully encapsulated in SensorReadingResource. This separation of concerns makes each class smaller and easier to maintain, test, and reason about. It also allows sub-resources to be reused or independently modified without touching the parent resource. In large APIs with deep nesting, this dramatically reduces complexity compared to a monolithic controller approach.








Part 5 — Error Handling & Logging

Q: Why is HTTP 422 more semantically accurate than 404 when a referenced resource is missing inside a valid JSON payload?

A 404 Not Found response typically means the requested URL does not exist. In this scenario, the URL /api/v1/sensors is perfectly valid and reachable. The problem is that the contents of the request body reference a roomId that does not exist in the system. The request was syntactically well-formed JSON (not a 400), but it was semantically invalid because a referenced dependency was unresolvable. HTTP 422 Unprocessable Entity precisely describes this condition — the server understood the request but could not process it due to a semantic error in the payload.


Q: From a cybersecurity standpoint, what are the risks of exposing Java stack traces to API consumers?

Exposing raw stack traces is a serious security risk for several reasons. Stack traces reveal the internal package structure and class names of the application, giving attackers a map of the codebase. They disclose the technology stack and library versions (e.g., Jersey 2.39.1, Java 11), enabling targeted exploitation of known CVEs. They may expose file paths and server configuration details. Error messages within stack traces can reveal business logic and data validation rules that attackers can exploit. The GenericExceptionMapper prevents all of this by logging the full trace server-side only, while returning a generic 500 response to the client.


Q: Why is it advantageous to use JAX-RS filters for logging rather than inserting Logger.info() manually in every resource method?

Using filters follows the Single Responsibility Principle — each resource method is responsible only for its business logic, not for cross-cutting concerns like logging. Manual insertion of logging statements in every method is repetitive, error-prone, and easy to forget on new endpoints. A single filter class automatically intercepts all requests and responses across the entire API. It can be enabled or disabled in one place, and consistent log formatting is guaranteed without developer discipline. This approach is cleaner, more maintainable, and scales better as the API grows.



 
 
