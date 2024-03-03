# MedTech Chain

Backend API for the User Management Server.


## Admin

Can do everything, i.e. the admin can do everything that a researcher can and more.


### Register New Researcher

`POST http://localhost:8088/api/users/register`

The generated username and password will be sent to the specified email.

#### Request Headers

| Header        | Value                                                                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Content-Type  | application/json                                                                                                                        |
| Authorization | Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk |


#### Body

```json
{
  "first_name": "John",
  "last_name": "Doe",
  "email": "J.Doe@tudelft.nl",
  "affiliation": "Delft University of Technology"
}
```

#### Example

##### Request

```shell
curl --location 'http://localhost:8088/api/users/register' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk' \
--data-raw '{
    "first_name": "John",
    "last_name": "Doe",
    "email": "J.Doe@tudelft.nl",
    "affiliation": "Delft University of Technology"
}'
```

##### Response

`201 CREATED`

This request does not return any response body.

-----

### Get All Researchers

`GET http://localhost:8088/api/users/researchers`

#### Request Headers

| Header        | Value                                                                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Authorization | Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk |

#### Example

##### Request

```shell
curl --location 'http://localhost:8088/api/users/researchers' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk'
```

##### Response

`200 OK`

```json
[ {
  "user_id" : "8eeda5c8-8a4c-47de-bdac-880e8c69c233",
  "first_name" : "John",
  "last_name" : "Doe",
  "email" : "J.Doe@tudelft.nl",
  "affiliation" : "Delft University of Technology"
}, {
  "user_id" : "0abe1518-07a1-4782-a2fa-af4a5a2adf6b",
  "first_name" : "Jane",
  "last_name" : "Doe",
  "email" : "J.Doe-1@tudelft.nl",
  "affiliation" : "Delft University of Technology"
} ]
```

---

### Get Filtered Researchers

`GET http://localhost:8088/api/users/researchers?first_name=John&last_name=Doe&affiliation=Delft%20University%20of%20Technology`

#### Request Headers

| Header        | Value                                                                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Authorization | Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk |

#### Query Parameters

| Parameter   | Value                          |
|-------------|--------------------------------|
| first_name  | John                           |
| last_name   | Doe                            |
| affiliation | Delft University of Technology |


#### Example

##### Request

*To be added*

##### Response

*To be added*

---

### Update Personal Details

`PUT http://localhost:8088/api/users/update?user_id=8eeda5c8-8a4c-47de-bdac-880e8c69c233`

#### Request Headers

| Header        | Value                                                                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Content-Type  | application/json                                                                                                                        |
| Authorization | Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk |

#### Query Parameters

| Parameter | Value                                |
|-----------|--------------------------------------|
| user_id   | 8eeda5c8-8a4c-47de-bdac-880e8c69c233 |

#### Body

```json
{
  "first_name": "Johnny",
  "last_name": "Doe",
  "affiliation": "Delft University of Technology"
}
```

#### Example

##### Request

*To be added*

##### Request

*To be added*

---

### Delete Researcher

`DELETE http://localhost:8088/api/users/delete?user_id=8eeda5c8-8a4c-47de-bdac-880e8c69c233`

#### Request Headers

| Header        | Value                                                                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Authorization | Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk |

#### Query Params

| Parameter | Value                                |
|-----------|--------------------------------------|
| user_id   | 8eeda5c8-8a4c-47de-bdac-880e8c69c233 |


#### Example

##### Request

```shell
curl --location --request DELETE 'http://localhost:8088/api/users/delete?user_id=8eeda5c8-8a4c-47de-bdac-880e8c69c233' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk'
```

##### Response

`200 OK`

This request does not return any response body.

---

## Researcher

A researcher can only log in, change their password and query the chain.


### Log In

`POST http://localhost:8088/api/users/login`

#### Request Headers

| Header        | Value                                                                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Content-Type  | application/json                                                                                                                        |

#### Body

```json
{
  "username": "jdoe",
  "password": "%n&Z*4vRj@q@S9ww3eUpX))4)s7&p+)8W]TljG4i"
}
```

#### Example

##### Request

```shell
curl --location 'http://localhost:8088/api/users/login' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username": "jdoe",
    "password": "%n&Z*4vRj@q@S9ww3eUpX))4)s7&p+)8W]TljG4i"
}'
```

##### Response

`200 OK`

```json
{
    "jwt": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3NTlhYTIwYy0zMTA2LTRhYzgtODNjNi02MjQzMGUzMTYxNDkiLCJyb2xlIjoiUkVTRUFSQ0hFUiIsImlhdCI6MTcwODkyNTAxMywiZXhwIjoxNzA4OTI4NjEzfQ.QiEpuZOzpUHE8bKWJ35nIqTjSw835pa-7HPgSl-HUJI",
    "token_type": "JWT",
    "expires_in": 60
}
```

---

### Change Password

`PUT http://localhost:8088/api/users/change_password`

#### Request Headers

| Header        | Value                                                                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Content-Type  | application/json                                                                                                                        |
| Authorization | Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk |

#### Body

```json
{
  "username": "jdoe",
  "old_password": "%n&Z*4vRj@q@S9ww3eUpX))4)s7&p+)8W]TljG4i",
  "new_password": "a$Ysit,kw%,6,!,W{i[vci#56q&[.+]r'M$P+#{n"
}
```

#### Example

##### Request

*To be added*

##### Request

*To be added*