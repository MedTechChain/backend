# HealthBlocks

Backend API for the User Management Server.

## Admin

Can do everything, i.e. the admin can do everything that a researcher can and more.

### Register New Researcher

`POST http://localhost:8088/api/users/register`

**Request Headers:**
```
Content-Type    application/json
Authorization   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk
```

**Body:**
```json
{
  "first_name": "Zeki",
  "last_name": "Erkin",
  "email": "Z.Erkin@tudelft.nl",
  "affiliation": "Delft University of Technology"
}
```


### Get All Researchers

`GET http://localhost:8088/api/users/researchers`

**Request Headers:**
```
Authorization   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk
```


### Get Filtered Researchers

`GET http://localhost:8088/api/users?first_name=Zeki&last_name=Erkin&affiliation=Delft%20University%20of%20Technology`

**Request Headers:**
```
Authorization   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk
```

**Query Params:**
```
first_name      Zeki
last_name       Erkin
affiliation     Delft University of Technology
```


### Update Personal Details

`PUT http://localhost:8088/api/users/change_details`

**Request Headers:**
```
Content-Type    application/json
Authorization   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk
```

**Body:**

```json
{
  "first_name": "Zekeriya",
  "last_name": "Erkin",
  "email": "Z.Erkin@tudelft.nl",
  "affiliation": "Delft University of Technology"
}
```


### Delete Researcher

`DELETE http://localhost:8088/api/users/delete?username=zerkin`


**Request Headers:**
```
Authorization   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk
```

**Query Params:**
```
username        zerkin
```


## Researcher
A researcher can only log in, change their password (not implemented yet) and query the chain.


### Log In
`POST http://localhost:8088/api/users/login`

**Request Headers:**
```
Content-Type    application/json
```

**Body:**
```json
{
"username": "zerkin",
"password": "%n&Z*4vRj@q@S9ww3eUpX))4)s7&p+)8W]TljG4i"
}
```


### Change Password
`PUT http://localhost:8088/api/users/change_password`


**Request Headers:**
```
Content-Type    application/json
Authorization   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMSIsInJvbGUiOiJyZXNlYXJjaGVyIn0.l-K2Bh-XtvtlTBsvn-2lRZxV6nGqjO8PuxRpiFH0Bhk
```

**Body:**
```json
{
"old_password": "%n&Z*4vRj@q@S9ww3eUpX))4)s7&p+)8W]TljG4i",
"new_password": "a$Ysit,kw%,6,!,W{i[vci#56q&[.+]r'M$P+#{n"
}
```