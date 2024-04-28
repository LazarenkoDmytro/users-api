# Users API

The Users API is a Spring Boot-based RESTful service that manages user profiles. It offers capabilities to create, retrieve, update, and delete user information, as well as additional functionalities such as filtering users by their date of birth.

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Running the Application

To run the application, execute the following command in the root directory:

    mvn spring-boot:run

This will start the application on http://localhost:8080.

## API Endpoints

### Create a User

- **URL**: /users
- **Method**: POST
- **Body**:

  {<p>
  &emsp;&emsp;&emsp;&emsp;"email": "john.doe@example.com",<p>
  &emsp;&emsp;&emsp;&emsp;"firstName": "John",<p>
  &emsp;&emsp;&emsp;&emsp;"lastName": "Doe",<p>
  &emsp;&emsp;&emsp;&emsp;"dateOfBirth": "1990-01-01",<p>
  &emsp;&emsp;&emsp;&emsp;"address": "123 Main St",<p>
  &emsp;&emsp;&emsp;&emsp;"phoneNumber": "123-456-7890"<p>
  }

- **Success Response**:
    - **Code**: 201 Created
    - **Content**: User details with links to user resource

### Retrieve All Users

- **URL**: /users
- **Method**: GET
- **Success Response**:
    - **Code**: 200 OK
    - **Content**: List of users with associated resources

### Get User by Email

- **URL**: /users/{email}
- **Method**: GET
- **URL Params**: email=[string]
- **Success Response**:
    - **Code**: 200 OK
    - **Content**: User details

### Update User Information

- **URL**: /users/{email}
- **Method**: PATCH
- **URL Params**: email=[string]
- **Body**:

  {<p>
  &emsp;&emsp;&emsp;&emsp;"firstName": "Jane"<p>
  }

- **Success Response**:
    - **Code**: 200 OK
    - **Content**: Updated user details

### Replace User

- **URL**: /users/{email}
- **Method**: PUT
- **URL Params**: email=[string]
- **Body**:

  {<p>
  &emsp;&emsp;&emsp;&emsp;"email": "jane.doe@example.com",<p>
  &emsp;&emsp;&emsp;&emsp;"firstName": "Jane",<p>
  &emsp;&emsp;&emsp;&emsp;"lastName": "Doe",<p>
  &emsp;&emsp;&emsp;&emsp;"dateOfBirth": "1990-02-01",<p>
  &emsp;&emsp;&emsp;&emsp;"address": "123 Main St",<p>
  &emsp;&emsp;&emsp;&emsp;"phoneNumber": "123-456-7890"<p>
  }

- **Success Response**:
    - **Code**: 201 Created
    - **Content**: New user details

### Delete User

- **URL**: /users/{email}
- **Method**: DELETE
- **URL Params**: email=[string]
- **Success Response**:
    - **Code**: 204 No Content

### Filter Users by Birth Date Range

- **URL**: /users/by-birthdate-range
- **Method**: GET
- **Query Params**:
    - **from**: YYYY-MM-DD
    - **to**: YYYY-MM-DD
- **Success Response**:
    - **Code**: 200 OK
    - **Content**: List of users born within the specified date range

## Error Handling

The API uses standard HTTP response codes to indicate the success or failure of requests:
- 200 OK: The request was successful.
- 400 Bad Request: The request was malformed or invalid.
- 404 Not Found: The specified resource was not found.
- 500 Internal Server Error: An error occurred on the server.

## Testing

Unit and integration tests are provided under the src/test/java directory. Use the following command to run the tests:

    mvn test
